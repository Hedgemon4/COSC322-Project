package State;

import java.util.ArrayList;

public class ActionGenerator {
    /**
     * This method finds all positions that each queen could go to, then calls a subroutine to generate all the arrow moves from each of those queen moves.
     *
     * @param state The state on which a move is to be made
     * @param color Which color is making a move. Use State.BLACK_QUEEN or State.WHITE_QUEEN
     * @return an ArrayList of legal actions
     */
    public static ArrayList<Action> generateActions(State state, int color) {
        ArrayList<Action> moves = new ArrayList<>();

        int[][] queenPos = state.getQueens(color);
        for (int[] oldPos : queenPos) {
            // Up
            for (int y = oldPos[1] + 1; y < State.BOARD_SIZE; y++) {
                if (state.getPos(oldPos[0], y) == 0)
                    moves.addAll(getActionsFromNewQueenPos(oldPos[0], oldPos[1], oldPos[0], y, state));
                else
                    break;
            }

            // Down
            for (int y = oldPos[1] - 1; y >= 0; y--) {
                if (state.getPos(oldPos[0], y) == 0)
                    moves.addAll(getActionsFromNewQueenPos(oldPos[0], oldPos[1], oldPos[0], y, state));
                else
                    break;
            }

            // Left
            for (int x = oldPos[0] - 1; x >= 0; x--) {
                if (state.getPos(x, oldPos[1]) == 0)
                    moves.addAll(getActionsFromNewQueenPos(oldPos[0], oldPos[1], x, oldPos[1], state));
                else
                    break;
            }

            // Right
            for (int x = oldPos[0] + 1; x < State.BOARD_SIZE; x++) {
                if (state.getPos(x, oldPos[1]) == 0)
                    moves.addAll(getActionsFromNewQueenPos(oldPos[0], oldPos[1], x, oldPos[1], state));
                else
                    break;
            }

            // Up right
            for (int offset = 1; offset <= Math.min(State.BOARD_SIZE - 1 - oldPos[0], State.BOARD_SIZE - 1 - oldPos[1]); offset++) {
                if (state.getPos(oldPos[0] + offset, oldPos[1] + offset) == 0)
                    moves.addAll(getActionsFromNewQueenPos(oldPos[0], oldPos[1], oldPos[0] + offset, oldPos[1] + offset, state));
                else
                    break;
            }

            // Up Left
            for (int offset = 1; offset <= Math.min(oldPos[0], State.BOARD_SIZE - 1 - oldPos[1]); offset++) {
                if (state.getPos(oldPos[0] - offset, oldPos[1] + offset) == 0)
                    moves.addAll(getActionsFromNewQueenPos(oldPos[0], oldPos[1], oldPos[0] - offset, oldPos[1] + offset, state));
                else
                    break;
            }


            // Down left
            for (int offset = 1; offset <= Math.min(oldPos[0], oldPos[1]); offset++) {
                if (state.getPos(oldPos[0] - offset, oldPos[1] - offset) == 0)
                    moves.addAll(getActionsFromNewQueenPos(oldPos[0], oldPos[1], oldPos[0] - offset, oldPos[1] - offset, state));
                else
                    break;
            }

            // Down right
            for (int offset = 1; offset <= Math.min(State.BOARD_SIZE - 1 - oldPos[0], oldPos[1]); offset++) {
                if (state.getPos(oldPos[0] + offset, oldPos[1] - offset) == 0)
                    moves.addAll(getActionsFromNewQueenPos(oldPos[0], oldPos[1], oldPos[0] + offset, oldPos[1] - offset, state));
                else
                    break;
            }
        }

        return moves;
    }

