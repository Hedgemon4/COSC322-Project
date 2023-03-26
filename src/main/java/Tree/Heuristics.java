package Tree;

import State.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

enum Direction {
    DR,
    D,
    DL,
    L,
    UL,
    U,
    UR,
    R,
    S
}

/**
 * Heuristics from <a href="https://www.google.com/url?sa=t&rct=j&q=&esrc=s&source=web&cd=&ved=2ahUKEwimybbW8OX9AhVYEjQIHbt2DDkQFnoECA0QAQ&url=https%3A%2F%2Fcore.ac.uk%2Fdownload%2Fpdf%2F81108035.pdf&usg=AOvVaw1-nBRvide0AaBBcjDESUE4">this paper</a>
 */
public class Heuristics {
    private static final HashMap<Long, Double> previouslyEvaluated = new HashMap<>();

    public static double bigPoppa(State s, int playerToMove) {
        long start = System.nanoTime();
        // Check to see if we have previously evaluated this state
        long hash = ZobristHash.zobristHash(s.getBitBoard(), playerToMove);
        Double heuristic = previouslyEvaluated.get(hash);
        if (heuristic != null)
            return heuristic;
        long end = System.nanoTime();
        System.out.println("Hashing took " + (end - start) + "ns");

        start = System.nanoTime();
        // Else evaluate it
        int[][] D1 = D(1, s);
        int[][] D2 = D(2, s);
        end = System.nanoTime();
        System.out.println("D took " + (end - start) + "ns");

        start = System.nanoTime();
        double t1 = t(D1, playerToMove);
        double t2 = t(D2, playerToMove);
        end = System.nanoTime();
        System.out.println("t took " + (end - start) + "ns");

        start = System.nanoTime();
        double c1 = c(1, D1);
        double c2 = c(2, D2);
        end = System.nanoTime();
        System.out.println("c took " + (end - start) + "ns");

        start = System.nanoTime();
        double w = w(D1);
        end = System.nanoTime();
        System.out.println("w took " + (end - start) + "ns");

        double[] f = f(w);

        heuristic = f[0] * t1 + f[1] * c1 + f[2] * c2 + f[3] * t2;

        start = System.nanoTime();
        previouslyEvaluated.put(hash, heuristic);
        end = System.nanoTime();
        System.out.println("put took " + (end - start) + "ns");

        // 87 is the estimated maximum value of the heuristic, so this just normalizes the output to be in the range of -1 to 1
        return heuristic;
    }

    private static double[] f(double w) {
        double[] f = new double[4];
        // f1 should be 1 when w is 0, and 0 when w is 80
        // f4 should be 1 when w is 80, and 0 when w is 0
        // f2 and f3 fill in the middle
        double a = 0.11, b = 1.18, c = 0.15, d = 80, g = 3.3;
        f[0] = f1(w, a);
        f[1] = Math.max(0, f1(b * (w - d/2 + d/(2*g)), a) - c);
        f[2] = Math.max(0, f1(b * (w - d/2 - d/(2*g)), a) - c);
        f[3] = f1(w - d, a);
        return f;
    }

    private static double f1(double w, double a) {
        return 4 * Math.exp(-a * w) / Math.pow(1 + Math.exp(-a * w), 2);
    }

    private static double w(int[][] D) {
        double w = 0;
        for (int a = 0; a < 100; a++)
            if (D[0][a] > 0)
                w += Math.pow(2, -Math.abs(D[0][a] - D[1][a]));
        return w;
    }

    private static double t(int[][] D, int nextPlayer) {
        double t = 0;
        for (int a = 0; a < 100; a++)
            if (D[0][a] > 0)
                t += delta(D[0][a], D[1][a], nextPlayer);

        return t;
    }

    private static double c(int mode, int[][] D) {
        double c = 0;
        if (mode == 1) {
            for (int a = 0; a < 100; a++)
                if (D[0][a] > 0)
                    c += Math.pow(2, -D[0][a]) - Math.pow(2, -D[1][a]);
        } else if (mode == 2) {
            for (int a = 0; a < 100; a++)
                if (D[0][a] > 0)
                    c += Math.min(1, Math.max(-1, (D[1][a] - D[0][a]) / 6.0));
        } else {
            throw new IllegalArgumentException("mode must 1 or 2");
        }

        return c;
    }

