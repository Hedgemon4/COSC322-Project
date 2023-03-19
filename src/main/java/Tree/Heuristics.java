package Tree;

import State.*;

import java.util.ArrayList;
import java.util.Arrays;

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
    public static double bigPoppa(State s, int playerToMove) {
        int[][] D1 = D(1, s);
        int[][] D2 = D(2, s);

        double t1 = t(D1, playerToMove);
        double t2 = t(D2, playerToMove);

        double c1 = c(1, D1);
        double c2 = c(2, D2);

        double w = w(D1);

        System.out.println("w = " + w);

        double[] f = f(w);

        return f[0] * t1 + f[1] * c1 + f[2] * c2 + f[3] * t2;
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

