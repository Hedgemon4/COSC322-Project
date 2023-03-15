package Tree;

import State.Action;
import State.ActionGenerator;
import State.State;
import NeuralNetwork.Model;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;

import java.util.ArrayList;

public class Simulate {
    /**
     * We perform a playout from the newly generated child node, choosing moves for both players according to the playout policy. These moves are not recorded in the search tree. In the figure, the simulation results in a win for black.
     *
     * @param node The Node to be played out
     * @return The player that won. Either State.BLACK or State.WHITE
     */
    public static double[] simulate(Node node, int threadNum) {
//        return earlyTerminationPlayout(node);
        return neuralAidedPlayout(node, threadNum);
    }

    private static double[] neuralAidedPlayout(Node node, int threadNum) {
        // Convert the state to a 10x10x3 array
        State nodeState = node.getState();
        INDArray input = Nd4j.zeros(1, 10, 10, 3);
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                int piece = nodeState.getPos(i,j);
                if (piece == State.BLACK_QUEEN) {
                    input.putScalar(new int[]{0, i, j, 0}, 1);
                } else if (piece == State.WHITE_QUEEN) {
                    input.putScalar(new int[]{0, i, j, 1}, 1);
                } else if (piece == State.ARROW) {
                    input.putScalar(new int[]{0, i, j, 2}, 1);
                }
            }
        }

        // Get the output of the model
        return Model.getModel(threadNum).output(input).data().getDoublesAt(0,2);
    }

    /**
     * @param node The Node to be played out
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

    private static int earlyTerminationPlayout(Node node) {
        int i = 0;
        final int TERMINATION_DEPTH = 35;
        State state = new State(node.getState(), node.getAction());
        int color = node.getColour();
        int depth = node.getDepth();
        ArrayList<Action> actions = ActionGenerator.generateActions(state, color, depth);
        Action selectedAction;
        while (actions.size() != 0 && i < TERMINATION_DEPTH) {
            selectedAction = actions.get((int) (Math.random() * actions.size()));
            state = new State(state, selectedAction);
            color = (color == State.BLACK_QUEEN) ? State.WHITE_QUEEN : State.BLACK_QUEEN;
            actions = ActionGenerator.generateActions(state, color, ++depth);
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
