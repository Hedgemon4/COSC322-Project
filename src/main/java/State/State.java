package State;

import java.util.ArrayList;

public class State implements Cloneable {
    // 0 = empty, 1 = black queen, 2 = white queen, 3 = arrow
    private byte[][] board;
    private int[][] blackQueens;
    private int[][] whiteQueens;

    public final int BOARD_SIZE = 10;
    public static final int WHITE_QUEEN = 2;
    public static final int BLACK_QUEEN = 1;
    public static final int ARROW = 3;

    public State(ArrayList<Integer> gameState) {
        board = new byte[BOARD_SIZE][BOARD_SIZE];
        blackQueens = new int[4][2];
        whiteQueens = new int[4][2];

        int blackQueensFound = 0;
        int whiteQueensFound = 0;
        for (int x = 0; x < BOARD_SIZE; x++) {
            for (int y = 0; y < BOARD_SIZE; y++) {
                byte piece = (byte)(int)gameState.get((y+1)*11 + x+1);
                board[x][y] = piece;

                // Save where the queens are placed
                if (piece == BLACK_QUEEN) {
                    blackQueens[blackQueensFound++] = new int[]{x,y};
                } else if (piece == WHITE_QUEEN) {
                    whiteQueens[whiteQueensFound++] = new int[]{x,y};
                }
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

    int[][] getQueens(int color) {
        if (color == BLACK_QUEEN)
            return blackQueens;
        else if (color == WHITE_QUEEN)
            return whiteQueens;
        else
            throw new IllegalArgumentException(color + " is not a valid color");
    }

    public int getPos(int x, int y) {
        return board[x][y];
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
                    case BLACK_QUEEN:
                        out.append("B "); break;
                    case WHITE_QUEEN:
                        out.append("W "); break;
                    case ARROW:
                        out.append("X "); break;
                }
            }
            out.append("\n");
        }

        return out.toString();
    }
}
