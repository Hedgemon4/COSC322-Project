package Tests;

import State.BitBoard;
import Tree.Heuristics;

public class HeuristicTesting {
    public static void main(String[] args) {
        BitBoard board = new BitBoard();
        board.setPiece(0, 4, 1);
        board.setPiece(9, 4, 1);
        System.out.println(Heuristics.calculateTileControl(4, 4, board));
    }
}
