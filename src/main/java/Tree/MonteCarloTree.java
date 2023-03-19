package Tree;

import State.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.PriorityQueue;
import java.util.concurrent.*;

import static Tree.ExpansionPolicy.calculateLiberty;

public class MonteCarloTree {
    private final double cValue;
    private Node root;
    private final int[] moveDictionary;

    /**
     * The number of nodes that the expansion policy will attempt to expand. May expand less if there aren't that many nodes left to expand
     */
    private final int NUM_TO_EXPAND = Runtime.getRuntime().availableProcessors();

    private final ExecutorService executor;

    public MonteCarloTree(State state, double cValue, int colour, int depth, int[] moveDictionary) {
        this.cValue = cValue;
        root = new Node(state, colour, depth);
        this.moveDictionary = moveDictionary;
        // Create a thread pool with the number of threads available on this computer
        executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    }

    public Action search() {
        Node tree = root;
        Timer time = new Timer(29.5);
        /*
            At the start of the game, we use a move dictionary to find out move, and then we use the saved time to start
            searching our tree.
         */

        Action selectedAction = null;

        boolean useMoveDictionary = root.getDepth() < 8;
        if (useMoveDictionary) {
            selectedAction = getMoveDictionaryMove();
        }
        try {
            while (time.timeLeft()) {
                Node leaf = select(tree);

                // Get most promising nodes to simulate
                Node[] children = ExpansionPolicy.expansionNode(leaf, NUM_TO_EXPAND);

                // Simulate each child in its own Thread
                // There should never be zero nodes returned because select() should not have chosen it
                if (children.length == 0)
                    throw new RuntimeException("HELP, THIS IS BAD");

                // Create a list of runnable tasks that will be executed in separate threads
                List<Callable<Integer>> callables = new ArrayList<>();
                for (Node child : children)
                    callables.add(() -> Simulate.simulate(child));

                try {
                    // Execute all tasks. Will block until all threads have returned a value
                    List<Future<Integer>> futures = executor.invokeAll(callables);

                    // Backpropagation of results
                    for (int i = 0; i < children.length; i++) {
                        Future<Integer> future = futures.get(i);
                        int result = future.get();
                        backPropagate(result, children[i]);
                    }
                } catch (InterruptedException | ExecutionException e) {
                    throw new RuntimeException(e);
                }
            }
        } catch (NullPointerException ignore) {
        }

        System.out.println("Ran " + getRoot().getTotalPlayouts() + " times");

        if (mostVisitedNode() != null && !useMoveDictionary)
            selectedAction = mostVisitedNode().getAction();

        return selectedAction;
    }

    private Node select(Node tree) {
        Node current = tree;
        while (!current.isLeaf())
            current = UCBMove(current);

        return current;
    }

    /**
     * We now use the result of the simulation to update all the search tree nodes going up to the root.
     *
     * @param result The player that won. Either State.BLACK or State.WHITE
     * @param child  The child node that was just simulated
     */
    private void backPropagate(int result, Node child) {
        if (child.getColour() != result) {
            child.setTotalPlayouts(child.getTotalPlayouts() + 1);
            child.setTotalWins(child.getTotalWins() + 1);
        } else {
            child.setTotalPlayouts(child.getTotalPlayouts() + 1);
        }

        while (child.getParent() != null) {
            child = child.getParent();
            if (child.getColour() != result) {
                child.setTotalPlayouts(child.getTotalPlayouts() + 1);
                child.setTotalWins(child.getTotalWins() + 1);
            } else {
                child.setTotalPlayouts(child.getTotalPlayouts() + 1);
            }
        }
    }

    private Node mostVisitedNode() {
        Node bestNode = null;
        int bestCount = -1;

        Node[] children = getRoot().getChildren();
        for (Node child : children) {
            if (child == null)
                continue;
            if (child.getTotalPlayouts() > bestCount) {
                bestNode = child;
                bestCount = child.getTotalPlayouts();
            }
        }
        return bestNode;
    }

    private Node UCBMove(Node n) {
        Node bestChild = null;
        double bestValue = -1;
        for (Node child : n.getChildren()) {
            if (child == null)
                continue;
            double val = UCBEquation(child);
            if (val > bestValue) {
                bestValue = val;
                bestChild = child;
            }
        }
        return bestChild;
    }

    private double UCBEquation(Node n) {
        return (double) n.getTotalWins() / n.getTotalPlayouts() + cValue * Math.sqrt(Math.log((double) n.getParent().getTotalPlayouts() / n.getTotalPlayouts()));
    }

    private Action getMoveDictionaryMove() {
        PriorityQueue<int[]> possibleActions;
        Action selected;
        int[][] queens = this.root.getState().getQueens(this.root.getColour());
        if (root.getDepth() < 4)
            possibleActions = getDictionaryMoves(queens);
        else {
            int min = 50;
            int x = queens[0][0];
            int y = queens[0][1];
            for (int[] item : queens) {
                int liberty = calculateLiberty(item[0], item[1], root.getState().getBitBoard());
                if (liberty < min) {
                    min = liberty;
                    x = item[0];
                    y = item[1];
                }
            }
            possibleActions = getDictionaryMoves(new int[][]{{x, y}});
        }
        do {
            int[] k = possibleActions.poll();
            selected = new Action(k[1], k[2], k[3], k[4], k[5], k[6]);
        } while (!Arrays.asList(root.getPossibleActions()).contains(selected));
        return selected;
    }

    private PriorityQueue<int[]> getDictionaryMoves(int[][] queens) {
        /*
            Gets the most common actions made from our move dictionary for the queens specified in the input array. The
            priority queue sorts based on the action weight (how often it is used).
         */
        PriorityQueue<int[]> topActions = new PriorityQueue<>((o1, o2) -> (o2[0]) - (o1[0]));
        for (int[] queen : queens) {
            int x = queen[0];
            int y = queen[1];
            // Parse through all the possible actions
            for (int i = 0; i < 100; i++) {
                for (int j = 0; j < 100; j++) {
                    StringBuilder sb = new StringBuilder();
                    sb.append(j < 10 ? "0" + j : j + "");
                    sb.append(i < 10 ? "0" + i : i + "");
                    sb.append(y == 0 ? "0" + x : x + y * 10);
                    int index = Integer.parseInt(sb.toString());
                    int num = moveDictionary[index];
                    /*
                        The index is formatted AANNOO where OO is the old index, NN is the new index, and AA is the
                        index of the arrow shot. AS such, we need to extract and convert each number which is an int
                        from 0 to 99 into an (x,y) coordinates
                     */
                    int oldX = index % 10;
                    int oldY = (index % 100) / 10;
                    int newX = (index % 1000) / 100;
                    int newY = (index % 10000) / 1000;
                    int arrowX = (index % 100000) / 10000;
                    int arrowY = (index % 1000000) / 100000;
                    topActions.offer(new int[]{num, oldX, oldY, newX, newY, arrowX, arrowY});
                }
            }
        }
        return topActions;
    }

    public void updateRoot(State state, Action action, int colour, int depth) {
        /*
            Updates the root of the tree to either be a child of the old root if it was in the old tree, or the newly
            created node if not.
         */
        Node updatedRoot = new Node(state, action, colour, depth);
        int index = Arrays.asList(root.getChildren()).indexOf(updatedRoot);
        if (index == -1)
            root = updatedRoot;
        else
            root = root.getChildren()[index];
    }

    public Node getRoot() {
        return this.root;
    }

    public double getCValue() {
        return this.cValue;
    }

    public void setRoot(Node root) {
        this.root = root;
    }
}