    /**
     * Returns what the paper describes as D <sub>mode</sub> for both players
     *
     * @param mode The distance mode <i>i</i>. 1 for queen distance, 2 for king distance
     * @param s The state to calculate the distances for
     * @return The distances for the whole board, for each player. D[0] = D <sub>i</sub><sup>1</sup>, D[1] = D <sub>i</sub><sup>2</sup>
     */
    @SuppressWarnings("OptionalGetWithoutIsPresent")
    private static int[][] D(int mode, State s) {
        BitBoard bitBoard = s.getBitBoard();

        int[] blackDistances = new int[100];
        int[] whiteDistances = new int[100];

        long blackTop = bitBoard.getBlackQueensTop();
        long blackBot = bitBoard.getBlackQueensBottom();
        long whiteTop = bitBoard.getWhiteQueensTop();
        long whiteBot = bitBoard.getWhiteQueensBottom();
        long arrowTop = bitBoard.getArrowTop();
        long arrowBot = bitBoard.getArrowBottom();

        for (int n = 0; n < 100; n++) {
            if (n < 50) {
                blackDistances[n] = (getBitAt(n, whiteBot) | getBitAt(n, arrowBot)) == 1 ? -1 : getBitAt(n, blackBot) == 1 ? 0 : 1000000;
                whiteDistances[n] = (getBitAt(n, blackBot) | getBitAt(n, arrowBot)) == 1 ? -1 : getBitAt(n, whiteBot) == 1 ? 0 : 1000000;
            } else {
                blackDistances[n] = (getBitAt(n - 50, whiteTop) | getBitAt(n - 50, arrowTop)) == 1 ? -1 : getBitAt(n - 50, blackTop) == 1 ? 0 : 1000000;
                whiteDistances[n] = (getBitAt(n - 50, blackTop) | getBitAt(n - 50, arrowTop)) == 1 ? -1 : getBitAt(n - 50, whiteTop) == 1 ? 0 : 1000000;
            }
        }

        if (mode == 1) { // Queen distance
            // Propagate black distances
            ArrayList<Direction>[] directions = new ArrayList[100];
            for (int n = 0; n < 100; n++) {
                directions[n] = new ArrayList<>();
            }
            // Set the directions of the queen squares to be stationary
            for (int n = 0; n < 100; n++) {
                if (blackDistances[n] == 0)
                    directions[n].add(Direction.S);
            }
            // Propagate the distances
            boolean changed;
            do {
                changed = false;
                for (int n = 0; n < 100; n++) {
                    int[] surroundingVals = getSurroundingValues(n, blackDistances);

                    for (int m = 0; m < surroundingVals.length; m++) {
                        if (surroundingVals[m] < 1000000) {
                            int offset;
                            if (m == 0)
                                offset = 9;
                            else if (m == 1)
                                offset = 10;
                            else if (m == 2)
                                offset = 11;
                            else if (m == 3)
                                offset = 1;
                            else if (m == 4)
                                offset = -9;
                            else if (m == 5)
                                offset = -10;
                            else if (m == 6)
                                offset = -11;
                            else
                                offset = -1;

                            Direction thisDir = Direction.values()[m];
                            ArrayList<Direction> dirThere = directions[n + offset];
                            if (!dirThere.contains(thisDir))
                                surroundingVals[m] += 1;
                        }
                    }

                    int min = surroundingVals[0];
                    ArrayList<Integer> minIndexes = new ArrayList<>();
                    minIndexes.add(0);
                    for (int m = 1; m < surroundingVals.length; m++) {
                        if (surroundingVals[m] < min) {
                            min = surroundingVals[m];
                            minIndexes.clear();
                            minIndexes.add(m);
                        } else if (surroundingVals[m] == min) {
                            minIndexes.add(m);
                        }
                    }

                    for (int minIndex : minIndexes) {
                        if (blackDistances[n] > min) {
                            changed = true;
                            blackDistances[n] = min;
                            directions[n].clear();
                            directions[n].add(Direction.values()[minIndex]);
                        } else if (blackDistances[n] == min && !directions[n].contains(Direction.values()[minIndex])) {
                            directions[n].add(Direction.values()[minIndex]);
                            changed = true;
                        }
                    }
                }
            } while (changed);

            // Propagate white distances
            directions = new ArrayList[100];
            for (int n = 0; n < 100; n++) {
                directions[n] = new ArrayList<>();
            }
            // Set the directions of the queen squares to be stationary
            for (int n = 0; n < 100; n++) {
                if (whiteDistances[n] == 0)
                    directions[n].add(Direction.S);
            }
            // Propagate the distances
            do {
                changed = false;
                for (int n = 0; n < 100; n++) {
                    int[] surroundingVals = getSurroundingValues(n, whiteDistances);

                    for (int m = 0; m < surroundingVals.length; m++) {
                        if (surroundingVals[m] < 1000000) {
                            int offset;
                            if (m == 0)
                                offset = 9;
                            else if (m == 1)
                                offset = 10;
                            else if (m == 2)
                                offset = 11;
                            else if (m == 3)
                                offset = 1;
                            else if (m == 4)
                                offset = -9;
                            else if (m == 5)
                                offset = -10;
                            else if (m == 6)
                                offset = -11;
                            else
                                offset = -1;

                            Direction thisDir = Direction.values()[m];
                            ArrayList<Direction> dirThere = directions[n + offset];
                            if (!dirThere.contains(thisDir))
                                surroundingVals[m] += 1;
                        }
                    }

                    int min = surroundingVals[0];
                    ArrayList<Integer> minIndexes = new ArrayList<>();
                    minIndexes.add(0);
                    for (int m = 1; m < surroundingVals.length; m++) {
                        if (surroundingVals[m] < min) {
                            min = surroundingVals[m];
                            minIndexes.clear();
                            minIndexes.add(m);
                        } else if (surroundingVals[m] == min) {
                            minIndexes.add(m);
                        }
                    }

                    for (int minIndex : minIndexes) {
                        if (whiteDistances[n] > min) {
                            changed = true;
                            whiteDistances[n] = min;
                            directions[n].clear();
                            directions[n].add(Direction.values()[minIndex]);
                        } else if (whiteDistances[n] == min && !directions[n].contains(Direction.values()[minIndex])) {
                            directions[n].add(Direction.values()[minIndex]);
                            changed = true;
                        }
                    }
                }
            } while (changed);
        } else if (mode == 2) { // King distance
            // Propagate black distances
            boolean changed;
            do {
                changed = false;
                for (int n = 0; n < 100; n++) {
                    int[] surroundingVals = getSurroundingValues(n, blackDistances);
                    int min = Arrays.stream(surroundingVals).min().getAsInt() + 1;
                    if (blackDistances[n] > min) {
                        changed = true;
                        blackDistances[n] = min;
                    }
                }
            } while (changed);

            // Propagate white distances
            do {
                changed = false;
                for (int n = 0; n < 100; n++) {
                    int[] surroundingVals = getSurroundingValues(n, whiteDistances);
                    int min = Arrays.stream(surroundingVals).min().getAsInt() + 1;
                    if (whiteDistances[n] > min) {
                        changed = true;
                        whiteDistances[n] = min;
                    }
                }
            } while (changed);
        } else {
            throw new IllegalArgumentException("mode must be 1 or 2");
        }

        return new int[][]{blackDistances, whiteDistances};
    }

