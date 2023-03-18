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

        /*
            The strategy is to find the least significant bit set to one, subtract one, and then sum the number
            of ones

            Formula:
                - Get all 1 bits after least significant one (n & ~(n - 1)) - 1
                - AND with mask again
         */

        // Right and Up are least significant bit
        // Down and left are the most significant bit

        // Compute Moves Left

        long mask = potentialMovesLeft >> (9 - x);
        long left = mask << (index - x);
        left &= top ? spaceTop : spaceBottom;
        if (left != 0) {
            int k = (int) Math.floor(Math.log(left) / Math.log(2)) + 1;
            moves += x - k - (y * 10);
        } else {
            moves += x;
        }

        // Compute Moves Right

        mask = potentialMovesRight >> x;
        long right = mask << index + 1;
        right &= top ? spaceTop : spaceBottom;
        if (right != 0) {
            right &=  ~(right - 1);
            right -= 1;
            right = right >> x + 1;
            moves += Long.bitCount(right & mask);
        } else {
            moves += 9 - x;
        }

        // Compute Moves Up

        return moves;
    }
}
