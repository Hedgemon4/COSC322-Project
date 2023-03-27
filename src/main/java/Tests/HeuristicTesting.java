package Tests;

import State.*;
import Tree.Heuristics;

import java.sql.SQLOutput;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Random;

public class HeuristicTesting {
    public static void main(String[] args) {
        State state = new State(new ArrayList<>(Arrays.asList(
                0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                0, 0, 3, 0, 0, 0, 0, 0, 0, 0, 0,
                0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0,
                0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0
        )));

        State s = randomState();
        BitBoard b = s.getBitBoard();
        System.out.println(b.boardToString());
        long n = (long) 1e6;
        long start = System.nanoTime();
        for (long i = 0; i < n; i++) {
            Heuristics.bigPoppa(s,1);
        }
        long end = System.nanoTime();
        System.out.println("Time: " + (end - start)  / n + "ns");
        BitBoard testBoard = new BitBoard();

//        for (int y = 9; y >= 0; y--) {
//            for (int x = 0; x < 10; x++) {
//                if (result[0][y*10 + x] == 1000000) {
//                    System.out.print("- ");
//                } else {
//                    System.out.print(result[0][y*10+x] + " ");
//                }
//            }
//            System.out.println();
//        }

//        testBoard.setArrowTop(result.getWhiteQueensTop());
//        testBoard.setArrowBottom(result.getWhiteQueensBottom());
//        System.out.println(testBoard.boardToString());
    }

    private static int[][] D2(State s) {
        // Clone input so as not to modify it
        BitBoard input;
        try {
            input = (BitBoard) s.getBitBoard().clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }

        // Initialize reachable array. Indexed as [color][moveNum][top/bottom]
        long[][][] reachable = new long[2][10][2];
        BitBoard result;

        for (int nMoves = 0; nMoves < 10; nMoves++) {
            // Find all reachable squares
            result = kingReachableInOneMove(State.BLACK_QUEEN, input);

            reachable[0][nMoves][0] = result.getArrowTop();// result[0];
            reachable[0][nMoves][1] = result.getArrowBottom();// result[1];

            if (nMoves > 0) {
                if (result.getArrowTop() == 0L && result.getArrowBottom() == 0L) {
                    break;
                }
            }

            input.setArrowTop(input.getArrowTop() | input.getBlackQueensTop());
            input.setArrowBottom(input.getArrowBottom() | input.getBlackQueensBottom());
            input.setBlackQueensTop(result.getArrowTop());
            input.setBlackQueensBottom(result.getArrowBottom());
        }

        try {
            input = (BitBoard) s.getBitBoard().clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }

        for (int nMoves = 0; nMoves < 10; nMoves++) {
            // Find all reachable squares
            result = kingReachableInOneMove(State.WHITE_QUEEN, input);

            reachable[1][nMoves][0] = result.getArrowTop();// result[0];
            reachable[1][nMoves][1] = result.getArrowTop();// result[1];

            if (nMoves > 0) {
                if (result.getArrowTop() == 0L && result.getArrowBottom() == 0L) {
                    break;
                }
            }

            input.setArrowTop(input.getArrowTop() | input.getWhiteQueensTop());
            input.setArrowBottom(input.getArrowBottom() | input.getWhiteQueensBottom());
            input.setWhiteQueensTop(result.getArrowTop());
            input.setWhiteQueensBottom(result.getArrowBottom());
        }

        int[][] output = new int[2][100];

        for (int color = 0; color < 2; color++) {
            loop: for (int i = 0; i < 100; i++) {
                for (int nMoves = 0; nMoves < 10; nMoves++) {
                    if (i < 50 && (reachable[color][nMoves][1] & (1L << i)) != 0) {
                        output[color][i] = nMoves + 1;
                        continue loop;
                    } else if (i >= 50 && (reachable[color][nMoves][0] & (1L << (i - 50))) != 0) {
                        output[color][i] = nMoves + 1;
                        continue loop;
                    }
                }
                output[color][i] = 1000000;
            }
        }

        return output;
    }

