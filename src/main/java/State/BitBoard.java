package State;

import org.apache.commons.lang.StringUtils;

import java.io.Serializable;
import java.util.Arrays;

import static State.State.*;

public class BitBoard implements Cloneable, Serializable {
    private long whiteQueensTop;
    private long whiteQueensBottom;
    private long blackQueensTop;
    private long blackQueensBottom;
    private long arrowTop;
    private long arrowBottom;

    public BitBoard() {
        whiteQueensTop = 0L;
        whiteQueensBottom = 0L;
        blackQueensTop = 0L;
        blackQueensBottom = 0L;
        arrowTop = 0L;
        arrowBottom = 0L;
    }

    public void setPiece(int x, int y, int pieceType) {
        int index = y * 10 + x;
        boolean top = false;
        if (index > 49) {
            index -= 50;
            top = true;
        }
        long mask = 1L << index;
        switch (pieceType) {
            case BLACK_QUEEN:
                if (top)
                    blackQueensTop |= mask;
                else
                    blackQueensBottom |= mask;
                break;
            case WHITE_QUEEN:
                if (top)
                    whiteQueensTop |= mask;
                else
                    whiteQueensBottom |= mask;
                break;
            case ARROW:
                if (top)
                    arrowTop |= mask;
                else
                    arrowBottom |= mask;
                break;
            case 0:
                break;
        }
    }

    public void clearPiece(int x, int y, int pieceType) {
        int index = y * 10 + x;
        boolean top = false;
        if (index > 49) {
            index -= 50;
            top = true;
        }
        long mask = ~(1L << index);
        switch (pieceType) {
            case BLACK_QUEEN:
                if (top)
                    blackQueensTop &= mask;
                else
                    blackQueensBottom &= mask;
                break;
            case WHITE_QUEEN:
                if (top)
                    whiteQueensTop &= mask;
                else
                    whiteQueensBottom &= mask;
                break;
            case ARROW:
                if (top)
                    arrowTop &= mask;
                else
                    arrowBottom &= mask;
                break;
        }
    }

    public boolean isPiece(int x, int y, int pieceType) {
        int index = y * 10 + x;
        boolean top = false;
        if (index > 49) {
            index -= 50;
            top = true;
        }
        long mask = 1L << index;
        switch (pieceType) {
            case BLACK_QUEEN:
                if (top)
                    return (blackQueensTop & mask) != 0L;
                else
                    return (blackQueensBottom & mask) != 0L;
            case WHITE_QUEEN:
                if (top)
                    return (whiteQueensTop & mask) != 0L;
                else
                    return (whiteQueensBottom & mask) != 0L;
            case ARROW:
                if (top)
                    return (arrowTop & mask) != 0L;
                else
                    return (arrowBottom & mask) != 0L;
            default:
                return false;
        }
    }

    public int getPiece(int x, int y) {
        int index = y * 10 + x;
        boolean top = false;
        if (index > 49) {
            index -= 50;
            top = true;
        }
        long mask = 1L << index;
        if (top && (blackQueensTop & mask) != 0L)
            return BLACK_QUEEN;
        else if (top && (whiteQueensTop & mask) != 0L)
            return WHITE_QUEEN;
        else if (top && (arrowTop & mask) != 0L)
            return ARROW;
        else if (!top && (blackQueensBottom & mask) != 0L)
            return BLACK_QUEEN;
        else if (!top && (whiteQueensBottom & mask) != 0L)
            return WHITE_QUEEN;
        else if (!top && (arrowBottom & mask) != 0L)
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

    public long getWhiteQueensTop() {
        return whiteQueensTop;
    }

    public void setWhiteQueensTop(long whiteQueensTop) {
        this.whiteQueensTop = whiteQueensTop;
    }

    public long getWhiteQueensBottom() {
        return whiteQueensBottom;
    }

    public void setWhiteQueensBottom(long whiteQueensBottom) {
        this.whiteQueensBottom = whiteQueensBottom;
    }

    public long getBlackQueensTop() {
        return blackQueensTop;
    }

    public void setBlackQueensTop(long blackQueensTop) {
        this.blackQueensTop = blackQueensTop;
    }

    public long getBlackQueensBottom() {
        return blackQueensBottom;
    }

    public void setBlackQueensBottom(long blackQueensBottom) {
        this.blackQueensBottom = blackQueensBottom;
    }

    public long getArrowTop() {
        return arrowTop;
    }

    public void setArrowTop(long arrowTop) {
        this.arrowTop = arrowTop;
    }

    public long getArrowBottom() {
        return arrowBottom;
    }

    public void setArrowBottom(long arrowBottom) {
        this.arrowBottom = arrowBottom;
    }

    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}