    private static ArrayList<Action> getActionsFromNewQueenPos(int oldX, int oldY, int newX, int newY, State state) {
        ArrayList<Action> moves = new ArrayList<>();

        // Up
        for (int y = newY + 1; y < State.BOARD_SIZE; y++) {
            if ((newX == oldX && y == oldY) || state.getPos(newX, y) == 0)
                moves.add(new Action(oldX, oldY, newX, newY, newX, y));
            else
                break;
        }

        // Down
        for (int y = newY - 1; y >= 0; y--) {
            if ((newX == oldX && y == oldY) || state.getPos(newX, y) == 0)
                moves.add(new Action(oldX, oldY, newX, newY, newX, y));
            else
                break;
        }

        // Left
        for (int x = newX - 1; x >= 0; x--) {
            if ((x == oldX && newY == oldY) || state.getPos(x, newY) == 0)
                moves.add(new Action(oldX, oldY, newX, newY, x, newY));
            else
                break;
        }

        // Right
        for (int x = newX + 1; x < State.BOARD_SIZE; x++) {
            if ((x == oldX && newY == oldY) || state.getPos(x, newY) == 0)
                moves.add(new Action(oldX, oldY, newX, newY, x, newY));
            else
                break;
        }

        // Up right
        for (int offset = 1; offset <= Math.min(State.BOARD_SIZE - 1 - newX, State.BOARD_SIZE - 1 - newY); offset++) {
            if ((newX + offset == oldX && newY + offset == oldY) || state.getPos(newX + offset, newY + offset) == 0)
                moves.add(new Action(oldX, oldY, newX, newY, newX + offset, newY + offset));
            else
                break;
        }

        // Up Left
        for (int offset = 1; offset <= Math.min(newX, State.BOARD_SIZE - 1 - newY); offset++) {
            if ((newX - offset == oldX && newY + offset == oldY) || state.getPos(newX - offset, newY + offset) == 0)
                moves.add(new Action(oldX, oldY, newX, newY, newX - offset, newY + offset));
            else
                break;
        }

        // Down left
        for (int offset = 1; offset <= Math.min(newX, newY); offset++) {
            if ((newX - offset == oldX && newY - offset == oldY) || state.getPos(newX - offset, newY - offset) == 0)
                moves.add(new Action(oldX, oldY, newX, newY, newX - offset, newY - offset));
            else
                break;
        }

        // Down right
        for (int offset = 1; offset <= Math.min(State.BOARD_SIZE - 1 - newX, newY); offset++) {
            if ((newX + offset == oldX && newY - offset == oldY) || state.getPos(newX + offset, newY - offset) == 0)
                moves.add(new Action(oldX, oldY, newX, newY, newX + offset, newY - offset));
            else
                break;
        }

        return moves;
    }

