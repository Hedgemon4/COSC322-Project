package Tree;

import State.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class MonteCarloTree {
    private final double cValue;
    private Node root;

    /**
     * The number of nodes that the expansion policy will attempt to expand. May expand less if there aren't that many nodes left to expand
     */
    private final int NUM_TO_EXPAND = Runtime.getRuntime().availableProcessors();

    private final ExecutorService executor;

    public MonteCarloTree(State state, double cValue, int colour, int depth) {
        this.cValue = cValue;
        root = new Node(state, colour, depth);
        // Create a thread pool with the number of threads available on this computer
        executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    }

    public Action search() {
        Node tree = root;
        Timer time = new Timer(29.5);
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

                    // Backpropagate the results
                    for (int i = 0; i < children.length; i++) {
                        Future<Integer> future = futures.get(i);
                        int result = future.get();
                        backPropagate(result, children[i]);
                    }
                } catch (InterruptedException | ExecutionException e) {
                    throw new RuntimeException(e);
                }
            }
        } catch(NullPointerException ignore) {}

        System.out.println("Ran " + getRoot().getTotalPlayouts() + " times");

        if(mostVisitedNode()==null)
            return null;
        else
            return mostVisitedNode().getAction();
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