    private static double delta(int blackDistance, int whiteDistance, int nextPlayer) {
        final double k = 0.2;
        if (blackDistance >= 1000000 && whiteDistance >= 1000000)
            return 0;
        else if (blackDistance == whiteDistance)
            return nextPlayer == State.BLACK_QUEEN ? k : -k;
        else if (blackDistance < whiteDistance)
            return 1;
        else
            return -1;
    }

    private static long getBitAt(int index, long bitString) {
        return (bitString >> index) & 1L;
    }

    private static int[] getSurroundingValues(int index, int[] distanceBoard) {
        int[] values = new int[8];
        for (int i = 0; i < 8; i++)
            values[i] = 1000000;

        // Up left
        if (index % 10 != 0 && index / 10 != 9) {
            int val = distanceBoard[index + 9];
            if (val != -1)
                values[0] = val;
        }
        // Up
        if (index / 10 != 9) {
            int val = distanceBoard[index + 10];
            if (val != -1)
                values[1] = val;
        }
        // Up Right
        if (index % 10 != 9 && index / 10 != 9) {
            int val = distanceBoard[index + 11];
            if (val != -1)
                values[2] = val;
        }
        // Right
        if (index % 10 != 9) {
            int val = distanceBoard[index + 1];
            if (val != -1)
                values[3] = val;
        }
        // Down right
        if (index / 10 != 0 && index % 10 != 9) {
            int val = distanceBoard[index - 9];
            if (val != -1)
                values[4] = val;
        }
        // Down
        if (index / 10 != 0) {
            int val = distanceBoard[index - 10];
            if (val != -1)
                values[5] = val;
        }
        // Down left
        if (index / 10 != 0 && index % 10 != 0) {
            int val = distanceBoard[index - 11];
            if (val != -1)
                values[6] = val;
        }
        // Left
        if (index % 10 != 0) {
            int val = distanceBoard[index - 1];
            if (val != -1)
                values[7] = val;
        }

        return values;
    }

