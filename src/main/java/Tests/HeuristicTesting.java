package Tests;

import State.BitBoard;
import Tree.Heuristics;

public class HeuristicTesting {
    public static void main(String[] args) {
        BitBoard board = new BitBoard();
        System.out.println(Heuristics.calculateTileControl(4, 4, board));
    }
}
