package State;

import java.util.ArrayList;

public class State implements Cloneable {
    // 0 = empty, 1 = black queen, 2 = white queen, 3 = arrow
    private byte[][] board;
    private final int BOARD_SIZE = 10;

    public State(ArrayList<Integer> gameState) {
        board = new byte[BOARD_SIZE][BOARD_SIZE];

        for (int x = 0; x < BOARD_SIZE; x++) {
            for (int y = 0; y < BOARD_SIZE; y++) {
                board[x][y] = (byte)(int)gameState.get((x+1)*11 + y+1);
            }
        }
    }

    public State(State state, Action action) {
        try {
            State cloned = (State) state.clone();
            board = cloned.board;
            board[action.getNewPos().get(0)][action.getNewPos().get(1)] = board[action.getOldPos().get(0)][action.getOldPos().get(1)];
            board[action.getOldPos().get(0)][action.getOldPos().get(1)] = 0;
            board[action.getArrowPos().get(0)][action.getArrowPos().get(1)] = 3;

        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }

    public Object clone() throws CloneNotSupportedException {
        State clone = (State) super.clone();
        clone.board = this.board.clone();
        return clone;
    }

    public String boardToString() {
        StringBuilder out = new StringBuilder();

        for (int y = BOARD_SIZE - 1; y >= 0; y--) {
            for (int x = 0; x < BOARD_SIZE; x++) {
                int tile = board[x][y];
                switch (tile) {
                    case 0:
                        out.append("- "); break;
                    case 1:
                        out.append("B "); break;
                    case 2:
                        out.append("W "); break;
                    case 3:
                        out.append("X "); break;
                }
            }
            out.append("\n");
        }

        return out.toString();
    }
}
