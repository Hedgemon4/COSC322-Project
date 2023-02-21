package Tree;

import State.*;

import java.util.ArrayList;

public class Simulate {

    /**
     * We perform a playout from the newly generated child node, choosing moves for both players according to the playout policy. These moves are not recorded in the search tree. In the figure, the simulation results in a win for black.
     *
     * @param node The Node to be played out
     * @return The player that won. Either State.BLACK or State.WHITE
     */
    public static int simulate(Node node) {
        return randomPlayout(node);
    }

    /**
     *
     * @param node
     * @return The result from a completely random playout of moves
     */
    private static int randomPlayout(Node node) {
        State state = new State(node.getState(), node.getAction());
        int color = node.getColour();
        int depth = node.getDepth();
        ArrayList<Action> actions = ActionGenerator.generateActions(state, color, depth);
        Action selectedAction;
        while (actions.size() != 0) {
            selectedAction = actions.get((int) (Math.random() * actions.size()));
            state = new State(state, selectedAction);
            color = color == State.BLACK_QUEEN ? State.WHITE_QUEEN : State.BLACK_QUEEN;
            actions = ActionGenerator.generateActions(state, color, ++depth);
        }
        return color == State.BLACK_QUEEN ? State.WHITE_QUEEN : State.BLACK_QUEEN;
    }
}
