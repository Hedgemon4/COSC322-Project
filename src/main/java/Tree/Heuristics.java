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

        long mask = 0;
        // Computer Moves Left
        if (x - 1 > -1) {
            // Calculate the number of possible moves left
            potentialMovesLeft = potentialMovesLeft >> 9 - x;
            // Shift bits to correct position on board
            potentialMovesLeft = potentialMovesLeft << index - x;
            // And with space bottom to get occupied tiles
            potentialMovesLeft = potentialMovesLeft & (top ? spaceTop: spaceBottom);
            potentialMovesLeft = potentialMovesLeft >> index - x;
            int i = 0;
            mask = 1L << x - 1;
            while (i < x && (potentialMovesLeft & mask) == 0) {
                moves++;
                i++;
                mask = mask >> 1;
            }
        }
        if (x + 1 < 10) {
            // Calculate the number of possible moves right
            potentialMovesRight = potentialMovesRight >> x;
            // Shift bits to correct position on board
            potentialMovesRight = potentialMovesRight << index + 1;
            // And with space bottom to get occupied tiles
            potentialMovesRight = potentialMovesRight & (top ? spaceTop: spaceBottom);
            potentialMovesRight = potentialMovesRight >> index + 1;
            int i = 0;
            mask = 1L << x - 1;
            while (i < x && (potentialMovesRight & mask) == 0) {
                moves++;
                i++;
                mask = mask >> 1;
            }
        }
        return moves;
    }
}
