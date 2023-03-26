package State;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Random;

public class ZobristHash {
    private static boolean initialized = false;
    private static long[] zobristTable;
    private static long blackToMove;

    public static long zobristHash(BitBoard bitBoard, int colorToMove) {
        if (!initialized) init();

        long hash = 0;

        if (colorToMove == State.BLACK_QUEEN)
            hash = blackToMove;

        int index = 0; // Start at 0 for black
        long currentBoard = bitBoard.getBlackQueensBottom();
        while (currentBoard > 0) {
            if ((currentBoard & 1L) == 1)
                hash ^= zobristTable[index];
            currentBoard >>= 1;
            index += 3;
        }
        index = 150; // Make sure that the index is correct going into the next loop because the last loop likely didn't execute a full 50 times
        currentBoard = bitBoard.getBlackQueensTop();
        while (currentBoard > 0) {
            if ((currentBoard & 1L) == 1)
                hash ^= zobristTable[index];
            currentBoard >>= 1;
            index += 3;
        }

        index = 1; // Start at 1 for white
        currentBoard = bitBoard.getWhiteQueensBottom();
        while (currentBoard > 0) {
            if ((currentBoard & 1L) == 1)
                hash ^= zobristTable[index];
            currentBoard >>= 1;
            index += 3;
        }
        index = 151; // Make sure that the index is correct going into the next loop because the last loop likely didn't execute a full 50 times
        currentBoard = bitBoard.getWhiteQueensTop();
        while (currentBoard > 0) {
            if ((currentBoard & 1L) == 1)
                hash ^= zobristTable[index];
            currentBoard >>= 1;
            index += 3;
        }

        index = 2; // Start at 2 for arrows
        currentBoard = bitBoard.getArrowBottom();
        while (currentBoard > 0) {
            if ((currentBoard & 1L) == 1)
                hash ^= zobristTable[index];
            currentBoard >>= 1;
            index += 3;
        }
        index = 152; // Make sure that the index is correct going into the next loop because the last loop likely didn't execute a full 50 times
        currentBoard = bitBoard.getArrowTop();
        while (currentBoard > 0) {
            if ((currentBoard & 1L) == 1)
                hash ^= zobristTable[index];
            currentBoard >>= 1;
            index += 3;
        }

        return hash;
    }

    public static void init() {
        Path path = Paths.get("bin/zobristTable.dat");
        try (ObjectInputStream in = new ObjectInputStream(new BufferedInputStream(Files.newInputStream(path)))) {
            zobristTable = (long[]) in.readObject();
            blackToMove = in.readLong();
        } catch (Exception e) {
            // Generate table if it doesn't exist or there are any errors reading it

            // Addressed boardIndex * 3 + (pieceNumber - 1) // 0 = black, 1 = white, 2 = arrow
            // This does waste 25% of the space, but is more efficient for lookup
            zobristTable = new long[300];

            // Fill table with random longs
            HashSet<Long> randomLongs = new HashSet<>();
            Random r = new Random();
            for (int i = 0; i < zobristTable.length; i++) {
                    // Fill table with random longs making sure there are no duplicates (even though it's an extremely small chance)
                    long randLong = r.nextLong();
                    while (randomLongs.contains(randLong))
                        randLong = r.nextLong();
                    randomLongs.add(randLong);
                    zobristTable[i] = randLong;
            }

            // Also generate another bitstring for if black is the player that makes the next move
            blackToMove = r.nextLong();
            while (randomLongs.contains(blackToMove))
                blackToMove = r.nextLong();

            // Save to file
            try (ObjectOutputStream out = new ObjectOutputStream(Files.newOutputStream(path))) {
                out.writeObject(zobristTable);
                out.writeLong(blackToMove);
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }

        // Only set initialized to true if we make it here without an exception
        initialized = true;
    }
}
