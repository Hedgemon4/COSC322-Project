package ExternalGameProcessing;

import java.io.*;
import java.nio.file.Files;

import State.*;

public class StatesToCSV {
    public static void main(String[] args) throws IOException, ClassNotFoundException {
        File[] stateFiles = new File("bin/training/state_list").listFiles();

        for (int i = 0; i < stateFiles.length; i++) {
            int finalI = i;
            new Thread(() -> {
                try (
                        ObjectInputStream in = new ObjectInputStream(new BufferedInputStream(Files.newInputStream(stateFiles[finalI].toPath())));
                        BufferedWriter out = new BufferedWriter(new FileWriter("bin/training/ideal_moves/ideal_moves" + finalI + ".tsv"))
                ) {
                    while (true) {
                        // Read data
                        Object[] data = (Object[]) in.readObject();
                        int player = (int) data[0];
                        State startState = (State) data[1];
                        State idealState = (State) data[2];

                        // Write the starting state
                        for (int x = 0; x < State.BOARD_SIZE; x++)
                            for (int y = 0; y < State.BOARD_SIZE; y++)
                                out.write(startState.getPos(x, y) + '0');
                        out.write('\t');

                        // Find positions of where things moved to/from in the ideal state. These are the expected outputs of the model
                        int[] queenMovement = queenMovement(player, startState, idealState);
                        int oldQueen = queenMovement[0];
                        int newQueen = queenMovement[1];
                        int arrow = arrowPos(startState, idealState);

                        // Write to file
                        writePos(out, oldQueen);
                        out.write('\t');
                        writePos(out, newQueen);
                        out.write('\t');
                        writePos(out, arrow);
                        out.write('\n');
                    }
                } catch (IOException ignore) {
                } catch (ClassNotFoundException e) {
                    throw new RuntimeException(e);
                }
            }).start();
        }
    }

    private static void writePos(Writer out, int pos) throws IOException {
        int j = 0;
        for (; j < pos; j++)
            out.write('0');
        out.write('1');
        for (j++; j < 100; j++)
            out.write('0');
    }

    public static int[] queenMovement(int player, State start, State end) {
        BitBoard startBB = start.getBitBoard();
        BitBoard endBB = end.getBitBoard();

        if (player == State.BLACK_QUEEN) {
            long bot = startBB.getBlackQueensBottom() ^ endBB.getBlackQueensBottom();
            long top = startBB.getBlackQueensTop() ^ endBB.getBlackQueensTop();

            if (Long.bitCount(bot) == 2) {
                int changedPos1 = Long.numberOfTrailingZeros(Long.lowestOneBit(bot));
                int changedPos2 = Long.numberOfTrailingZeros(Long.highestOneBit(bot));

                if ((bot & (1L << changedPos1)) == 0)
                    return new int[]{changedPos1, changedPos2};
                else
                    return new int[]{changedPos2, changedPos1};
            } else if (Long.bitCount(top) == 2) {
                int changedPos1 = Long.numberOfTrailingZeros(Long.lowestOneBit(top));
                int changedPos2 = Long.numberOfTrailingZeros(Long.highestOneBit(top));

                if ((top & (1L << changedPos1)) == 0)
                    return new int[]{changedPos1 + 50, changedPos2 + 50};
                else
                    return new int[]{changedPos2 + 50, changedPos1 + 50};
            } else {
                int changedPos1 = Long.numberOfTrailingZeros(Long.lowestOneBit(bot));
                int changedPos2 = Long.numberOfTrailingZeros(Long.lowestOneBit(top));

                if ((bot & (1L << changedPos1)) == 0)
                    return new int[]{changedPos1, changedPos2 + 50};
                else
                    return new int[]{changedPos2 + 50, changedPos1};
            }
        } else if (player == State.WHITE_QUEEN) {
            long bot = startBB.getWhiteQueensBottom() ^ endBB.getWhiteQueensBottom();
            long top = startBB.getWhiteQueensTop() ^ endBB.getWhiteQueensTop();

            if (Long.bitCount(bot) == 2) {
                int changedPos1 = Long.numberOfTrailingZeros(Long.lowestOneBit(bot));
                int changedPos2 = Long.numberOfTrailingZeros(Long.highestOneBit(bot));

                if ((bot & (1L << changedPos1)) == 0)
                    return new int[]{changedPos1, changedPos2};
                else
                    return new int[]{changedPos2, changedPos1};
            } else if (Long.bitCount(top) == 2) {
                int changedPos1 = Long.numberOfTrailingZeros(Long.lowestOneBit(top));
                int changedPos2 = Long.numberOfTrailingZeros(Long.highestOneBit(top));

                if ((top & (1L << changedPos1)) == 0)
                    return new int[]{changedPos1 + 50, changedPos2 + 50};
                else
                    return new int[]{changedPos2 + 50, changedPos1 + 50};
            } else {
                int changedPos1 = Long.numberOfTrailingZeros(Long.lowestOneBit(bot));
                int changedPos2 = Long.numberOfTrailingZeros(Long.lowestOneBit(top));

                if ((bot & (1L << changedPos1)) == 0)
                    return new int[]{changedPos1, changedPos2 + 50};
                else
                    return new int[]{changedPos2 + 50, changedPos1};
            }
        } else throw new RuntimeException("WTF. Invalid player " + player);
    }

    public static int arrowPos(State start, State end) {
        long bot = start.getBitBoard().getArrowBottom() ^ end.getBitBoard().getArrowBottom();
        long top = start.getBitBoard().getArrowTop() ^ end.getBitBoard().getArrowTop();

        if (Long.bitCount(bot) == 2) {
            int changedPos1 = Long.numberOfTrailingZeros(Long.lowestOneBit(bot));
            int changedPos2 = Long.numberOfTrailingZeros(Long.highestOneBit(bot));

            if ((bot & (1L << changedPos1)) == 0)
                return changedPos2;
            else
                return changedPos1;
        } else if (Long.bitCount(top) == 2) {
            int changedPos1 = Long.numberOfTrailingZeros(Long.lowestOneBit(top));
            int changedPos2 = Long.numberOfTrailingZeros(Long.highestOneBit(top));

            if ((top & (1L << changedPos1)) == 0)
                return changedPos2 + 50;
            else
                return changedPos1 + 50;
        } else {
            int changedPos1 = Long.numberOfTrailingZeros(Long.lowestOneBit(bot));
            int changedPos2 = Long.numberOfTrailingZeros(Long.lowestOneBit(top));

            if ((bot & (1L << changedPos1)) == 0)
                return changedPos2 + 50;
            else
                return changedPos1;
        }
    }
}
