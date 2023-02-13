package State;

import java.util.ArrayList;
import java.util.Objects;

public class ActionChecker {

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
