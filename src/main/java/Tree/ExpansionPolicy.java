package Tree;

import State.Action;
import State.ActionGenerator;
import State.State;

public class ExpansionPolicy {
    public static Node expansionNode(Node node) {
        return randomExpansion(node);
    }

    private static Node randomExpansion(Node node) {
        int randomInt = (int) (Math.random() * node.getPossibleActions().length);
        while(node.getChildren()[randomInt] != null) {
            randomInt = (int) (Math.random() * node.getPossibleActions().length);
        }
        Action randomAction = node.getPossibleActions()[randomInt];
        State state = new State(node.getState(), randomAction);
        int colour =  node.getColour() == State.BLACK_QUEEN ? State.WHITE_QUEEN: State.BLACK_QUEEN;
        Action[] actions =  ActionGenerator.generateActions(state, colour, node.getDepth()).toArray(new Action[0]);
        Node expansion = new Node(state, randomAction, node, colour, 0, 0, actions, node.getDepth() + 1);
        node.getChildren()[randomInt] = expansion;
        return expansion;
    }
}
