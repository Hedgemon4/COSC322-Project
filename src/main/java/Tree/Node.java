package Tree;

import State.Action;
import State.ActionGenerator;
import State.State;

public class Node {
    private int totalWins;
    private int totalPlayouts;
    private Node parent;
    private Node[] children;
    private Action[] possibleActions;
    private State state;
    private Action action;
    private int colour;

    public Node(State state, Action action, int colour) {
        this.state = state;
        this.action = action;
        this.colour = colour;

        possibleActions = ActionGenerator.generateActions(state, colour).toArray(new Action[0]);
        children = new Node[possibleActions.length];
    }

    public Node(State state, int colour) {
        this(state, null, colour);
    }

    public Node(State state, Action action, Node parent, int colour, int utilityValue, int totalPlayouts) {
        this.state = state;
        this.action = action;
        this.parent = parent;
        this.colour = colour;
        this.totalWins = utilityValue;
        this.totalPlayouts = totalPlayouts;
    }

    public Node(State state, Action action, Node parent, int colour, int utilityValue, int totalPlayouts, Action[] possibleActions) {
        this.state = state;
        this.action = action;
        this.parent = parent;
        this.colour = colour;
        this.totalWins = utilityValue;
        this.totalPlayouts = totalPlayouts;
        this.possibleActions = possibleActions;
        children = new Node[possibleActions.length];
    }

    public Node(int utilityValue, int totalPlayouts) {
        this.totalWins = utilityValue;
        this.totalPlayouts = totalPlayouts;
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
}
