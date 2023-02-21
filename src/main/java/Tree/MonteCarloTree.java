package Tree;

import State.*;

public class MonteCarloTree {
    private final double cValue;
    private Node root;

    public MonteCarloTree(State state, double cValue, int colour, int depth) {
        this.cValue = cValue;
        root = new Node(state, colour, depth);
    }

    public Action search() {
        Node tree = root;
        Timer time = new Timer(29.5);
        while (time.timeLeft()) {
            Node leaf = select(tree);
            Node child = expand(leaf);
            int result = Simulate.simulate(child);
            backPropagate(result, child);
        }
        System.out.println("Ran " + getRoot().getTotalPlayouts() + " times");
        return mostVisitedNode().getAction();
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
     * @param child The child node that was just simulated
     */
    public void backPropagate(int result, Node child) {
        if (child.getColour() == result) {
            child.setTotalPlayouts(child.getTotalPlayouts() + 1);
            child.setTotalWins(child.getTotalWins() + 1);
        } else {
            child.setTotalPlayouts(child.getTotalPlayouts() + 1);
        }

        while (child.getParent() != null) {
            child = child.getParent();
            if (child.getColour() == result) {
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
