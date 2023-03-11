package State;

import java.util.Objects;

public class ActionChecker {

    /**
     * **Expensive operation.** Do not use in bulk, probably only to validate the opponent's move.
     * @param state The State on which the Action is being taken. The move should not have been made on the state yet.
     * @param action The Action to validate
     * @return if the action is valid
     */
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public static boolean validMove(State state, Action action) {
        // If any of the queen positions are out of bounds

        if (!inBounds(action.getOldX(), action.getOldY()) || !inBounds(action.getNewX(), action.getNewY()) )
            return false;

        //if any of the arrow position are out of bound

        if ( !inBounds(action.getArrowX(), action.getArrowY()))
            return false;

        // If the oldPos wasn't a queen
        if (state.getPos(action.getOldX(), action.getOldY()) != 1 && state.getPos(action.getOldX(), action.getOldY()) != 2)
            return false;



        // If the spot where the new queen or the arrow went is occupied
        if (state.getPos(action.getNewX(), action.getNewY()) != 0 || (state.getPos(action.getArrowX(), action.getArrowY()) != 0 && !(Objects.equals(action.getArrowX(), action.getOldX()) && Objects.equals(action.getArrowY(), action.getOldY()))))
            return false;

        int xDirection = action.getNewX() - action.getOldX();
        int yDirection = action.getNewY() - action.getOldY();
        // Normalize directions
        if (xDirection != 0) xDirection /= Math.abs(xDirection);
        if (yDirection != 0) yDirection /= Math.abs(yDirection);

        int checkX = action.getOldX() + xDirection;
        int checkY = action.getOldY() + yDirection;

        // If there was a piece between the old and new queen position
        while (!((xDirection != 0 && checkX == action.getNewX()) || (yDirection != 0 && checkY == action.getNewY()))) {
            if (state.getPos(checkX, checkY) != 0)
                return false;
            checkX += xDirection;
            checkY += yDirection;
        }

        // If the new position was not in a valid direction from the old position
        if (checkX != action.getNewX() || checkY != action.getNewY())
            return false;



        // Check if the arrow is within range of the new queen position
        int deltaX = Math.abs(action.getArrowX() - action.getNewX());
        int deltaY = Math.abs(action.getArrowY() - action.getNewY());
        if (deltaX > 1 || deltaY > 1 || (deltaX == 0 && deltaY == 0)) {
            return false;
        }

        // Check if the arrow can be shot in a straight line from the new queen position
        if (deltaX == 0) {
            for (int i = Math.min(action.getArrowY(), action.getNewY()) + 1; i < Math.max(action.getArrowY(), action.getNewY()); i++) {
                if (state.getPos(action.getNewX(), i) != 0) {
                    return false;
                }
            }
        } else if (deltaY == 0) {
            for (int i = Math.min(action.getArrowX(), action.getNewX()) + 1; i < Math.max(action.getArrowX(), action.getNewX()); i++) {
                if (state.getPos(i, action.getNewY()) != 0) {
                    return false;
                }
            }
        } else if (deltaX == deltaY) {
            int startX = action.getNewX() < action.getArrowX() ? action.getNewX() : action.getArrowX();
            int startY = action.getNewY() < action.getArrowY() ? action.getNewY() : action.getArrowY();
            int endX = action.getNewX() > action.getArrowX() ? action.getNewX() : action.getArrowX();
            int endY = action.getNewY() > action.getArrowY() ? action.getNewY() : action.getArrowY();
            for (int i = 1; i < deltaX; i++) {
                if (state.getPos(startX + i, startY + i) != 0) {
                    return false;
                }
            }
        } else {
            return false;
        }
        return true;
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    private static boolean inBounds(int x, int y) {
        return x >= 0 && x < State.BOARD_SIZE &&
                y >= 0 && y < State.BOARD_SIZE;
    }
}
