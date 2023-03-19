package Tree;

import State.*;

public class MonteCarloTree {
    private final double cValue;
    private Node root;
    private final int[] moveDictionary;

    public MonteCarloTree(State state, double cValue, int colour, int depth, int[] moveDictionary) {
        this.cValue = cValue;
        root = new Node(state, colour, depth);
        this.moveDictionary = moveDictionary;
    }

    public Action search() {
        Node tree = root;
        Timer time = new Timer(29.5);
        boolean useMoveDictionary = root.getDepth() < 6;
        Action selected;
        if (useMoveDictionary) {
            selected = moveDictionaryMove();
            return selected;
        } else {
            while (time.timeLeft()) {
                Node leaf = select(tree);
                Node child = expand(leaf);
                int result = Simulate.simulate(child);
                backPropagate(result, child);
            }
            System.out.println("Ran " + getRoot().getTotalPlayouts() + " times");
            return mostVisitedNode().getAction();
        }
    }

    private Node select(Node tree) {
        Node current = tree;
        while (!current.isLeaf())
            current = UCBMove(current);

        return current;
    }

    private Node expand(Node leaf) {
        return ExpansionPolicy.expansionNode(leaf);
    }

    /**
     * We now use the result of the simulation to update all the search tree nodes going up to the root.
     *
     * @param result The player that won. Either State.BLACK or State.WHITE
     * @param child  The child node that was just simulated
     */
    public void backPropagate(int result, Node child) {
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

    private Action moveDictionaryMove() {
        int[][] queens = this.root.getState().getQueens(this.root.getColour());
        int max = 0;
        int maxIndex = 0;
        for (int s = 0; s < 4; s++) {
            int x = queens[s][0];
            int y = queens[s][1];
            for (int i = 0; i < 100; i++) {
                for (int j = 0; j < 100; j++) {
                    StringBuilder sb = new StringBuilder();
                    sb.append(j < 10 ? "0" + j : j + "");
                    sb.append(i < 10 ? "0" + i : i + "");
                    sb.append(y == 0 ? "0" + x : x + y * 10);
                    int index = Integer.parseInt(sb.toString());
                    int num = moveDictionary[index];
                    if (num > max) {
                        maxIndex = index;
                        max = num;
                    }
                }
            }
        }
        int oldX = maxIndex % 10;
        int oldY = (maxIndex % 100) / 10;
        int newX = (maxIndex % 1000) / 100;
        int newY = (maxIndex % 10000) / 1000;
        int arrowX = (maxIndex % 100000) / 10000;
        int arrowY = (maxIndex % 1000000) / 100000;
        return new Action(oldX, oldY, newX, newY, arrowX, arrowY);
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
