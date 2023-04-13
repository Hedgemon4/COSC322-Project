package ExternalGameProcessing;

import State.Action;
import State.State;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * This class is used to generate the training data for the neural network. It takes in a list of games and generates a list of states. Each entry is a pair of states where the first state is the state before the move and the second state is the state after the move.
 */
public class DataAugmentation {
    public static void main(String[] args) throws Exception {
        State openingBoard = new State(new ArrayList<>(Arrays.asList(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 2, 0, 0, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 2, 0, 0, 0, 0, 0, 0, 0, 0, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 1, 0, 0, 0)));

        try (
                ObjectWriter out = new ObjectWriter("bin/training/state_list/state_list.dat", 100000);
                BufferedReader in = new BufferedReader(new FileReader("bin/training/parsed_games.csv"))
        ) {
            // Read first line of file and discard it. It's just the header
            in.readLine();
            // For each line in the csv. Each row is a whole game. The first column is the winner of the game, the rest are moves of the game
            String row;
            int count = 0;
            while ((row = in.readLine()) != null) {
                String[] values = row.split(",");

                State state = (State) openingBoard.clone();

                // For each move
                for (int i = 1; i < values.length; i++) {
                    // Parse the action
                    Action move = parseMove(values[i]);

                    // Get player making move
                    int player = state.getPos(move.getOldX(), move.getOldY());
                    if (player != State.BLACK_QUEEN && player != State.WHITE_QUEEN) {
                        System.out.println("Error: " + player);
                        System.out.println(state.boardToString());
                        System.out.println(move);
                        System.out.println(values[i]);
                        System.out.println(row);
                        System.exit(1);
                    }

                    //////////////////////////////////////////
                    // Save states and their ideal response //
                    //////////////////////////////////////////

                    // Save the current state as the initial board
                    // Save this new state as the ideal action to make given the initial state
                    State idealState = new State(state, move);
                    out.writeDataPair(new Object[]{player, state, idealState});

                    // Only augment the data if it's not the first 5 moves of the game
                    if (i > 5) {
                        // Flip
                        out.writeDataPair(new Object[]{player, flipBoard(state), flipBoard(idealState)});
                        count++;

                        // Rotate 90
                        State rotated = rotateBoard(state);
                        State idealRotated = rotateBoard(idealState);
                        out.writeDataPair(new Object[]{player, rotated, idealRotated});
                        count++;

                        // Rotate 90, flip
                        out.writeDataPair(new Object[]{player, flipBoard(rotated), flipBoard(idealRotated)});
                        count++;

                        // Rotate 180
                        rotated = rotateBoard(rotated);
                        idealRotated = rotateBoard(idealRotated);
                        out.writeDataPair(new Object[]{player, rotated, idealRotated});
                        count++;

                        // Rotate 180, flip
                        out.writeDataPair(new Object[]{player, flipBoard(rotated), flipBoard(idealRotated)});
                        count++;

                        // Rotate 270
                        rotated = rotateBoard(rotated);
                        idealRotated = rotateBoard(idealRotated);
                        out.writeDataPair(new Object[]{player, rotated, idealRotated});
                        count++;

                        // Rotate 270, flip
                        out.writeDataPair(new Object[]{player, flipBoard(rotated), flipBoard(idealRotated)});
                        count++;
                    }

                    // Update the state with the move
                    state = new State(state, move);
                }
            }
            System.out.println(count);
        }
    }

    /**
     * Rotates the board 90 degrees clockwise
     *
     * @param board The board to rotate
     * @return The rotated board
     */
    private static State rotateBoard(State board) {
        ArrayList<Integer> newBoard = new ArrayList<>(Arrays.asList(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0));
        for (int y = 0; y < State.BOARD_SIZE; y++) {
            for (int x = 0; x < State.BOARD_SIZE; x++) {
                if (x == 0)
                    newBoard.add(0);
                newBoard.add(board.getPos(State.BOARD_SIZE - 1 - y, x));
            }
        }
        return new State(newBoard);
    }

    /**
     * Flips the board over the horizontal axis
     *
     * @param board The board to flip
     * @return The flipped board
     */
    private static State flipBoard(State board) {
        ArrayList<Integer> newBoard = new ArrayList<>(Arrays.asList(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0));
        for (int y = 0; y < State.BOARD_SIZE; y++) {
            for (int x = 0; x < State.BOARD_SIZE; x++) {
                if (x == 0)
                    newBoard.add(0);
                newBoard.add(board.getPos(x, State.BOARD_SIZE - 1 - y));
            }
        }
        return new State(newBoard);
    }

    /**
     * Parses a move from a string
     *
     * @param move The move to parse as a string in the format like "B[j4-f4/c7]"
     * @return An Action object representing the move
     */
    private static Action parseMove(String move) {
        move = move.substring(2, move.length() - 1);
        String[] parts = move.split("[-/]");
        int oldX = parts[0].charAt(0) - 'a';
        int oldY = Integer.parseInt(parts[0].substring(1)) - 1;
        int newX = parts[1].charAt(0) - 'a';
        int newY = Integer.parseInt(parts[1].substring(1)) - 1;
        int arrowX = parts[2].charAt(0) - 'a';
        int arrowY = Integer.parseInt(parts[2].substring(1)) - 1;

        return new Action(oldX, oldY, newX, newY, arrowX, arrowY);
    }
}
