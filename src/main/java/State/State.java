package State;

import java.util.ArrayList;

public class State implements Cloneable {
    // 0 = empty, 1 = black queen, 2 = white queen, 3 = arrow
    private int[][] blackQueens;
    private int[][] whiteQueens;
    private BitBoard bitBoard;

    public static final int BOARD_SIZE = 10;
    public static final int WHITE_QUEEN = 2;
    public static final int BLACK_QUEEN = 1;
    public static final int ARROW = 3;

    public State(ArrayList<Integer> gameState) {
        bitBoard = new BitBoard();
        blackQueens = new int[4][2];
        whiteQueens = new int[4][2];
        int blackQueensFound = 0;
        int whiteQueensFound = 0;
        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                int piece = gameState.get((j + 1) * 11 + i + 1);
                if (piece == BLACK_QUEEN) {
                    blackQueens[blackQueensFound++] = new int[]{i, j};
                } else if (piece == WHITE_QUEEN) {
                    whiteQueens[whiteQueensFound++] = new int[]{i, j};
                }
                bitBoard.setPiece(i, j, piece);
            }
        }
    }

    public State(State state, Action action) {
        try {
            State cloned = (State) state.clone();
            bitBoard = cloned.bitBoard;
            blackQueens = cloned.blackQueens;
            whiteQueens = cloned.whiteQueens;

            int movingQueen = bitBoard.getPiece(action.getOldX(), action.getOldY());
            bitBoard.setPiece(action.getNewX(), action.getNewY(), movingQueen);
            bitBoard.clearPiece(action.getOldX(), action.getOldY(), movingQueen);
            bitBoard.setPiece(action.getArrowX(), action.getArrowY(), 3);

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

    public int[][] getQueens(int color) {
        if (color == BLACK_QUEEN)
            return blackQueens;
        else if (color == WHITE_QUEEN)
            return whiteQueens;
        else
            throw new IllegalArgumentException(color + " is not a valid color");
    }

    public int getPos(int x, int y) {
        return bitBoard.getPiece(x, y);
    }

    public Object clone() throws CloneNotSupportedException {
        State clone = (State) super.clone();
        clone.bitBoard = (BitBoard) this.bitBoard.clone();
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
        return bitBoard.boardToString();
    }

    public BitBoard getBitBoard() {
        return this.bitBoard;
    }
}
