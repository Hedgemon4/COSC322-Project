package Tree;

import State.Action;
import State.ActionGenerator;
import State.State;
import State.BitBoard;

import java.util.ArrayList;
import java.util.PriorityQueue;

public class ExpansionPolicy {

    public static Node[] expansionNode(Node node, int numToExpand) {
        return bitBoardLibertyExpansionPolicy(node, numToExpand);
    }

    private static Node randomExpansion(Node node) {
        int randomInt = (int) (Math.random() * node.getPossibleActions().length);
        while (node.getChildren()[randomInt] != null) {
            randomInt = (int) (Math.random() * node.getPossibleActions().length);
        }
        Action randomAction = node.getPossibleActions()[randomInt];
        State state = new State(node.getState(), randomAction);
        int colour = node.getColour() == State.BLACK_QUEEN ? State.WHITE_QUEEN : State.BLACK_QUEEN;
        Action[] actions = ActionGenerator.generateActions(state, colour, node.getDepth()).toArray(new Action[0]);
        Node expansion = new Node(state, randomAction, node, colour, 0, 0, actions, node.getDepth() + 1);
        node.getChildren()[randomInt] = expansion;
        return expansion;
    }

    private static Node[] bitBoardLibertyExpansionPolicy(Node node, int numToExpand) {
        // For each action, we want to compute what the liberty is, and weight actions which put your liberty at risk
        // low, and actions which place the opponents liberty low as good
        BitBoard board = node.getState().getBitBoard();
        PriorityQueue<Object[]> topActions = new PriorityQueue<>((o1, o2) -> ((int) o2[1]) - ((int) o1[1]));

        int i = 0;
        for (Action action : node.getPossibleActions()) {
            // Don't expand if this action has already been expanded
            if (node.getChildren()[i] != null) {
                i++;
                continue;
            }

            // Calculate resulting state and liberty of moved piece
            int queenX = action.getOldX();
            int queenY = action.getOldY();
            State result = new State(node.getState(), action);
            int moveLibertyOld = calculateLiberty(queenX, queenY, board);
            int moveLibertyNew = calculateLiberty(action.getNewX(), action.getNewY(), result.getBitBoard());
            int actionWeight = 0;

            // Punish moving to the edge
            if (action.getNewX() == 0 || action.getNewX() == 9)
                actionWeight -= 50;
            if (action.getNewY() == 0 || action.getNewY() == 9)
                actionWeight -= 50;

            // Weight move to escape being trapped
            if (moveLibertyOld < 3 && moveLibertyNew > 2)
                actionWeight += 40;

            // Punish moving to a spot with less than 3 liberties
            if (moveLibertyNew < 3)
                actionWeight -= 20;

            // TODO: Prioritize Reducing enemy liberties


            // Add relevant data to the heap
            topActions.offer(new Object[]{i, actionWeight, result});
            i++;
        }

        ArrayList<Node> nodesToReturn = new ArrayList<>(numToExpand);
        int j = 0;
        while (j++ < numToExpand && topActions.size() > 0) {
            Object[] actionInfo = topActions.poll();

            int pickedActionIndex = (int) actionInfo[0];
            State state = (State) actionInfo[2];

            int colour = node.getColour() == State.BLACK_QUEEN ? State.WHITE_QUEEN : State.BLACK_QUEEN;
            Action[] actions = ActionGenerator.generateActions(state, colour, node.getDepth()).toArray(new Action[0]);
            Node expansion = new Node(state, node.getPossibleActions()[pickedActionIndex], node, colour, 0, 0, actions, node.getDepth() + 1);
            node.getChildren()[pickedActionIndex] = expansion;

            nodesToReturn.add(expansion);
        }

        return nodesToReturn.toArray(new Node[0]);
    }