    private static BitBoard kingReachableInOneMove(int color, BitBoard b) {
        long boardMask = -1L >>> (64 - 50);
        long aroundMask = 0b11100000001010000000111L;
        long notAFile = 0b11111111101111111110111111111011111111101111111110L;
        long notJFile = 0b01111111110111111111011111111101111111110111111111L;

        long blackTop = b.getBlackQueensTop();
        long blackBottom = b.getBlackQueensBottom();
        long whiteTop = b.getWhiteQueensTop();
        long whiteBottom = b.getWhiteQueensBottom();
        long arrowTop = b.getArrowTop();
        long arrowBottom = b.getArrowBottom();

        long occupiedTop = blackTop | whiteTop | arrowTop;
        long occupiedBottom = blackBottom | whiteBottom | arrowBottom;

        // All squares reachable in one move
        long reachableTop = 0L;
        long reachableBottom = 0L;

        int queenCount;
        long[] queensTop, queensBottom;

        if (color == State.BLACK_QUEEN) {
            // Find black queens
            queenCount = Long.bitCount(blackTop) + Long.bitCount(blackBottom);
            queensTop = new long[queenCount];
            queensBottom = new long[queenCount];
            for (int i = 0; i < queenCount; i++) {
                if (blackBottom > 1) {
                    queensBottom[i] = Long.lowestOneBit(blackBottom);
                    queensTop[i] = 0L;
                    blackBottom ^= queensBottom[i];
                } else {
                    queensBottom[i] = 0L;
                    queensTop[i] = Long.lowestOneBit(blackTop);
                    blackTop ^= queensTop[i];
                }
            }
        } else {
            // Find white queens
            queenCount = Long.bitCount(whiteTop) + Long.bitCount(whiteBottom);
            queensTop = new long[queenCount];
            queensBottom = new long[queenCount];
            for (int i = 0; i < queenCount; i++) {
                if (whiteBottom > 1) {
                    queensBottom[i] = Long.lowestOneBit(whiteBottom);
                    queensTop[i] = 0L;
                    whiteBottom ^= queensBottom[i];
                } else {
                    queensBottom[i] = 0L;
                    queensTop[i] = Long.lowestOneBit(whiteTop);
                    whiteTop ^= queensTop[i];
                }
            }
        }


        // Find all reachable squares
        for (int pieceNum = 0; pieceNum < queenCount; pieceNum++) {

            // The piece we are moving
            long startBottom = queensBottom[pieceNum];
            long startTop = queensTop[pieceNum];

            ///////////////////
            // Generate mask //
            ///////////////////

            int index;
            if (startTop > 0) {
                index = 50 + Long.numberOfTrailingZeros(startTop);
            } else {
                index = Long.numberOfTrailingZeros(startBottom);
            }

            long maskBottom, maskTop;
            if (index > 39) {
                if (index < 61)
                    maskTop = aroundMask >>> (61 - index);
                else
                    maskTop = aroundMask << (index - 61);
            } else {
                maskTop = 0L;
            }

            if (index < 61) {
                if (index < 11) {
                    maskBottom = aroundMask >>> (11 - index);
                } else {
                    maskBottom = aroundMask << (index - 11);
                }
            } else {
                maskBottom = 0L;
            }

            if (index % 10 == 0) {
                maskTop &= notJFile;
                maskBottom &= notJFile;
            } else if (index % 10 == 9) {
                maskTop &= notAFile;
                maskBottom &= notAFile;
            }

            maskTop &= boardMask;
            maskBottom &= boardMask;


            // Actually get squares we can move to
            reachableTop |= ~occupiedTop & maskTop;
            reachableBottom |= ~occupiedBottom & maskBottom;
        }

        BitBoard out = new BitBoard();
        out.setArrowTop(reachableTop);
        out.setArrowBottom(reachableBottom);

        return out;
    }

