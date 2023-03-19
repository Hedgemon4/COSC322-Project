package Tree;

import State.Action;
import State.ActionGenerator;
import State.State;

import java.util.Arrays;
import java.util.Objects;

public class Node {
    private int totalWins;
    private int totalPlayouts;
    private Node parent;
    private Node[] children;
    private Action[] possibleActions;
    private State state;
    private Action action;
    private int colour;
    private int depth;

    public Node(State state, Action action, int colour, int depth) {
        this.state = state;
        this.action = action;
        this.colour = colour;
        this.depth = depth;
        possibleActions = ActionGenerator.generateActions(state, colour).toArray(new Action[0]);
        children = new Node[possibleActions.length];
    }

    public Node(State state, int colour, int depth) {
        this(state, null, colour, depth);
    }

    public Node(State state, Action action, Node parent, int colour, int utilityValue, int totalPlayouts) {
        this.state = state;
        this.action = action;
        this.parent = parent;
        this.colour = colour;
        this.totalWins = utilityValue;
        this.totalPlayouts = totalPlayouts;
    }

    public Node(State state, Action action, Node parent, int colour, int utilityValue, int totalPlayouts, Action[] possibleActions, int depth) {
        this.state = state;
        this.action = action;
        this.parent = parent;
        this.colour = colour;
        this.totalWins = utilityValue;
        this.totalPlayouts = totalPlayouts;
        this.possibleActions = possibleActions;
        this.depth = depth;
        children = new Node[possibleActions.length];
    }

    public Node(int utilityValue, int totalPlayouts) {
        this.totalWins = utilityValue;
        this.totalPlayouts = totalPlayouts;
    }

    public int getDepth() {
        return depth;
    }

    public void setDepth(int depth) {
        this.depth = depth;
    }

    public Action[] getPossibleActions() {
        return possibleActions;
    }

    public int getTotalWins() {
        return totalWins;
    }

    public void setTotalWins(int totalWins) {
        this.totalWins = totalWins;
    }

    public int getTotalPlayouts() {
        return totalPlayouts;
    }

    public void setTotalPlayouts(int totalPlayouts) {
        this.totalPlayouts = totalPlayouts;
    }

    public Node getParent() {
        return parent;
    }

    public void setParent(Node parent) {
        this.parent = parent;
    }

    public Node[] getChildren() {

        return children;
    }

    public void setChildren(Node[] children) {
        this.children = children;
    }

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }

    public Action getAction() {
        return action;
    }

    public void setAction(Action action) {
        this.action = action;
    }

    public boolean isLeaf() {
        return totalPlayouts < possibleActions.length;
    }

    public int getColour() {
        return this.colour;
    }

    public void setColour(int colour) {
        this.colour = colour;
    }

    @Override
    public String toString() {
        return "Node{" +
                "totalWins=" + totalWins +
                ", totalPlayouts=" + totalPlayouts +
                ", parent=" + parent +
                ", children=" + Arrays.toString(children) +
                ", possibleActions=" + Arrays.toString(possibleActions) +
                ", state=" + state +
                ", action=" + action +
                ", colour=" + colour +
                ", depth=" + depth +
                '}';
    }

    /*
        Note that the following equals and hashCode methods only check the state, action, colour, and depth values.
        This should be used for ONLY for resetting the tree node, not any other comparison.

        TODO: Extract to another class and apply only on updates
     */

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Node node = (Node) o;
        return colour == node.colour && depth == node.depth && Objects.equals(state, node.state) && Objects.equals(action, node.action);
    }

    @Override
    public int hashCode() {
        return Objects.hash(state, action, colour, depth);
    }
}