    private static int calculateLiberty(int x, int y, BitBoard board) {
        // Combine all boards together such that it now represents if there is ANY piece on the board at a given index
        long spaceTop = 0L;
        long spaceBottom = 0L;
        spaceTop |= board.getArrowTop();
        spaceTop |= board.getBlackQueensTop();
        spaceTop |= board.getWhiteQueensTop();
        spaceBottom |= board.getArrowBottom();
        spaceBottom |= board.getWhiteQueensBottom();
        spaceBottom |= board.getBlackQueensBottom();

        long boardLibertiesTop = 0L;
        long boardLibertiesBottom = 0L;

        // Mask to only show squares adjacent to the specified square
        int mask = 0;
        if (x + 1 < 10) {
            // Right
            mask = (x + 1) + y * 10;
            if (mask > 49) {
                mask -= 50;
                // TODO: the subtraction here does not work due to the board orientation
                long k = 1L;
                k = k << mask;
                boardLibertiesTop |= ~(k & spaceTop);
            } else
                boardLibertiesBottom |= ~((1L << mask) & spaceBottom);
        }
        if (x - 1 > -1) {
            // Left
            mask = (x - 1) + y * 10;
            if (mask > 49) {
                mask -= 50;
                boardLibertiesTop |= ~((1L << mask) & spaceTop);
            } else
                boardLibertiesBottom |= ~((1L << mask) & spaceBottom);
        }
        if (y + 1 < 10) {
            // Up
            mask = x + 10 * (y + 1);
            if (mask > 49) {
                mask -= 50;
                boardLibertiesTop |= ~((1L << mask) & spaceTop);
            } else
                boardLibertiesBottom |= ~((1L << mask) & spaceBottom);
        }
        if (y - 1 > -1) {
            // Down
            mask = x + (y - 1) * 10;
            if (mask > 49) {
                mask -= 50;
                boardLibertiesTop |= ~((1L << mask) & spaceTop);
            } else
                boardLibertiesBottom |= ~((1L << mask) & spaceBottom);
        }
        if (x + 1 < 10 && y + 1 < 10) {
            // Up Right
            mask = x + 1 + (y + 1) * 10;
            if (mask > 49) {
                mask -= 50;
                boardLibertiesTop |= ~((1L << mask) & spaceTop);
            } else
                boardLibertiesBottom |= ~((1L << mask) & spaceBottom);
        }
        if (x + 1 < 10 && y - 1 > -1) {
            // Down Right
            mask = x + 1 + (y - 1) * 10;
            if (mask > 49) {
                mask -= 50;
                boardLibertiesTop |= ~((1L << mask) & spaceTop);
            } else
                boardLibertiesBottom |= ~((1L << mask) & spaceBottom);
        }
        if (x - 1 > -1 && y + 1 < 10) {
            // Up Left
            mask = (x - 1) + (y + 1) * 10;
            if (mask > 49) {
                mask -= 50;
                boardLibertiesTop |= ~((1L << mask) & spaceTop);
            } else
                boardLibertiesBottom |= ~((1L << mask) & spaceBottom);
        }
        if (x - 1 > -1 && y - 1 > -1) {
            // Down Left
            mask = (x - 1) + (y - 1) * 10;
            if (mask > 49) {
                mask -= 50;
                boardLibertiesTop |= ~((1L << mask) & spaceTop);
            } else
                boardLibertiesBottom |= ~((1L << mask) & spaceBottom);
        }

        // Sum the number of pieces surrounding the given position
        return Long.bitCount(boardLibertiesTop) + Long.bitCount(boardLibertiesBottom);
    }

    private static int calculateLiberties(int[][] queens, BitBoard bitBoard) {
        int liberty = 0;
        for (int[] queen : queens) {
            int x = queen[0];
            int y = queen[1];
            if (x + 1 < 10)
                liberty += bitBoard.getPiece(x + 1, y) == 0 ? 1 : 0;
            if (x - 1 > -1)
                liberty += bitBoard.getPiece(x - 1, y) == 0 ? 1 : 0;
            if (y + 1 < 10)
                liberty += bitBoard.getPiece(x, y + 1) == 0 ? 1 : 0;
            if (y - 1 > -1)
                liberty += bitBoard.getPiece(x, y - 1) == 0 ? 1 : 0;
            if (x + 1 < 10 && y + 1 < 10)
                liberty += bitBoard.getPiece(x + 1, y + 1) == 0 ? 1 : 0;
            if (x + 1 < 10 && y - 1 > -1)
                liberty += bitBoard.getPiece(x + 1, y - 1) == 0 ? 1 : 0;
            if (x - 1 > -1 && y + 1 < 10)
                liberty += bitBoard.getPiece(x - 1, y + 1) == 0 ? 1 : 0;
            if (x - 1 > -1 && y - 1 > -1)
                liberty += bitBoard.getPiece(x - 1, y - 1) == 0 ? 1 : 0;
        }
        return liberty;
    }

    private static int[][] calculateLiberties(int[][] queens, byte[][] board) {
        int[][] liberty = new int[10][10];
        for (int i = 0; i < queens.length; i++) {
            int x = queens[i][0];
            int y = queens[i][1];
            if (x + 1 < 10)
                liberty[x][y] += board[x + 1][y] == 0 ? 1 : 0;
            if (x - 1 > -1)
                liberty[x][y] += board[x - 1][y] == 0 ? 1 : 0;
            if (y + 1 < 10)
                liberty[x][y] += board[x][y + 1] == 0 ? 1 : 0;
            if (y - 1 > -1)
                liberty[x][y] += board[x][y - 1] == 0 ? 1 : 0;
            if (x + 1 < 10 && y + 1 < 10)
                liberty[x][y] += board[x + 1][y + 1] == 0 ? 1 : 0;
            if (x + 1 < 10 && y - 1 > -1)
                liberty[x][y] += board[x + 1][y - 1] == 0 ? 1 : 0;
            if (x - 1 > -1 && y + 1 < 10)
                liberty[x][y] += board[x - 1][y + 1] == 0 ? 1 : 0;
            if (x - 1 > -1 && y - 1 > -1)
                liberty[x][y] += board[x - 1][y - 1] == 0 ? 1 : 0;
        }
        return liberty;
    }
}
