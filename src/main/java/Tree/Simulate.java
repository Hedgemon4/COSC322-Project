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
    public static double simulate(Node node) {
//        return earlyTerminationPlayout(node);
        return heuristicSimulation(node);
    }

    private static double heuristicSimulation(Node node) {
        State state = node.getState();

        double heuristic = Heuristics.bigPoppa(state, node.getColour());
        return heuristic;
//        if (heuristic > 0)
//            return State.BLACK_QUEEN;
//        else
//            return State.WHITE_QUEEN;
    }


    /**
     * Hot garbage. Waaaaaaaaaay too slow
     */
    private static int heuristicSimulation2(Node node) {
        State state = node.getState();
        int color = node.getColour();
        ArrayList<Action> actions = ActionGenerator.generateActions(state, color);
        while (actions.size() > 0) {
            Action definitelyTheBestAction = null;
            double bestH = Math.pow(-1, color) * Integer.MAX_VALUE;
            for (Action a : actions) {
                double h = Heuristics.bigPoppa(new State(state, a), color);
                if (color == 1 && h > bestH) {
                    definitelyTheBestAction = a;
                    bestH = h;
                } else if (color == 2 && h < bestH) {
                    definitelyTheBestAction = a;
                    bestH = h;
                }
            }
            assert definitelyTheBestAction != null;
            state = new State(state, definitelyTheBestAction);
            color = color == 1 ? 2 : 1;
            actions = ActionGenerator.generateActions(state, color);
        }

        return color == 1 ? 2 : 1;
    }

    /**
     * @param node
     * @return The result from a completely random playout of moves
     */
    private static int randomPlayout(Node node) {
        State state = new State(node.getState(), node.getAction());
        int color = node.getColour();
        int depth = node.getDepth();
        ArrayList<Action> actions = ActionGenerator.generateActions(state, color);
        Action selectedAction;
        while (actions.size() != 0) {
            selectedAction = actions.get((int) (Math.random() * actions.size()));
            state = new State(state, selectedAction);
            color = color == State.BLACK_QUEEN ? State.WHITE_QUEEN : State.BLACK_QUEEN;
            actions = ActionGenerator.generateActions(state, color);
        }
        return color == State.BLACK_QUEEN ? State.WHITE_QUEEN : State.BLACK_QUEEN;
    }

    private static int earlyTerminationPlayout(Node node) {
        int i = 0;
        final int TERMINATION_DEPTH = 35;
        State state = new State(node.getState(), node.getAction());
        int color = node.getColour();
        int depth = node.getDepth();
        ArrayList<Action> actions = ActionGenerator.generateActions(state, color);
        Action selectedAction;
        while (actions.size() != 0 && i < TERMINATION_DEPTH) {
            selectedAction = actions.get((int) (Math.random() * actions.size()));
            state = new State(state, selectedAction);
            color = (color == State.BLACK_QUEEN) ? State.WHITE_QUEEN : State.BLACK_QUEEN;
            actions = ActionGenerator.generateActions(state, color);
            i++;
        }
        if (i < TERMINATION_DEPTH)
            return color == State.BLACK_QUEEN ? State.WHITE_QUEEN : State.BLACK_QUEEN;
        else {
            int blackControl = boardControlHeuristic(state, State.BLACK_QUEEN);
            int whiteControl = boardControlHeuristic(state, State.WHITE_QUEEN);
            if (blackControl == whiteControl)
                return 0;
            else if (blackControl > whiteControl)
                return State.BLACK_QUEEN;
            else
                return State.WHITE_QUEEN;
//            double heuristic = Heuristics.bigPoppa(state, node.getColour());
//            if (heuristic > 0)
//                return State.BLACK_QUEEN;
//            else
//                return State.WHITE_QUEEN;
        }
    }

    private static int boardControlHeuristic(State state, int colour) {
        int[][] queens = state.getQueens(colour);
        byte[][] board = new byte[State.BOARD_SIZE][State.BOARD_SIZE];
        for (int[] oldPos : queens) {
            // Up
            for (int y = oldPos[1] + 1; y < state.BOARD_SIZE; y++) {
                if (state.getPos(oldPos[0], y) == 0)
                    board[oldPos[0]][y] = 1;
                else
                    break;
            }

            // Down
            for (int y = oldPos[1] - 1; y >= 0; y--) {
                if (state.getPos(oldPos[0], y) == 0)
                    board[oldPos[0]][y] = 1;
                else
                    break;
            }

            // Left
            for (int x = oldPos[0] - 1; x >= 0; x--) {
                if (state.getPos(x, oldPos[1]) == 0)
                    board[x][oldPos[1]] = 1;
                else
                    break;
            }

            // Right
            for (int x = oldPos[0] + 1; x < state.BOARD_SIZE; x++) {
                if (state.getPos(x, oldPos[1]) == 0)
                    board[x][oldPos[1]] = 1;
                else
                    break;
            }

            // Up right
            for (int offset = 1; offset <= Math.min(state.BOARD_SIZE - 1 - oldPos[0], state.BOARD_SIZE - 1 - oldPos[1]); offset++) {
                if (state.getPos(oldPos[0] + offset, oldPos[1] + offset) == 0)
                    board[oldPos[0] + offset][oldPos[1] + offset] = 1;
                else
                    break;
            }

            // Up Left
            for (int offset = 1; offset <= Math.min(oldPos[0], state.BOARD_SIZE - 1 - oldPos[1]); offset++) {
                if (state.getPos(oldPos[0] - offset, oldPos[1] + offset) == 0)
                    board[oldPos[0] - offset][oldPos[1] + offset] = 1;
                else
                    break;
            }


            // Down left
            for (int offset = 1; offset <= Math.min(oldPos[0], oldPos[1]); offset++) {
                if (state.getPos(oldPos[0] - offset, oldPos[1] - offset) == 0)
                    board[oldPos[0] - offset][oldPos[1] - offset] = 1;
                else
                    break;
            }

            // Down right
            for (int offset = 1; offset <= Math.min(state.BOARD_SIZE - 1 - oldPos[0], oldPos[1]); offset++) {
                if (state.getPos(oldPos[0] + offset, oldPos[1] - offset) == 0)
                    board[oldPos[0] + offset][oldPos[1] - offset] = 1;
                else
                    break;
            }
        }
        int control = 0;
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                control += board[i][j];
            }
        }
        return control;
    }
}
