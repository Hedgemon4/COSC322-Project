package State;

import org.apache.commons.lang.StringUtils;

import java.util.Arrays;

import static State.State.*;

public class BitBoard {
    long whiteQueens;
    long blackQueens;
    long arrow;

    public BitBoard() {
        whiteQueens = 0L;
        blackQueens = 0L;
        arrow = 0L;
    }

    public void setPiece(int row, int col, int pieceType) {
        int index = row * 10 + col;
        long mask = 1L << index;
        switch (pieceType) {
            case BLACK_QUEEN:
                blackQueens |= mask;
                break;
            case WHITE_QUEEN:
                whiteQueens |= mask;
                break;
            case ARROW:
                arrow |= mask;
                break;
        }
    }

    public void clearPiece(int row, int col, int pieceType) {
        int index = row * 10 + col;
        long mask = ~(1L << index);
        switch (pieceType) {
            case BLACK_QUEEN:
                blackQueens &= mask;
                break;
            case WHITE_QUEEN:
                whiteQueens &= mask;
                break;
            case ARROW:
                arrow &= mask;
                break;
        }
    }

    public boolean isPiece(int row, int col, int pieceType) {
        int index = row * 10 + col;
        long mask = 1L << index;
        switch (pieceType) {
            case BLACK_QUEEN:
                return (blackQueens & mask) != 0L;
            case WHITE_QUEEN:
                return (whiteQueens & mask) != 0L;
            case ARROW:
                return (arrow & mask) != 0L;
            default:
                return false;
        }
    }

    public int getPiece(int row, int col) {
        int index = row * 10 + col;
        long mask = 1L << index;
        if ((blackQueens & mask) != 0L)
            return BLACK_QUEEN;
        else if ((whiteQueens & mask) != 0L)
            return WHITE_QUEEN;
        else if ((arrow & mask) != 0L)
            return ARROW;
        else
            return 0;
    }

    public void boardToBitMap(byte[][] board) {
        System.out.println(Arrays.toString(board));
        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                if (board[i][j] != 0)
                    setPiece(i, j, board[i][j]);
            }
        }
    }

    public String toBoardString() {
        System.out.println(Long.toBinaryString(blackQueens));
        System.out.println(Long.toBinaryString(whiteQueens));
        System.out.println(Long.toBinaryString(arrow));
        StringBuilder sb = new StringBuilder();
        sb.append(StringUtils.repeat("-", 20)).append(System.lineSeparator());
        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                int pieceType = getPiece(i, j);
                switch (pieceType) {
                    case BLACK_QUEEN:
                        sb.append("B");
                        break;
                    case WHITE_QUEEN:
                        sb.append("W");
                        break;
                    case ARROW:
                        sb.append("X");
                        break;
                    default:
                        sb.append("-");
                }
                sb.append(" ");
            }
            sb.append(System.lineSeparator());
        }
        return sb.toString();
    }

    public String boardToString() {
        StringBuilder sb = new StringBuilder();

        for (int y = BOARD_SIZE - 1; y >= 0; y--) {
            for (int x = 0; x < BOARD_SIZE; x++) {
                if (x == 0)
                    sb.append(String.format("%2d ", y + 1));
                int pieceType = getPiece(x, y);
                switch (pieceType) {
                    case BLACK_QUEEN:
                        sb.append("B ");
                        break;
                    case WHITE_QUEEN:
                        sb.append("W ");
                        break;
                    case ARROW:
                        sb.append("X ");
                        break;
                    case 0:
                        sb.append("- ");
                        break;
                }
            }
            sb.append("\n");
        }
        sb.append("   a b c d e f g h i j");

        return sb.toString();
    }
}
