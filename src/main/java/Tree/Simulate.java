package Tree;

import State.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.PriorityQueue;

public class Simulate {

    /**
     * We perform a playout from the newly generated child node, choosing moves for both players according to the playout policy. These moves are not recorded in the search tree. In the figure, the simulation results in a win for black.
     *
     * @param node The Node to be played out
     * @return The player that won. Either State.BLACK or State.WHITE
     */
    public static int simulate(Node node) {
        return earlyTerminationPlayout(node);
    }

    public static int simulate(Node node, int[] moveDictionary) {
        return moveDictionarySimulate(node, moveDictionary);
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
        }
    }

    private static int moveDictionarySimulate(Node node, int[] moveDictionary) {
        final int TERMINATION_DEPTH = 20;
        ArrayList<Action> actions = new ArrayList<>(Arrays.asList(node.getPossibleActions()));
        int depth = 0;
        State state = node.getState();
        int color = node.getColour();
        while(actions.size() != 0 && depth < TERMINATION_DEPTH) {
            // Get the top actions from the move dictionary
            int[][] queens = node.getColour() == State.BLACK_QUEEN ? node.getState().getQueens(State.BLACK_QUEEN) :
                    node.getState().getQueens(State.WHITE_QUEEN);
            PriorityQueue<int[]> topActions = getDictionaryMoves(queens, moveDictionary);
            Action selected;
            do {
                int[] k = topActions.poll();
                selected = new Action(k[1], k[2], k[3], k[4], k[5], k[6]);
            } while (!actions.contains(selected));
            state = new State(state, selected);
            color = color == State.BLACK_QUEEN ? State.WHITE_QUEEN : State.BLACK_QUEEN;
            actions = ActionGenerator.generateActions(state, color);
            depth++;
        }
        double heuristicValue = Heuristics.bigPoppa(state, color);
        if (heuristicValue > 0)
            return State.BLACK_QUEEN;
        else
            return State.WHITE_QUEEN;
    }

    private static PriorityQueue<int[]> getDictionaryMoves(int[][] queens, int[] moveDictionary) {
        /*
            Gets the most common actions made from our move dictionary for the queens specified in the input array. The
            priority queue sorts based on the action weight (how often it is used).
         */
        PriorityQueue<int[]> topActions = new PriorityQueue<>((o1, o2) -> (o2[0]) - (o1[0]));
        for (int[] queen : queens) {
            int x = queen[0];
            int y = queen[1];
            // Parse through all the possible actions
            for (int i = 0; i < 100; i++) {
                for (int j = 0; j < 100; j++) {
                    StringBuilder sb = new StringBuilder();
                    sb.append(j < 10 ? "0" + j : j + "");
                    sb.append(i < 10 ? "0" + i : i + "");
                    sb.append(y == 0 ? "0" + x : x + y * 10);
                    int index = Integer.parseInt(sb.toString());
                    int num = moveDictionary[index];
                    /*
                        The index is formatted AANNOO where OO is the old index, NN is the new index, and AA is the
                        index of the arrow shot. AS such, we need to extract and convert each number which is an int
                        from 0 to 99 into an (x,y) coordinates
                     */
                    int oldX = index % 10;
                    int oldY = (index % 100) / 10;
                    int newX = (index % 1000) / 100;
                    int newY = (index % 10000) / 1000;
                    int arrowX = (index % 100000) / 10000;
                    int arrowY = (index % 1000000) / 100000;
                    topActions.offer(new int[]{num, oldX, oldY, newX, newY, arrowX, arrowY});
                }
            }
        }
        return topActions;
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
