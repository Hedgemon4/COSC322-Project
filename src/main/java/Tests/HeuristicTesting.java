package Tests;

import State.*;
import Tree.Heuristics;

import java.util.ArrayList;
import java.util.Arrays;

public class HeuristicTesting {
    public static void main(String[] args) {
        State s = new State(new ArrayList<>(Arrays.asList(
                0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                0, 0, 0, 0, 2, 0, 0, 2, 0, 0, 0,
                0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                0, 2, 0, 0, 0, 0, 0, 0, 0, 0, 2,
                0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 1,
                0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                0, 0, 0, 0, 1, 0, 0, 1, 0, 0, 0
        )));

        long start = System.currentTimeMillis();
        double val = Heuristics.bigPoppa(s, 1);
        long end = System.currentTimeMillis();
        System.out.println(end - start + "ms");
        System.out.println("val = " + val);
    }
}
