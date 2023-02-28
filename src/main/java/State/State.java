package State;

import java.util.ArrayList;

public class State implements Cloneable {
    // 0 = empty, 1 = black queen, 2 = white queen, 3 = arrow
    private byte[][] board;
    private int[][] blackQueens;
    private int[][] whiteQueens;

    public static final int BOARD_SIZE = 10;
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
            blackQueens = cloned.blackQueens;
            whiteQueens = cloned.whiteQueens;

            byte movingQueen = board[action.getOldX()][action.getOldY()];
            board[action.getNewX()][action.getNewY()] = movingQueen;
            board[action.getOldX()][action.getOldY()] = 0;
            board[action.getArrowX()][action.getArrowY()] = 3;

            // Update the queen that moved
            if (movingQueen == BLACK_QUEEN) {
                for (int i = 0; i < blackQueens.length; i++) {
                    if (blackQueens[i][0] == action.getOldX() && blackQueens[i][1] == action.getOldY()) {
                        blackQueens[i] = new int[]{action.getNewX(), action.getNewY()};
                        break;
                    }
                }
            } else {
                for (int i = 0; i < whiteQueens.length; i++) {
                    if (whiteQueens[i][0] == action.getOldX() && whiteQueens[i][1] == action.getOldY()) {
                        whiteQueens[i] = new int[]{action.getNewX(), action.getNewY()};
                        break;
                    }
                }
            }

        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }

    public long zobristHash() {
        return ZobristHash.hash(this);
    }

    public int[][] getQueens(int color) {
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

        clone.board = new byte[this.board.length][];
        for (int i = 0; i < this.board.length; i++) {
            clone.board[i] = this.board[i].clone();
        }

        clone.blackQueens = new int[this.blackQueens.length][];
        for (int i = 0; i < clone.blackQueens.length; i++) {
            clone.blackQueens[i] = this.blackQueens[i].clone();
        }

        clone.whiteQueens = new int[this.whiteQueens.length][];
        for (int i = 0; i < clone.whiteQueens.length; i++) {
            clone.whiteQueens[i] = this.whiteQueens[i].clone();
        }

        return clone;
    }

    public String boardToString() {
        StringBuilder out = new StringBuilder();

        for (int y = BOARD_SIZE - 1; y >= 0; y--) {
            for (int x = 0; x < BOARD_SIZE; x++) {
                if (x == 0)
                    out.append(String.format("%2d ", y + 1));
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
        out.append("   a b c d e f g h i j");

        return out.toString();
    }

    public byte[][] getBoard() {
        return board;
    }
}
