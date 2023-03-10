package Tests;

import State.BitBoard;
import Tree.Heuristics;

public class HeuristicTesting {
    public static void main(String[] args) {
        BitBoard board = new BitBoard();
        board.setPiece(9, 0, 1);
        System.out.println(Heuristics.calculateTileControl(9, 0, board));
    }
}
