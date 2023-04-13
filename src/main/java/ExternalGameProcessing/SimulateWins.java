package ExternalGameProcessing;

import State.*;

import java.io.*;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class SimulateWins {
    public static void main(String[] args) {
//        State closeToLastMove = new State(new ArrayList<>(Arrays.asList(
//                0,0,0,0,0,0,0,0,0,0,0,
//                0,1,1,0,3,0,0,0,0,0,0,
//                0,1,1,0,3,0,0,0,0,0,0,
//                0,3,3,3,0,0,0,0,0,0,0,
//                0,0,0,0,0,0,0,0,0,0,0,
//                0,0,0,0,0,0,0,0,0,0,0,
//                0,0,0,0,0,0,0,0,0,0,0,
//                0,0,0,0,0,0,0,0,0,0,0,
//                0,0,0,0,0,0,0,0,0,0,0,
//                0,0,0,0,0,0,0,0,0,0,0,
//                0,0,2,2,2,2,0,0,0,0,0
//        )));
//        System.out.println(closeToLastMove.boardToString());
//        double[] winRates = simulateWin(closeToLastMove, State.WHITE_QUEEN, 100);
//        System.out.println("Black wins: " + winRates[0]*100 + "%");
//        System.out.println("White wins: " + winRates[1]*100 + "%");

//        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream("bin/training/state_list/state_list0.dat"))) {
//            in.readObject();
//            in.readObject();
//            Object[] data = (Object[]) in.readObject();
//            System.out.println(data[0]);
//            System.out.println(((State)data[1]).boardToString());
//            System.out.println(((State)data[2]).boardToString());
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        } catch (ClassNotFoundException e) {
//            throw new RuntimeException(e);
//        }

        File file = new File("bin/training/state_list");
        File[] files = file.listFiles();
        AtomicInteger gamesSimulated = new AtomicInteger(1);
        int totalGames = 1100000;
        long startTime = System.currentTimeMillis();
        for (int i = 0; i < files.length; i++) {
            int finalI = i;
            new Thread(() -> {
                try (BufferedWriter writer = new BufferedWriter(new FileWriter("bin/training/win_probabilities/win_probabilities" + finalI + ".tsv"))) {
                    try (ObjectInputStream in = new ObjectInputStream(new BufferedInputStream(Files.newInputStream(files[finalI].toPath())))) {
                        while (true) {
                            // Read in data
                            Object[] data = (Object[]) in.readObject();
                            int player = (int) data[0];
                            State initialState = (State) data[1];

                            // Simulate games
                            double[] winRates = simulateWin(initialState, player, 100);

                            // Convert initial state a list of integers
                            StringBuilder stateList = new StringBuilder("[");
                            for (int y = 0; y < State.BOARD_SIZE; y++) {
                                for (int x = 0; x < State.BOARD_SIZE; x++) {
                                    stateList.append(initialState.getPos(x, y));
                                }
                            }
                            stateList.append("]");

                            // Write data
                            writer.write(player + "\t" + stateList + "\t" + winRates[0] + "\t" + winRates[1] + "\n");

                            gamesSimulated.getAndIncrement();
                        }
                    } catch (IOException ignored) {
                    } catch (ClassNotFoundException e) {
                        throw new RuntimeException(e);
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }).start();
        }

        // Print out progress
        while (gamesSimulated.get() < totalGames) {
            // Print progress
            // Calculate estimated time remaining
            long seconds = (System.currentTimeMillis() - startTime) / gamesSimulated.get() * (totalGames - gamesSimulated.get()) / 1000;
            long second = seconds % 60;
            long minute = (seconds / 60) % 60;
            long hour = seconds / 3600;

            String time = String.format("%02d:%02d:%02d", hour, minute, second);

            System.out.printf("\r%6.3f%% [%-31s] %d/%d - Time Remaining: %s", ((double) gamesSimulated.get() / totalGames) * 100, new String(new char[gamesSimulated.get() * 30 / totalGames]).replace("\0", "=") + ">", gamesSimulated.get(), totalGames, time);

            try {
                TimeUnit.MILLISECONDS.sleep(500);
            } catch (InterruptedException ignore) {}
        }

    }

    private static double[] simulateWin(State state, int player, int numTrials) {
        int blackWins = 0, whiteWins = 0;

        Random r = new Random();

        for (int i = 0; i < numTrials; i++) {
            int currentColor = player;
            State s;
            try {
                s = (State) state.clone();
            } catch (CloneNotSupportedException e) {
                throw new RuntimeException(e);
            }

            // Make random moves while moves are possible
            ArrayList<Action> actions = ActionGenerator.generateActions(s, currentColor, 100);
            while (actions.size() > 0) {
                s = new State(s, actions.get(r.nextInt(actions.size())));
                currentColor = currentColor == State.BLACK_QUEEN ? State.WHITE_QUEEN : State.BLACK_QUEEN;
                actions = ActionGenerator.generateActions(s, currentColor, 100);
            }

            if (currentColor == State.BLACK_QUEEN) {
                whiteWins++;
            } else {
                blackWins++;
            }
        }

        return new double[]{(double) blackWins / numTrials, (double) whiteWins / numTrials};
    }
}
