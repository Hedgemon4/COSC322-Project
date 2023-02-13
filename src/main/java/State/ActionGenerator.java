package State;

import java.util.ArrayList;
import java.util.Objects;

public class ActionGenerator {
    public static ArrayList<Action> generateActions(State state, int color) {
        ArrayList<Action> moves = new ArrayList<>();

        int[][] queenPos = state.getQueens(color);
        for (int[] oldPos : queenPos) {
            // Up
            for (int y = oldPos[1] + 1; y < state.BOARD_SIZE; y++) {
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
            for (int x = oldPos[0] + 1; x < state.BOARD_SIZE; x++) {
                if (state.getPos(x, oldPos[1]) == 0)
                    moves.addAll(getActionsFromNewQueenPos(oldPos[0], oldPos[1], x, oldPos[1], state));
                else
                    break;
            }

            // Up right
            for (int offset = 1; offset <= Math.min(state.BOARD_SIZE - 1 - oldPos[0], state.BOARD_SIZE - 1 - oldPos[1]); offset++) {
                if (state.getPos(oldPos[0] + offset, oldPos[1] + offset) == 0)
                    moves.addAll(getActionsFromNewQueenPos(oldPos[0], oldPos[1], oldPos[0] + offset, oldPos[1] + offset, state));
                else
                    break;
            }

            // Up Left
            for (int offset = 1; offset <= Math.min(oldPos[0], state.BOARD_SIZE - 1 - oldPos[1]); offset++) {
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
            for (int offset = 1; offset <= Math.min(state.BOARD_SIZE - 1 - oldPos[0], oldPos[1]); offset++) {
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
        for (int y = newY + 1; y < state.BOARD_SIZE; y++) {
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
        for (int x = newX + 1; x < state.BOARD_SIZE; x++) {
            if ((x == oldX && newY == oldY) || state.getPos(x, newY) == 0)
                moves.add(new Action(oldX, oldY, newX, newY, x, newY));
            else
                break;
        }

        // Up right
        for (int offset = 1; offset <= Math.min(state.BOARD_SIZE - 1 - newX, state.BOARD_SIZE - 1 - newY); offset++) {
            if ((newX + offset == oldX && newY + offset == oldY) || state.getPos(newX + offset, newY + offset) == 0)
                moves.add(new Action(oldX, oldY, newX, newY, newX + offset, newY + offset));
            else
                break;
        }

        // Up Left
        for (int offset = 1; offset <= Math.min(newX, state.BOARD_SIZE - 1 - newY); offset++) {
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
        for (int offset = 1; offset <= Math.min(state.BOARD_SIZE - 1 - newX, newY); offset++) {
            if ((newX + offset == oldX && newY - offset == oldY) || state.getPos(newX + offset, newY - offset) == 0)
                moves.add(new Action(oldX, oldY, newX, newY, newX + offset, newY - offset));
            else
                break;
        }

        return moves;
    }

    /**
     * **Expensive operation.** Do not use in bulk, probably only to validate the opponent's move.
     * @param state The State on which the Action is being taken. The move should not have been made on the state yet.
     * @param action The Action to validate
     * @return if the action is valid
     */
    public static boolean validMove(State state, Action action) {
        // If any of the positions are out of bounds
        if (!inBounds(state, action.getOldPos()) || !inBounds(state, action.getNewPos()) || !inBounds(state, action.getArrowPos()))
            return false;

        // If the oldPos wasn't a queen
        if (state.getPos(action.getOldPos().get(0), action.getOldPos().get(1)) != 1 && state.getPos(action.getOldPos().get(0), action.getOldPos().get(1)) != 2)
            return false;

        // If the spot where the new queen or the arrow went is occupied
        if (state.getPos(action.getNewPos().get(0), action.getNewPos().get(1)) != 0 || (state.getPos(action.getArrowPos().get(0), action.getArrowPos().get(1)) != 0 && !(Objects.equals(action.getArrowPos().get(0), action.getOldPos().get(0)) && Objects.equals(action.getArrowPos().get(1), action.getOldPos().get(1)))))
            return false;

        int xDirection = action.getNewPos().get(0) - action.getOldPos().get(0);
        int yDirection = action.getNewPos().get(1) - action.getOldPos().get(1);
        // Normalize directions
        if (xDirection != 0) xDirection /= Math.abs(xDirection);
        if (yDirection != 0) yDirection /= Math.abs(yDirection);

        int checkX = action.getOldPos().get(0) + xDirection;
        int checkY = action.getOldPos().get(1) + yDirection;

        // If there was a piece between the old and new queen position
        while (!((xDirection != 0 && checkX == action.getNewPos().get(0)) || (yDirection != 0 && checkY == action.getNewPos().get(1)))) {
            if (state.getPos(checkX, checkY) != 0)
                return false;
            checkX += xDirection;
            checkY += yDirection;
        }

        // If the new position was not in a valid direction from the old position
        if (checkX != action.getNewPos().get(0) || checkY != action.getNewPos().get(1))
            return false;

        // Find direction of arrow from the new pos
        xDirection = action.getArrowPos().get(0) - action.getNewPos().get(0);
        yDirection = action.getArrowPos().get(1) - action.getNewPos().get(1);
        // Normalize directions
        if (xDirection != 0) xDirection /= Math.abs(xDirection);
        if (yDirection != 0) yDirection /= Math.abs(yDirection);

        // If there was a piece between the new pos and the arrow that isn't just the old piece
        while (checkX != action.getNewPos().get(0) && checkY != action.getNewPos().get(1)) {
            if (state.getPos(checkX, checkY) != 0 && !(checkX == action.getOldPos().get(0) && checkY == action.getOldPos().get(1)))
                return false;
            checkX += xDirection;
            checkY += yDirection;
        }

        return true;
    }

    private static boolean inBounds(State state, ArrayList<Integer> position) {
        return position.get(0) >= 0 && position.get(0) < state.BOARD_SIZE &&
                position.get(1) >= 0 && position.get(1) < state.BOARD_SIZE;
    }
}
