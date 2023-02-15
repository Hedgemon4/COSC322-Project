package State;

import ygraph.ai.smartfox.games.amazons.AmazonsGameMessage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;

public class Action {
    private final ArrayList<Integer> oldPos;
    private final ArrayList<Integer> newPos;
    private final ArrayList<Integer> arrowPos;

    public Action(ArrayList<Integer> oldQueenPos, ArrayList<Integer> newQueenPos, ArrayList<Integer> arrowPos) {
        this.oldPos = oldQueenPos;
        this.newPos = newQueenPos;
        this.arrowPos = arrowPos;
    }

    /**
     * Converts a map to an action. Each action in the input is 1 indexed and is converted to a 0 indexed action internally
     * @param actionMap The map returned by the server
     */
    @SuppressWarnings("unchecked")
    public Action(Map<String, Object> actionMap) {
        newPos = (ArrayList<Integer>) actionMap.get(AmazonsGameMessage.QUEEN_POS_CURR);
        oldPos = (ArrayList<Integer>) actionMap.get(AmazonsGameMessage.QUEEN_POS_NEXT);
        arrowPos = (ArrayList<Integer>) actionMap.get(AmazonsGameMessage.ARROW_POS);

        newPos.set(0, newPos.get(0) - 1);
        newPos.set(1, newPos.get(1) - 1);
        oldPos.set(0, oldPos.get(0) - 1);
        oldPos.set(1, oldPos.get(1) - 1);
        arrowPos.set(0, arrowPos.get(0) - 1);
        arrowPos.set(1, arrowPos.get(1) - 1);
    }

    Action(int oldX, int oldY, int newX, int newY, int arrowX, int arrowY) {
        this(
                new ArrayList<>(Arrays.asList(oldX, oldY)),
                new ArrayList<>(Arrays.asList(newX, newY)),
                new ArrayList<>(Arrays.asList(arrowX, arrowY))
        );
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

    @Override
    public String toString() {
        return "Action{" +
                "oldPos=" + oldPos +
                ", newPos=" + newPos +
                ", arrowPos=" + arrowPos +
                '}';
    }
}
