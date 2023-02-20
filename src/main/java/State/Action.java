package State;

import ygraph.ai.smartfox.games.amazons.AmazonsGameMessage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class Action {
    private final ArrayList<Integer> oldPos;
    private final ArrayList<Integer> newPos;
    private final ArrayList<Integer> arrowPos;

    /**
     * Converts a map to an action. Each action in the input is 1 indexed and is converted to a 0 indexed action internally
     * @param actionMap The map returned by the server
     */
    @SuppressWarnings("unchecked")
    public Action(Map<String, Object> actionMap) {
        oldPos = (ArrayList<Integer>) ((ArrayList<Integer>) actionMap.get(AmazonsGameMessage.QUEEN_POS_CURR)).clone();
        newPos = (ArrayList<Integer>) ((ArrayList<Integer>) actionMap.get(AmazonsGameMessage.QUEEN_POS_NEXT)).clone();
        arrowPos = (ArrayList<Integer>) ((ArrayList<Integer>) actionMap.get(AmazonsGameMessage.ARROW_POS)).clone();

        // idk why, but the server sends 1 indexed (y, x) coordinates. kinda whack
        int oldX = oldPos.get(1);
        int oldY = oldPos.get(0);
        int newX = newPos.get(1);
        int newY = newPos.get(0);
        int arrowX = arrowPos.get(1);
        int arrowY = arrowPos.get(0);

        oldPos.set(0, oldX - 1);
        oldPos.set(1, oldY - 1);
        newPos.set(0, newX - 1);
        newPos.set(1, newY - 1);
        arrowPos.set(0, arrowX - 1);
        arrowPos.set(1, arrowY - 1);
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
        oldPos = new ArrayList<>(Arrays.asList(oldX, oldY));
        newPos = new ArrayList<>(Arrays.asList(newX, newY));
        arrowPos = new ArrayList<>(Arrays.asList(arrowX, arrowY));
    }

    public ArrayList<Integer> getOldPos() {
        return oldPos;
    }

    public ArrayList<Integer> getNewPos() {
        return newPos;
    }

    public ArrayList<Integer> getArrowPos() {
        return arrowPos;
    }

    /**
     * Represents the action in the conventional notation of [old queen pos]-[new queen pos]/[arrow pos]
     * @return The action in the conventional notation
     */
    @Override
    public String toString() {
        String[] letters = {"a", "b", "c", "d", "e", "f", "g", "h", "i", "j"};
        return letters[oldPos.get(0)] + (oldPos.get(1) + 1) + "-" + letters[newPos.get(0)] + (newPos.get(1) + 1) + "/" + letters[arrowPos.get(0)] + (arrowPos.get(1) + 1);
    }

    /**
     * Converts the action to a map that can be sent to the server
     * @return A map that can be sent to the server
     */
    public Map<String, Object> toServerResponse() {
        Map<String, Object> map = new HashMap<>();
        map.put(AmazonsGameMessage.QUEEN_POS_CURR, new ArrayList<>(Arrays.asList(oldPos.get(1) + 1, oldPos.get(0) + 1)));
        map.put(AmazonsGameMessage.QUEEN_POS_NEXT, new ArrayList<>(Arrays.asList(newPos.get(1) + 1, newPos.get(0) + 1)));
        map.put(AmazonsGameMessage.ARROW_POS, new ArrayList<>(Arrays.asList(arrowPos.get(1) + 1, arrowPos.get(0) + 1)));
        return map;
    }
}
