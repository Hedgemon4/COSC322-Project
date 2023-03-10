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
        long potentialMovesUpRightTop ;

        return moves;
    }
}