    /**
     * Returns the minimum number of moves required to reach each square from the given state.
     * @param b The state to start from
     * @return A 2D array of integers. The first index is the color, the second is the square.
     */
    private static int[][] minMoves(BitBoard b) {
        // Clone input so as not to modify it
        BitBoard input;
        try {
            input = (BitBoard) b.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }

        // Initialize reachable array. Indexed as [color][moveNum][top/bottom]
        long[][][] reachable = new long[2][10][2];
        BitBoard result;

        for (int nMoves = 0; nMoves < 10; nMoves++) {
            // Find all reachable squares
            result = reachableInOneMove(State.BLACK_QUEEN, input);

            reachable[0][nMoves][0] = result.getArrowTop();// result[0];
            reachable[0][nMoves][1] = result.getArrowBottom();// result[1];

            if (nMoves > 0) {
                if (result.getArrowTop() == 0L && result.getArrowBottom() == 0L) {
                    break;
                }
            }

            input.setArrowTop(input.getArrowTop() | input.getBlackQueensTop());
            input.setArrowBottom(input.getArrowBottom() | input.getBlackQueensBottom());
            input.setBlackQueensTop(result.getArrowTop());
            input.setBlackQueensBottom(result.getArrowBottom());
        }

        try {
            input = (BitBoard) b.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }

        for (int nMoves = 0; nMoves < 10; nMoves++) {
            // Find all reachable squares
            result = reachableInOneMove(State.WHITE_QUEEN, input);

            reachable[1][nMoves][0] = result.getArrowTop();// result[0];
            reachable[1][nMoves][1] = result.getArrowTop();// result[1];

            if (nMoves > 0) {
                if (result.getArrowTop() == 0L && result.getArrowBottom() == 0L) {
                    break;
                }
            }

            input.setArrowTop(input.getArrowTop() | input.getWhiteQueensTop());
            input.setArrowBottom(input.getArrowBottom() | input.getWhiteQueensBottom());
            input.setWhiteQueensTop(result.getArrowTop());
            input.setWhiteQueensBottom(result.getArrowBottom());
        }

        int[][] output = new int[2][100];

        for (int color = 0; color < 2; color++) {
            loop: for (int i = 0; i < 100; i++) {
                for (int nMoves = 0; nMoves < 10; nMoves++) {
                    if (i < 50 && (reachable[color][nMoves][1] & (1L << i)) != 0) {
                        output[color][i] = nMoves + 1;
                        continue loop;
                    } else if (i >= 50 && (reachable[color][nMoves][0] & (1L << (i - 50))) != 0) {
                        output[color][i] = nMoves + 1;
                        continue loop;
                    }
                }
                output[color][i] = 1000000;
            }
        }

        return output;
    }