    public static BitBoard reachableInOneMove(BitBoard b) {
        // Constants
        long columnMask = 0b00000000010000000001000000000100000000010000000001L;
        long rowMask = 0b1111111111L;
        long diagonalMask = 0b10000000000100000000001000000000010000000000100000000001L;
        long antiDiagonalMask = 0b000000001000000001000000001000000001000000001L;
        long boardMask = -1L >>> (64 - 50);

        long whiteTop = b.getWhiteQueensTop();
        long whiteBottom = b.getWhiteQueensBottom();
        long blackTop = b.getBlackQueensTop();
        long blackBottom = b.getBlackQueensBottom();
        long arrowTop = b.getArrowTop();
        long arrowBottom = b.getArrowBottom();

        // All squares reachable in one move
        long blackReachableTop = 0L;
        long blackReachableBottom = 0L;
        long whiteReachableTop = 0L;
        long whiteReachableBottom = 0L;

        // Find black queens
        long[] blackQueensTop = new long[4];
        long[] blackQueensBottom = new long[4];
        if (blackBottom > 1) {
            blackQueensBottom[0] = Long.lowestOneBit(blackBottom);
            blackQueensTop[0] = 0L;
            blackBottom ^= blackQueensBottom[0];
        } else {
            blackQueensBottom[0] = 0L;
            blackQueensTop[0] = Long.lowestOneBit(blackTop);
            blackTop ^= blackQueensTop[0];
        }
        if (blackBottom > 1) {
            blackQueensBottom[1] = Long.lowestOneBit(blackBottom);
            blackQueensTop[1] = 0L;
            blackBottom ^= blackQueensBottom[1];
        } else {
            blackQueensBottom[1] = 0L;
            blackQueensTop[1] = Long.lowestOneBit(blackTop);
            blackTop ^= blackQueensTop[1];
        }
        if (blackBottom > 1) {
            blackQueensBottom[2] = Long.lowestOneBit(blackBottom);
            blackQueensTop[2] = 0L;
            blackBottom ^= blackQueensBottom[2];
        } else {
            blackQueensBottom[2] = 0L;
            blackQueensTop[2] = Long.lowestOneBit(blackTop);
            blackTop ^= blackQueensTop[2];
        }
        if (blackBottom > 1) {
            blackQueensBottom[3] = Long.lowestOneBit(blackBottom);
            blackQueensTop[3] = 0L;
            blackBottom ^= blackQueensBottom[3];
        } else {
            blackQueensBottom[3] = 0L;
            blackQueensTop[3] = Long.lowestOneBit(blackTop);
            blackTop ^= blackQueensTop[3];
        }


        // Find white queens
        long[] whiteQueensTop = new long[4];
        long[] whiteQueensBottom = new long[4];
        if (whiteBottom > 1) {
            whiteQueensBottom[0] = Long.lowestOneBit(whiteBottom);
            whiteQueensTop[0] = 0L;
            whiteBottom ^= whiteQueensBottom[0];
        } else {
            whiteQueensBottom[0] = 0L;
            whiteQueensTop[0] = Long.lowestOneBit(whiteTop);
            whiteTop ^= whiteQueensTop[0];
        }
        if (whiteBottom > 1) {
            whiteQueensBottom[1] = Long.lowestOneBit(whiteBottom);
            whiteQueensTop[1] = 0L;
            whiteBottom ^= whiteQueensBottom[1];
        } else {
            whiteQueensBottom[1] = 0L;
            whiteQueensTop[1] = Long.lowestOneBit(whiteTop);
            whiteTop ^= whiteQueensTop[1];
        }
        if (whiteBottom > 1) {
            whiteQueensBottom[2] = Long.lowestOneBit(whiteBottom);
            whiteQueensTop[2] = 0L;
            whiteBottom ^= whiteQueensBottom[2];
        } else {
            whiteQueensBottom[2] = 0L;
            whiteQueensTop[2] = Long.lowestOneBit(whiteTop);
            whiteTop ^= whiteQueensTop[2];
        }
        if (whiteBottom > 1) {
            whiteQueensBottom[3] = Long.lowestOneBit(whiteBottom);
            whiteQueensTop[3] = 0L;
            whiteBottom ^= whiteQueensBottom[3];
        } else {
            whiteQueensBottom[3] = 0L;
            whiteQueensTop[3] = Long.lowestOneBit(whiteTop);
            whiteTop ^= whiteQueensTop[3];
        }

        for (int pieceNum = 0; pieceNum < 8; pieceNum++) {

            // The piece we are moving
            long startBottom, startTop;
            if (pieceNum < 4) {
                startBottom = blackQueensBottom[pieceNum];
                startTop = blackQueensTop[pieceNum];
            } else {
                startBottom = whiteQueensBottom[pieceNum - 4];
                startTop = whiteQueensTop[pieceNum - 4];
            }

            int startRow = getRow(startBottom, startTop);
            int startCol = getCol(startBottom, startTop);

            // All pieces except the one we are moving
            long occupiedTop = blackTop | whiteTop | arrowTop;
            long occupiedBottom = blackBottom | whiteBottom | arrowBottom;
            occupiedTop ^= startTop;
            occupiedBottom ^= startBottom;


            ///////////////////////////////////////////
            // Generate masks along the 4 directions //
            ///////////////////////////////////////////

            // Horizontal
            long rowMaskTop, rowMaskBottom;
            if (startRow < 5) {
                rowMaskTop = 0L;
                rowMaskBottom = rowMask << (startRow * 10);
            } else {
                rowMaskTop = rowMask << ((startRow - 5) * 10);
                rowMaskBottom = 0L;
            }

            // Vertical
            long colMaskTop = columnMask << startCol;
            long colMaskBottom = columnMask << startCol;

            // Diagonal
            long diagMaskTop, diagMaskBottom;
            int diagShift = startCol - startRow;
            if (diagShift >= 0) {
                if (diagShift >= 5) {
                    diagMaskTop = 0L;
                    diagMaskBottom = (diagonalMask << diagShift) & ~(-1L << ((10 - diagShift) * 10));
                } else {
                    diagMaskTop = (diagonalMask << (diagShift + 5)) & ~(-1L << ((5 - diagShift) * 10));
                    diagMaskBottom = diagonalMask << diagShift;
                }
            } else {
                if (diagShift < -5) {
                    diagMaskTop = (diagonalMask >>> (-diagShift + 6)) & (-1L << ((-diagShift - 5) * 10));
                    diagMaskBottom = 0L;
                } else {
                    diagMaskTop = diagonalMask >>> (-diagShift + 6);
                    diagMaskBottom = (diagonalMask >>> (-diagShift)) & (-1L << (-diagShift * 10));
                }
            }

            long antiDiagMaskTop, antiDiagMaskBottom;
            int antiDiagShift = startCol + startRow;
            if (antiDiagShift >= 9) {
                if (antiDiagShift > 13) {
                    antiDiagMaskTop = (antiDiagonalMask << (antiDiagShift - 5)) & (-1L << ((antiDiagShift - 14) * 10 + 1));
                    antiDiagMaskBottom = 0L;
                } else {
                    antiDiagMaskTop = (antiDiagonalMask << (antiDiagShift - 5));
                    antiDiagMaskBottom = (antiDiagonalMask << (antiDiagShift)) & (-1L << ((antiDiagShift - 9) * 10 + 1));
                }
            } else {
                if (antiDiagShift < 4) {
                    antiDiagMaskTop = 0L;
                    antiDiagMaskBottom = (antiDiagonalMask << (antiDiagShift)) & ~(-1L << ((antiDiagShift + 1) * 10 - 1));
                } else {
                    antiDiagMaskTop = (antiDiagonalMask << (antiDiagShift - 5)) & ~(-1L << ((antiDiagShift - 4) * 10 - 1));
                    antiDiagMaskBottom = (antiDiagonalMask << (antiDiagShift));
                }
            }

            // Make sure that all masks are only on the board
            rowMaskTop &= boardMask;
            rowMaskBottom &= boardMask;
            colMaskTop &= boardMask;
            colMaskBottom &= boardMask;
            diagMaskTop &= boardMask;
            diagMaskBottom &= boardMask;
            antiDiagMaskTop &= boardMask;
            antiDiagMaskBottom &= boardMask;


            ////////////////////////
            //     Get pieces     //
            ////////////////////////

            // Get the possible pieces below the current piece
            long piecesBelowBottom, piecesBelowTop;
            piecesBelowBottom = startBottom - 1;
            if (startTop == 0)
                piecesBelowTop = 0L;
            else
                piecesBelowTop = startTop - 1;

            // Get the possible pieces above the current piece
            long piecesAboveBottom = ~startBottom ^ piecesBelowBottom;
            long piecesAboveTop = ~startTop ^ piecesBelowTop;


            // Get the pieces that are along the path of the current piece in each direction
            long occupiedRowTop = occupiedTop & rowMaskTop;
            long occupiedRowBottom = occupiedBottom & rowMaskBottom;
            long occupiedColTop = occupiedTop & colMaskTop;
            long occupiedColBottom = occupiedBottom & colMaskBottom;
            long occupiedDiagTop = occupiedTop & diagMaskTop;
            long occupiedDiagBottom = occupiedBottom & diagMaskBottom;
            long occupiedAntiDiagTop = occupiedTop & antiDiagMaskTop;
            long occupiedAntiDiagBottom = occupiedBottom & antiDiagMaskBottom;


            ////////////////////////
            // Get blocking piece //
            ////////////////////////

            // Get the first blocking piece in col above the current piece
            long blockingPieceColAboveBottom, blockingPieceColAboveTop;
            if (startTop != 0) {
                blockingPieceColAboveBottom = 0L;
                blockingPieceColAboveTop = Long.lowestOneBit(occupiedColTop & piecesAboveTop);
            } else {
                blockingPieceColAboveBottom = Long.lowestOneBit(occupiedColBottom & piecesAboveBottom);
                if (blockingPieceColAboveBottom == 0)
                    blockingPieceColAboveTop = Long.lowestOneBit(occupiedColTop & piecesAboveTop);
                else
                    blockingPieceColAboveTop = 0L;
            }

            // Get the first blocking piece in col below the current piece
            long blockingPieceColBelowBottom, blockingPieceColBelowTop;
            if (startTop == 0) {
                blockingPieceColBelowTop = 0L;
                blockingPieceColBelowBottom = Long.highestOneBit(occupiedColBottom & piecesBelowBottom);
            } else {
                blockingPieceColBelowTop = Long.highestOneBit(occupiedColTop & piecesBelowTop);
                if (blockingPieceColBelowTop == 0)
                    blockingPieceColBelowBottom = Long.highestOneBit(occupiedColBottom & piecesBelowBottom);
                else
                    blockingPieceColBelowBottom = 0L;
            }

            // Get the first blocking piece in row above the current piece
            long blockingPieceRowAboveBottom, blockingPieceRowAboveTop;
            if (startTop != 0) {
                blockingPieceRowAboveBottom = 0L;
                blockingPieceRowAboveTop = Long.lowestOneBit(occupiedRowTop & piecesAboveTop);
            } else {
                blockingPieceRowAboveBottom = Long.lowestOneBit(occupiedRowBottom & piecesAboveBottom);
                blockingPieceRowAboveTop = 0L;
            }

            // Get the first blocking piece in row below the current piece
            long blockingPieceRowBelowBottom, blockingPieceRowBelowTop;
            if (startTop == 0) {
                blockingPieceRowBelowTop = 0L;
                blockingPieceRowBelowBottom = Long.highestOneBit(occupiedRowBottom & piecesBelowBottom);
            } else {
                blockingPieceRowBelowTop = Long.highestOneBit(occupiedRowTop & piecesBelowTop);
                blockingPieceRowBelowBottom = 0L;
            }

            // Get the first blocking piece in diag above the current piece
            long blockingPieceDiagAboveBottom, blockingPieceDiagAboveTop;
            if (startTop != 0) {
                blockingPieceDiagAboveBottom = 0L;
                blockingPieceDiagAboveTop = Long.lowestOneBit(occupiedDiagTop & piecesAboveTop);
            } else {
                blockingPieceDiagAboveBottom = Long.lowestOneBit(occupiedDiagBottom & piecesAboveBottom);
                if (blockingPieceDiagAboveBottom == 0)
                    blockingPieceDiagAboveTop = Long.lowestOneBit(occupiedDiagTop & piecesAboveTop);
                else
                    blockingPieceDiagAboveTop = 0L;
            }

            // Get the first blocking piece in diag below the current piece
            long blockingPieceDiagBelowBottom, blockingPieceDiagBelowTop;
            if (startTop == 0) {
                blockingPieceDiagBelowTop = 0L;
                blockingPieceDiagBelowBottom = Long.highestOneBit(occupiedDiagBottom & piecesBelowBottom);
            } else {
                blockingPieceDiagBelowTop = Long.highestOneBit(occupiedDiagTop & piecesBelowTop);
                if (blockingPieceDiagBelowTop == 0)
                    blockingPieceDiagBelowBottom = Long.highestOneBit(occupiedDiagBottom & piecesBelowBottom);
                else
                    blockingPieceDiagBelowBottom = 0L;
            }

            // Get the first blocking piece in anti-diag above the current piece
            long blockingPieceAntiDiagAboveBottom, blockingPieceAntiDiagAboveTop;
            if (startTop != 0) {
                blockingPieceAntiDiagAboveBottom = 0L;
                blockingPieceAntiDiagAboveTop = Long.lowestOneBit(occupiedAntiDiagTop & piecesAboveTop);
            } else {
                blockingPieceAntiDiagAboveBottom = Long.lowestOneBit(occupiedAntiDiagBottom & piecesAboveBottom);
                if (blockingPieceAntiDiagAboveBottom == 0)
                    blockingPieceAntiDiagAboveTop = Long.lowestOneBit(occupiedAntiDiagTop & piecesAboveTop);
                else
                    blockingPieceAntiDiagAboveTop = 0L;
            }

            // Get the first blocking piece in anti-diag below the current piece
            long blockingPieceAntiDiagBelowBottom, blockingPieceAntiDiagBelowTop;
            if (startTop == 0) {
                blockingPieceAntiDiagBelowTop = 0L;
                blockingPieceAntiDiagBelowBottom = Long.highestOneBit(occupiedAntiDiagBottom & piecesBelowBottom);
            } else {
                blockingPieceAntiDiagBelowTop = Long.highestOneBit(occupiedAntiDiagTop & piecesBelowTop);
                if (blockingPieceAntiDiagBelowTop == 0)
                    blockingPieceAntiDiagBelowBottom = Long.highestOneBit(occupiedAntiDiagBottom & piecesBelowBottom);
                else
                    blockingPieceAntiDiagBelowBottom = 0L;
            }


            ///////////////////////////
            // Get pieces in between //
            ///////////////////////////

            // Get squares movable to in col above the current piece
            long betweenColAboveBottom, betweenColAboveTop;
            if (startTop != 0) {
                betweenColAboveBottom = 0L;
                if (blockingPieceColAboveTop == 0)
                    betweenColAboveTop = piecesAboveTop & colMaskTop;
                else
                    betweenColAboveTop = piecesAboveTop & (blockingPieceColAboveTop - 1) & colMaskTop;
            } else {
                if (blockingPieceColAboveBottom == 0) {
                    betweenColAboveBottom = piecesAboveBottom & colMaskBottom;
                    betweenColAboveTop = (blockingPieceColAboveTop - 1) & colMaskTop;
                } else {
                    betweenColAboveBottom = piecesAboveBottom & (blockingPieceColAboveBottom - 1) & colMaskBottom;
                    betweenColAboveTop = 0L;
                }
            }

            // Get squares movable to in col below the current piece
            long betweenColBelowBottom, betweenColBelowTop;
            if (startTop == 0) {
                betweenColBelowTop = 0L;
                if (blockingPieceColBelowBottom == 0)
                    betweenColBelowBottom = piecesBelowBottom & colMaskBottom;
                else
                    betweenColBelowBottom = piecesBelowBottom & (-blockingPieceColBelowBottom ^ blockingPieceColBelowBottom) & colMaskBottom;
            } else {
                if (blockingPieceColBelowTop == 0) {
                    betweenColBelowTop = piecesBelowTop & colMaskTop;
                    if (blockingPieceColBelowBottom == 0)
                        betweenColBelowBottom = piecesBelowBottom & colMaskBottom;
                    else
                        betweenColBelowBottom = (-blockingPieceColBelowBottom ^ blockingPieceColBelowBottom) & colMaskBottom;
                } else {
                    betweenColBelowTop = piecesBelowTop & (-blockingPieceColBelowTop ^ blockingPieceColBelowTop) & colMaskTop;
                    betweenColBelowBottom = 0L;
                }
            }

            // Get squares movable to in row above the current piece
            long betweenRowAboveBottom, betweenRowAboveTop;
            if (startTop != 0) {
                betweenRowAboveBottom = 0L;
                if (blockingPieceRowAboveTop == 0)
                    betweenRowAboveTop = piecesAboveTop & rowMaskTop;
                else
                    betweenRowAboveTop = piecesAboveTop & (blockingPieceRowAboveTop - 1) & rowMaskTop;
            } else {
                if (blockingPieceRowAboveBottom == 0) {
                    betweenRowAboveBottom = piecesAboveBottom & rowMaskBottom;
                } else {
                    betweenRowAboveBottom = piecesAboveBottom & (blockingPieceRowAboveBottom - 1) & rowMaskBottom;
                }
                betweenRowAboveTop = 0L;
            }

            // Get squares movable to in row below the current piece
            long betweenRowBelowBottom, betweenRowBelowTop;
            if (startTop == 0) {
                betweenRowBelowTop = 0L;
                if (blockingPieceRowBelowBottom == 0)
                    betweenRowBelowBottom = piecesBelowBottom & rowMaskBottom;
                else
                    betweenRowBelowBottom = piecesBelowBottom & (-blockingPieceRowBelowBottom ^ blockingPieceRowBelowBottom) & rowMaskBottom;
            } else {
                if (blockingPieceRowBelowTop == 0) {
                    betweenRowBelowTop = piecesBelowTop & rowMaskTop;
                } else {
                    betweenRowBelowTop = piecesBelowTop & (-blockingPieceRowBelowTop ^ blockingPieceRowBelowTop) & rowMaskTop;
                }
                betweenRowBelowBottom = 0L;
            }

            // Get squares movable to in diag above the current piece
            long betweenDiagAboveBottom, betweenDiagAboveTop;
            if (startTop != 0) {
                betweenDiagAboveBottom = 0L;
                if (blockingPieceDiagAboveTop == 0)
                    betweenDiagAboveTop = piecesAboveTop & diagMaskTop;
                else
                    betweenDiagAboveTop = piecesAboveTop & (blockingPieceDiagAboveTop - 1) & diagMaskTop;
            } else {
                if (blockingPieceDiagAboveBottom == 0) {
                    betweenDiagAboveBottom = piecesAboveBottom & diagMaskBottom;
                    betweenDiagAboveTop = (blockingPieceDiagAboveTop - 1) & diagMaskTop;
                } else {
                    betweenDiagAboveBottom = piecesAboveBottom & (blockingPieceDiagAboveBottom - 1) & diagMaskBottom;
                    betweenDiagAboveTop = 0L;
                }
            }

            // Get squares movable to in diag below the current piece
            long betweenDiagBelowBottom, betweenDiagBelowTop;
            if (startTop == 0) {
                betweenDiagBelowTop = 0L;
                if (blockingPieceDiagBelowBottom == 0)
                    betweenDiagBelowBottom = piecesBelowBottom & diagMaskBottom;
                else
                    betweenDiagBelowBottom = piecesBelowBottom & (-blockingPieceDiagBelowBottom ^ blockingPieceDiagBelowBottom) & diagMaskBottom;
            } else {
                if (blockingPieceDiagBelowTop == 0) {
                    betweenDiagBelowTop = piecesBelowTop & diagMaskTop;
                    if (blockingPieceDiagBelowBottom == 0)
                        betweenDiagBelowBottom = piecesBelowBottom & diagMaskBottom;
                    else
                        betweenDiagBelowBottom = (-blockingPieceDiagBelowBottom ^ blockingPieceDiagBelowBottom) & diagMaskBottom;
                } else {
                    betweenDiagBelowTop = piecesBelowTop & (-blockingPieceDiagBelowTop ^ blockingPieceDiagBelowTop) & diagMaskTop;
                    betweenDiagBelowBottom = 0L;
                }
            }

            // Get squares movable to in anti-diag above the current piece
            long betweenAntiDiagAboveBottom, betweenAntiDiagAboveTop;
            if (startTop != 0) {
                betweenAntiDiagAboveBottom = 0L;
                if (blockingPieceAntiDiagAboveTop == 0)
                    betweenAntiDiagAboveTop = piecesAboveTop & antiDiagMaskTop;
                else
                    betweenAntiDiagAboveTop = piecesAboveTop & (blockingPieceAntiDiagAboveTop - 1) & antiDiagMaskTop;
            } else {
                if (blockingPieceAntiDiagAboveBottom == 0) {
                    betweenAntiDiagAboveBottom = piecesAboveBottom & antiDiagMaskBottom;
                    betweenAntiDiagAboveTop = (blockingPieceAntiDiagAboveTop - 1) & antiDiagMaskTop;
                } else {
                    betweenAntiDiagAboveBottom = piecesAboveBottom & (blockingPieceAntiDiagAboveBottom - 1) & antiDiagMaskBottom;
                    betweenAntiDiagAboveTop = 0L;
                }
            }

            // Get squares movable to in anti-diag below the current piece
            long betweenAntiDiagBelowBottom, betweenAntiDiagBelowTop;
            if (startTop == 0) {
                betweenAntiDiagBelowTop = 0L;
                if (blockingPieceAntiDiagBelowBottom == 0)
                    betweenAntiDiagBelowBottom = piecesBelowBottom & antiDiagMaskBottom;
                else
                    betweenAntiDiagBelowBottom = piecesBelowBottom & (-blockingPieceAntiDiagBelowBottom ^ blockingPieceAntiDiagBelowBottom) & antiDiagMaskBottom;
            } else {
                if (blockingPieceAntiDiagBelowTop == 0) {
                    betweenAntiDiagBelowTop = piecesBelowTop & antiDiagMaskTop;
                    if (blockingPieceAntiDiagBelowBottom == 0)
                        betweenAntiDiagBelowBottom = piecesBelowBottom & antiDiagMaskBottom;
                    else
                        betweenAntiDiagBelowBottom = (-blockingPieceAntiDiagBelowBottom ^ blockingPieceAntiDiagBelowBottom) & antiDiagMaskBottom;
                } else {
                    betweenAntiDiagBelowTop = piecesBelowTop & (-blockingPieceAntiDiagBelowTop ^ blockingPieceAntiDiagBelowTop) & antiDiagMaskTop;
                    betweenAntiDiagBelowBottom = 0L;
                }
            }

            // Or results onto the total reachable squares in pne move
            long reachableTop = betweenColAboveTop | betweenColBelowTop | betweenRowAboveTop | betweenRowBelowTop | betweenDiagAboveTop | betweenDiagBelowTop | betweenAntiDiagAboveTop | betweenAntiDiagBelowTop;
            long reachableBottom = betweenColAboveBottom | betweenColBelowBottom | betweenRowAboveBottom | betweenRowBelowBottom | betweenDiagAboveBottom | betweenDiagBelowBottom | betweenAntiDiagAboveBottom | betweenAntiDiagBelowBottom;
            if (pieceNum < 4) {
                blackReachableTop |= reachableTop;
                blackReachableBottom |= reachableBottom;
            } else {
                whiteReachableTop |= reachableTop;
                whiteReachableBottom |= reachableBottom;
            }
        }

        // Put the squares that are reachable by both players in one move into a BitBoard for return
        BitBoard reachable = new BitBoard();
        reachable.setBlackQueensBottom(blackReachableBottom);
        reachable.setBlackQueensTop(blackReachableTop);
        reachable.setWhiteQueensBottom(whiteReachableBottom);
        reachable.setWhiteQueensTop(whiteReachableTop);

        return reachable;
    }

