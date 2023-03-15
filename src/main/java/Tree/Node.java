package Tree;

import State.Action;
import State.ActionGenerator;
import State.State;

import java.util.Arrays;

public class Node {
    private double totalWins;
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
        possibleActions = ActionGenerator.generateActions(state, colour, depth).toArray(new Action[0]);
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

    public double getTotalWins() {
        return totalWins;
    }

    public void setTotalWins(double totalWins) {
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
}