    /**
     * Returns a bitboard with all the reachable positions in one move from the given board. Uses almost only bitwise operations, making it very fast.
     *
     * @param b The board to find reachable positions from.
     * @return A bitboard with all the reachable positions in one move from the given board for both colors.
     */
    private static BitBoard reachableInOneMove(int color, BitBoard b) {
        // Constants
        long columnMask = 0b00000000010000000001000000000100000000010000000001L;
        long rowMask = 0b1111111111L;
        long diagonalMask = 0b10000000000100000000001000000000010000000000100000000001L;
        long antiDiagonalMask = 0b000000001000000001000000001000000001000000001L;
        long boardMask = -1L >>> (64 - 50);

        long blackTop = b.getBlackQueensTop();
        long blackBottom = b.getBlackQueensBottom();
        long whiteTop = b.getWhiteQueensTop();
        long whiteBottom = b.getWhiteQueensBottom();
        long arrowTop = b.getArrowTop();
        long arrowBottom = b.getArrowBottom();

        long occupiedTop = blackTop | whiteTop | arrowTop;
        long occupiedBottom = blackBottom | whiteBottom | arrowBottom;

        // All squares reachable in one move
        long reachableTop = 0L;
        long reachableBottom = 0L;

        int queenCount;
        long[] queensTop, queensBottom;

        if (color == State.BLACK_QUEEN) {
            // Find black queens
            queenCount = Long.bitCount(blackTop) + Long.bitCount(blackBottom);
            queensTop = new long[queenCount];
            queensBottom = new long[queenCount];
            for (int i = 0; i < queenCount; i++) {
                if (blackBottom > 1) {
                    queensBottom[i] = Long.lowestOneBit(blackBottom);
                    queensTop[i] = 0L;
                    blackBottom ^= queensBottom[i];
                } else {
                    queensBottom[i] = 0L;
                    queensTop[i] = Long.lowestOneBit(blackTop);
                    blackTop ^= queensTop[i];
                }
            }
        } else {
            // Find white queens
            queenCount = Long.bitCount(whiteTop) + Long.bitCount(whiteBottom);
            queensTop = new long[queenCount];
            queensBottom = new long[queenCount];
            for (int i = 0; i < queenCount; i++) {
                if (whiteBottom > 1) {
                    queensBottom[i] = Long.lowestOneBit(whiteBottom);
                    queensTop[i] = 0L;
                    whiteBottom ^= queensBottom[i];
                } else {
                    queensBottom[i] = 0L;
                    queensTop[i] = Long.lowestOneBit(whiteTop);
                    whiteTop ^= queensTop[i];
                }
            }
        }

        for (int pieceNum = 0; pieceNum < queenCount; pieceNum++) {

            // The piece we are moving
            long startBottom = queensBottom[pieceNum];
            long startTop = queensTop[pieceNum];

            int startRow = getRow(startBottom, startTop);
            int startCol = getCol(startBottom, startTop);

            // All pieces except the one we are moving
            long currentOccupiedTop = occupiedTop ^ startTop;
            long currentOccupiedBottom = occupiedBottom ^ startBottom;


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
            long occupiedRowTop = currentOccupiedTop & rowMaskTop;
            long occupiedRowBottom = currentOccupiedBottom & rowMaskBottom;
            long occupiedColTop = currentOccupiedTop & colMaskTop;
            long occupiedColBottom = currentOccupiedBottom & colMaskBottom;
            long occupiedDiagTop = currentOccupiedTop & diagMaskTop;
            long occupiedDiagBottom = currentOccupiedBottom & diagMaskBottom;
            long occupiedAntiDiagTop = currentOccupiedTop & antiDiagMaskTop;
            long occupiedAntiDiagBottom = currentOccupiedBottom & antiDiagMaskBottom;


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
            reachableTop |= betweenColAboveTop | betweenColBelowTop | betweenRowAboveTop | betweenRowBelowTop | betweenDiagAboveTop | betweenDiagBelowTop | betweenAntiDiagAboveTop | betweenAntiDiagBelowTop;
            reachableBottom |= betweenColAboveBottom | betweenColBelowBottom | betweenRowAboveBottom | betweenRowBelowBottom | betweenDiagAboveBottom | betweenDiagBelowBottom | betweenAntiDiagAboveBottom | betweenAntiDiagBelowBottom;
        }

        // Return the reachable squares
        BitBoard out = new BitBoard();
        out.setArrowTop(reachableTop);
        out.setArrowBottom(reachableBottom);
        return out;
//        return new long[]{reachableTop, reachableBottom};
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

    public static State randomState() {
        Random r = new Random();
        ArrayList<Integer> board = new ArrayList<>(100);
        board.add(1);
        board.add(1);
        board.add(1);
        board.add(1);
        board.add(2);
        board.add(2);
        board.add(2);
        board.add(2);
        int numArrows = r.nextInt(30) + 10;
        for (int i = 0; i < numArrows; i++) {
            board.add(3);
        }
        while (board.size() < 100) {
            board.add(0);
        }
        Collections.shuffle(board);
        // Turn into an 11 by 11 arraylist
        ArrayList<Integer> newBoard = new ArrayList<>(121);
        for (int y = 0; y < 11; y++) {
            for (int x = 0; x < 11; x++) {
                if (x == 0 || y == 0) {
                    newBoard.add(0);
                    continue;
                }
                newBoard.add(board.get((y - 1) * 10 + x - 1));
            }
        }
        return new State(newBoard);
    }
}