    public static int generateNumberOfActions(State state, int color, int depth) {
        int count = 0;
        int[][] queenPos = state.getQueens(color);
        for (int[] oldPos : queenPos) {
            // Up
            for (int y = oldPos[1] + 1; y < State.BOARD_SIZE; y++) {
                if (state.getPos(oldPos[0], y) == 0)
                    count += getNumActionsFromNewQueenPos(oldPos[0], oldPos[1], oldPos[0], y, state);
                else
                    break;
            }

            // Down
            for (int y = oldPos[1] - 1; y >= 0; y--) {
                if (state.getPos(oldPos[0], y) == 0)
                    count += getNumActionsFromNewQueenPos(oldPos[0], oldPos[1], oldPos[0], y, state);
                else
                    break;
            }

            // Left
            for (int x = oldPos[0] - 1; x >= 0; x--) {
                if (state.getPos(x, oldPos[1]) == 0)
                    count += getNumActionsFromNewQueenPos(oldPos[0], oldPos[1], x, oldPos[1], state);
                else
                    break;
            }

            // Right
            for (int x = oldPos[0] + 1; x < State.BOARD_SIZE; x++) {
                if (state.getPos(x, oldPos[1]) == 0)
                    count += getNumActionsFromNewQueenPos(oldPos[0], oldPos[1], x, oldPos[1], state);
                else
                    break;
            }

            // Up right
            for (int offset = 1; offset <= Math.min(State.BOARD_SIZE - 1 - oldPos[0], State.BOARD_SIZE - 1 - oldPos[1]); offset++) {
                if (state.getPos(oldPos[0] + offset, oldPos[1] + offset) == 0)
                    count += getNumActionsFromNewQueenPos(oldPos[0], oldPos[1], oldPos[0] + offset, oldPos[1] + offset, state);
                else
                    break;
            }

            // Up Left
            for (int offset = 1; offset <= Math.min(oldPos[0], State.BOARD_SIZE - 1 - oldPos[1]); offset++) {
                if (state.getPos(oldPos[0] - offset, oldPos[1] + offset) == 0)
                    count += getNumActionsFromNewQueenPos(oldPos[0], oldPos[1], oldPos[0] - offset, oldPos[1] + offset, state);
                else
                    break;
            }


            // Down left
            for (int offset = 1; offset <= Math.min(oldPos[0], oldPos[1]); offset++) {
                if (state.getPos(oldPos[0] - offset, oldPos[1] - offset) == 0)
                    count += getNumActionsFromNewQueenPos(oldPos[0], oldPos[1], oldPos[0] - offset, oldPos[1] - offset, state);
                else
                    break;
            }

            // Down right
            for (int offset = 1; offset <= Math.min(State.BOARD_SIZE - 1 - oldPos[0], oldPos[1]); offset++) {
                if (state.getPos(oldPos[0] + offset, oldPos[1] - offset) == 0)
                    count += getNumActionsFromNewQueenPos(oldPos[0], oldPos[1], oldPos[0] + offset, oldPos[1] - offset, state);
                else
                    break;
            }
        }
        return count;
    }

    private static int getNumActionsFromNewQueenPos(int oldX, int oldY, int newX, int newY, State state) {
        int count = 0;
        // Up
        for (int y = newY + 1; y < State.BOARD_SIZE; y++) {
            if ((newX == oldX && y == oldY) || state.getPos(newX, y) == 0)
                count++;
            else
                break;
        }

        // Down
        for (int y = newY - 1; y >= 0; y--) {
            if ((newX == oldX && y == oldY) || state.getPos(newX, y) == 0)
                count++;
            else
                break;
        }

        // Left
        for (int x = newX - 1; x >= 0; x--) {
            if ((x == oldX && newY == oldY) || state.getPos(x, newY) == 0)
                count++;
            else
                break;
        }

        // Right
        for (int x = newX + 1; x < State.BOARD_SIZE; x++) {
            if ((x == oldX && newY == oldY) || state.getPos(x, newY) == 0)
                count++;
            else
                break;
        }

        // Up right
        for (int offset = 1; offset <= Math.min(State.BOARD_SIZE - 1 - newX, State.BOARD_SIZE - 1 - newY); offset++) {
            if ((newX + offset == oldX && newY + offset == oldY) || state.getPos(newX + offset, newY + offset) == 0)
                count++;
            else
                break;
        }

        // Up Left
        for (int offset = 1; offset <= Math.min(newX, State.BOARD_SIZE - 1 - newY); offset++) {
            if ((newX - offset == oldX && newY + offset == oldY) || state.getPos(newX - offset, newY + offset) == 0)
                count++;
            else
                break;
        }

        // Down left
        for (int offset = 1; offset <= Math.min(newX, newY); offset++) {
            if ((newX - offset == oldX && newY - offset == oldY) || state.getPos(newX - offset, newY - offset) == 0)
                count++;
            else
                break;
        }

        // Down right
        for (int offset = 1; offset <= Math.min(State.BOARD_SIZE - 1 - newX, newY); offset++) {
            if ((newX + offset == oldX && newY - offset == oldY) || state.getPos(newX + offset, newY - offset) == 0)
                count++;
            else
                break;
        }

        return count;
    }
}
