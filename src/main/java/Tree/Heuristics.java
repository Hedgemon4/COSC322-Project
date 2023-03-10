package Tree;

import State.BitBoard;

public class Heuristics {
    public static int calculateTileControl(int x, int y, BitBoard board) {
        // this is the number of tiles you can move to from this position
        int moves = 0;

        // Calculate filled tiles
        long spaceTop = 0L;
        long spaceBottom = 0L;
        spaceTop |= board.getArrowTop();
        spaceTop |= board.getBlackQueensTop();
        spaceTop |= board.getWhiteQueensTop();
        spaceBottom |= board.getArrowBottom();
        spaceBottom |= board.getWhiteQueensBottom();
        spaceBottom |= board.getBlackQueensBottom();

        long potentialMovesBottom = 0L;
        long potentialMovesTop = 0L;

        // In each direction, need to figure out moves for top and bottom
        int index = x + y * 10;
        boolean top = false;
        if (index > 49) {
            index -= 50;
            top = true;
        }

        long potentialMovesLeft = 0B111111111;
        long potentialMovesRight = 0B111111111;
        long potentialMovesUpTop = 0B11111;
        long potentialMovesUpBottom = 0B1111;
        long potentialMovesDownTop = 0B11111;
        long potentialMovesDownBottom = 0B1111;
        long potentialMovesUpRightTop = 0B01000000000010000000000100000000001000000000010000000000L;
        long potentialMovesUpRightBottom = 0B010000000000100000000001000000000010000000000L;
        long potentialMovesUpLeftTop = 0B0100000000100000000100000000100000000100000000L;
        long potentialMovesUpLeftBottom = 0B0100000000100000000100000000100000000L;
        long potentialMovesDownRightTop = 0B010000000000100000000001000000000010000000000L;
        long potentialMovesDownRightBottom = 0B01000000000010000000000100000000001000000000010000000000L;
        long potentialMovesDownLeftTop = 0B0100000000100000000100000000100000000100000000L;
        long potentialMovesDownLeftBottom = 0B0100000000100000000100000000100000000L;

        int mask = 0;

        // Computer Moves Left
        if (x - 1 > -1) {
            potentialMovesLeft = potentialMovesLeft >> (9 - x);
            potentialMovesLeft = potentialMovesLeft << (index - 1);
            if (top)
                potentialMovesTop |= potentialMovesLeft & spaceTop;
            else
                potentialMovesBottom |= potentialMovesLeft & spaceBottom;
        }

        return (Long.bitCount(potentialMovesTop) + Long.bitCount(potentialMovesBottom));
    }
}