    private static int getCol(long startBottom, long startTop) {
        if (startBottom == 0)
            return Long.bitCount(startTop - 1) % 10;
        else
            return Long.bitCount(startBottom - 1) % 10;
    }

    private static int getRow(long startBottom, long startTop) {
        if (startBottom == 0)
            return Long.bitCount(startTop - 1) / 10 + 5;
        else
            return Long.bitCount(startBottom - 1) / 10;
    }

    public static int calculateTileControl(int x, int y, BitBoard board) {
        // this is the number of tiles you can move to from this position
        int moves = 0;

        // Calculate filled tiles
        long spaceTop = 0L;
        long spaceBottom = 0L;
        spaceTop |= board.getArrowTop();
        spaceTop |= board.getBlackQueensTop();
        spaceTop |= board.getWhiteQueensTop();
        spaceBottom |= board.getArrowBottom();
        spaceBottom |= board.getWhiteQueensBottom();
        spaceBottom |= board.getBlackQueensBottom();

        long potentialMovesBottom = 0L;
        long potentialMovesTop = 0L;

        // In each direction, need to figure out moves for top and bottom
        int index = x + y * 10;
        boolean top = false;
        if (index > 49) {
            index -= 50;
            top = true;
        }

        long potentialMovesLeft = 0B111111111;
        long potentialMovesRight = 0B111111111;
        long potentialMovesUpTop = 0B11111;
        long potentialMovesUpBottom = 0B1111;
        long potentialMovesDownTop = 0B11111;
        long potentialMovesDownBottom = 0B1111;
        long potentialMovesUpRightTop = 0B01000000000010000000000100000000001000000000010000000000L;
        long potentialMovesUpRightBottom = 0B010000000000100000000001000000000010000000000L;
        long potentialMovesUpLeftTop = 0B0100000000100000000100000000100000000100000000L;
        long potentialMovesUpLeftBottom = 0B0100000000100000000100000000100000000L;
        long potentialMovesDownRightTop = 0B010000000000100000000001000000000010000000000L;
        long potentialMovesDownRightBottom = 0B01000000000010000000000100000000001000000000010000000000L;
        long potentialMovesDownLeftTop = 0B0100000000100000000100000000100000000100000000L;
        long potentialMovesDownLeftBottom = 0B0100000000100000000100000000100000000L;

        /*
            The strategy is to find the least significant bit set to one, subtract one, and then sum the number
            of ones

            Formula:
                - Get all 1 bits after least significant one (n & ~(n - 1)) - 1
                - AND with mask again
         */

        // Right and Up are least significant bit
        // Down and left are the most significant bit

        // Compute Moves Left

        long mask = potentialMovesLeft >> (9 - x);
        long left = mask << (index - x);
        left &= top ? spaceTop : spaceBottom;
        if (left != 0) {
            int k = (int) Math.floor(Math.log(left) / Math.log(2)) + 1;
            moves += x - k - (y * 10);
        } else {
            moves += x;
        }

        // Compute Moves Right

        mask = potentialMovesRight >> x;
        long right = mask << index + 1;
        right &= top ? spaceTop : spaceBottom;
        if (right != 0) {
            right &= ~(right - 1);
            right -= 1;
            right = right >> x + 1;
            moves += Long.bitCount(right & mask);
        } else {
            moves += 9 - x;
        }

        // Compute Moves Up

        return moves;
    }
}

