package Tests;

import State.Action;
import State.ActionGenerator;
import State.State;
import Tree.Heuristics;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

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

        Random r = new Random();
        ArrayList<State> states = new ArrayList<>();
        states.add(s);
        int numStates = 10000;
        for (int i = 0; i < numStates; i++) {
            State randomState = states.get(r.nextInt(states.size()));
            ArrayList<Action> actions = ActionGenerator.generateActions(randomState, r.nextInt(1) + 1);
            states.add(new State(s, actions.get(r.nextInt(actions.size()))));
        }

        long start = System.currentTimeMillis();
        for (State state : states)
            Heuristics.bigPoppa(state, 1);
        long end = System.currentTimeMillis();
        System.out.println((double)(end - start) / numStates + "ms");
    }
}
