package State;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;

public class ZobristHash {
    private static boolean initialized = false;
    private static long[] zobristTable;

    /**
     * Returns a hash of the given state. Is not guaranteed to be unique since there are 4^100 = 2^200 possible states, but only 2^64 possible longs, but it should be good enough for our purposes since Monte Carlo Tree Search is robust to a few miscalculations, if they even happen.
     * @param s The state to hash
     * @return The hash of the state
     */
    public static long hash(State s) {
        if (!initialized)
            init();

        byte[][] board = s.getBoard();

        long hash = 0;

        // XOR all the zobrist values for each piece on the board
        for (int x = 0; x < board.length; x++)
            for (int y = 0; y < board[x].length; y++)
                hash ^= zobristTable[x + y*State.BOARD_SIZE + board[x][y]*100];

        return hash;
    }

    private static void init() {
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream("bin/zobristTable.dat"))) {
            zobristTable = (long[]) in.readObject();
        } catch (FileNotFoundException e) {
            // File should be in the repo, but if it's not, please contact Justin
            throw new RuntimeException("Zobrist table not found. Please make sure you've cloned the repository correctly and that the file 'bin/zobristTable.dat' exists. If you don't have it and it's not on the repo, please contact Justin");

            // For reference only, this is how the zobrist table was generated. Do not uncomment this for any reason or consult Justin before doing so.
            /*File f = new File("bin/zobristTable.dat");
            if (!f.getParentFile().exists())
                f.getParentFile().mkdirs();
            try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream("bin/zobristTable.dat"))) {
                Random r = new Random();
                zobristTable = new long[State.BOARD_SIZE*State.BOARD_SIZE*4];
                for (int i = 0; i < zobristTable.length; i++)
                    zobristTable[i] = r.nextLong();

                out.writeObject(zobristTable);
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }*/

        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }

        initialized = true;
    }
}
