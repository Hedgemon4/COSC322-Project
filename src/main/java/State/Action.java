package State;

import ygraph.ai.smartfox.games.amazons.AmazonsGameMessage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class Action {
    private int oldX;
    private int oldY;
    private int newX;
    private int newY;
    private int arrowX;
    private int arrowY;

    /**
     * Converts a map to an action. Each action in the input is 1 indexed and is converted to a 0 indexed action internally
     * @param actionMap The map returned by the server
     */
    @SuppressWarnings("unchecked")
    public Action(Map<String, Object> actionMap) {
        ArrayList<Integer> oldPos = (ArrayList<Integer>) ((ArrayList<Integer>) actionMap.get(AmazonsGameMessage.QUEEN_POS_CURR)).clone();
        ArrayList<Integer> newPos = (ArrayList<Integer>) ((ArrayList<Integer>) actionMap.get(AmazonsGameMessage.QUEEN_POS_NEXT)).clone();
        ArrayList<Integer> arrowPos = (ArrayList<Integer>) ((ArrayList<Integer>) actionMap.get(AmazonsGameMessage.ARROW_POS)).clone();

        // idk why, but the server sends 1 indexed (y, x) coordinates. kinda whack
        oldX = oldPos.get(1) - 1;
        oldY = oldPos.get(0) - 1;
        newX = newPos.get(1) - 1;
        newY = newPos.get(0) - 1;
        arrowX = arrowPos.get(1) - 1;
        arrowY = arrowPos.get(0) - 1;
    }

    /**
     * Internal constructor for the action class
     * @param oldX The old x position of the queen
     * @param oldY The old y position of the queen
     * @param newX The new x position of the queen
     * @param newY The new y position of the queen
     * @param arrowX The x position of the arrow
     * @param arrowY The y position of the arrow
     */
    Action(int oldX, int oldY, int newX, int newY, int arrowX, int arrowY) {
        this.oldX = oldX;
        this.oldY = oldY;
        this.newX = newX;
        this.newY = newY;
        this.arrowX = arrowX;
        this.arrowY = arrowY;
    }

    public int getOldX() {
        return oldX;
    }

    public void setOldX(int oldX) {
        this.oldX = oldX;
    }

    public int getOldY() {
        return oldY;
    }

    public void setOldY(int oldY) {
        this.oldY = oldY;
    }

    public int getNewX() {
        return newX;
    }

    public void setNewX(int newX) {
        this.newX = newX;
    }

    public int getNewY() {
        return newY;
    }

    public void setNewY(int newY) {
        this.newY = newY;
    }

    public int getArrowX() {
        return arrowX;
    }

    public void setArrowX(int arrowX) {
        this.arrowX = arrowX;
    }

    public int getArrowY() {
        return arrowY;
    }

    public void setArrowY(int arrowY) {
        this.arrowY = arrowY;
    }

    /**
     * Represents the action in the conventional notation of [old queen pos]-[new queen pos]/[arrow pos]
     * @return The action in the conventional notation
     */
    @Override
    public String toString() {
        String[] letters = {"a", "b", "c", "d", "e", "f", "g", "h", "i", "j"};
        return letters[oldX] + (oldY + 1) + "-" + letters[newX] + (newY + 1) + "/" + letters[arrowX] + (arrowY + 1);
    }

    /**
     * Converts the action to a map that can be sent to the server
     * @return A map that can be sent to the server
     */
    public Map<String, Object> toServerResponse() {
        Map<String, Object> map = new HashMap<>();
        map.put(AmazonsGameMessage.QUEEN_POS_CURR, new ArrayList<>(Arrays.asList(oldY + 1, oldX + 1)));
        map.put(AmazonsGameMessage.QUEEN_POS_NEXT, new ArrayList<>(Arrays.asList(newY + 1, newX + 1)));
        map.put(AmazonsGameMessage.ARROW_POS, new ArrayList<>(Arrays.asList(arrowY + 1, arrowX + 1)));
        return map;
    }
}
