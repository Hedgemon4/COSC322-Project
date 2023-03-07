package Tree;

import State.Action;
import State.ActionGenerator;
import State.State;
import State.BitBoard;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.PriorityQueue;

public class ExpansionPolicy {
    private static final int shiftUp = 10;
    private static final int shiftRight = 1;

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
        int[][] nodeQueens = node.getState().getQueens(node.getColour());
        BitBoard board = node.getState().getBitBoard();
        int[] nodeLiberty = new int[4];
        for (int i = 0; i < 4; i++)
            nodeLiberty[i] = calculateLiberty(nodeQueens[i][0], nodeQueens[i][1], board);
        System.out.println(Arrays.toString(nodeLiberty));
        return null;
    }

//    private static Node[] libertyExpansionPolicy(Node node, int numToExpand) {
//        // Want to prioritize moving queens which might get trapped
//        int[][] nodeQueens = node.getState().getQueens(node.getColour());
//        byte[][] board = node.getState().getBoard();
//        int[][] nodeLiberty = calculateLiberties(nodeQueens, board);
//        // Prioritize trapping enemy queens
//        int otherColour = node.getColour() == State.BLACK_QUEEN ? State.WHITE_QUEEN : State.BLACK_QUEEN;
//        int[][] otherQueens = node.getState().getQueens(otherColour);
//        // Don't make moves if trapped
//
//        // Store each potential node in this max-heap for easily finding the top nodes
//        PriorityQueue<Object[]> topActions = new PriorityQueue<>((o1, o2) -> ((int) o2[1]) - ((int) o1[1]));
//
//        int i = 0;
//        for (Action action : node.getPossibleActions()) {
//            // Don't expand if this action has already been expanded
//            if (node.getChildren()[i] != null) {
//                i++;
//                continue;
//            }
//
//            int actionWeight = 0;
//
//            int queenX = action.getOldX();
//            int queenY = action.getOldY();
//
//            // Punish moving to the edge
//            if (action.getNewX() == 0 || action.getNewX() == 9)
//                actionWeight -= 50;
//            if (action.getNewY() == 0 || action.getNewY() == 9)
//                actionWeight -= 50;
//
//            // Punish moving to a spot with less than 3 liberties
//            if (nodeLiberty[queenX][queenY] != 0) {
//                if (nodeLiberty[queenX][queenY] < 3)
//                    actionWeight += 20;
//            }
//            State result = new State(node.getState(), action);
//            int[][] resultingLiberty = calculateLiberties(otherQueens, result.getBoard());
//            if (resultingLiberty[otherQueens[0][0]][otherQueens[0][1]] < 3) {
//                actionWeight += 10;
//            }
//            if (resultingLiberty[otherQueens[1][0]][otherQueens[1][1]] < 3) {
//                actionWeight += 10;
//            }
//            if (resultingLiberty[otherQueens[2][0]][otherQueens[2][1]] < 3) {
//                actionWeight += 10;
//            }
//            if (resultingLiberty[otherQueens[3][0]][otherQueens[3][1]] < 3) {
//                actionWeight += 10;
//            }
//
//            // Add relevant data to the heap
//            topActions.offer(new Object[]{i, actionWeight, result});
//            i++;
//        }
//
//        // Find top nodes to expand (or all if there are less than numToExpand)
//        ArrayList<Node> nodesToReturn = new ArrayList<>(numToExpand);
//        int j = 0;
//        while (j++ < numToExpand && topActions.size() > 0) {
//            Object[] actionInfo = topActions.poll();
//
//            int pickedActionIndex = (int) actionInfo[0];
//            State state = (State) actionInfo[2];
//
//            int colour = node.getColour() == State.BLACK_QUEEN ? State.WHITE_QUEEN : State.BLACK_QUEEN;
//            Action[] actions = ActionGenerator.generateActions(state, colour, node.getDepth()).toArray(new Action[0]);
//            Node expansion = new Node(state, node.getPossibleActions()[pickedActionIndex], node, colour, 0, 0, actions, node.getDepth() + 1);
//            node.getChildren()[pickedActionIndex] = expansion;
//
//            nodesToReturn.add(expansion);
//        }
//
//        return nodesToReturn.toArray(new Node[0]);
//    }

    private static int calculateLiberty(int x, int y, BitBoard board) {
        long spaceTop = 0L;
        long spaceBottom = 0L;
        spaceTop |= board.getArrowTop();
        spaceTop |= board.getBlackQueensTop();
        spaceTop |= board.getWhiteQueensTop();
        spaceBottom |= board.getArrowBottom();
        spaceBottom |= board.getWhiteQueensBottom();
        spaceBottom |= board.getBlackQueensBottom();
        long currentPositionTop = 0L;
        long currentPositionBottom = 0L;
        long boardLibertiesTop = 0L;
        long boardLibertiesBottom = 0L;
        int index = x + y * 10;
        if (index > 49) {
            index -= 50;
            currentPositionTop |= (currentPositionTop << index);
        } else
            currentPositionBottom |= (currentPositionBottom << index);
        if (x + 1 < 10) {
            // Right
            long rightTop = currentPositionTop << shiftRight;
            long rightBottom = currentPositionBottom << shiftRight;
            boardLibertiesTop |= (rightTop & spaceTop);
            boardLibertiesBottom |= (rightBottom & spaceBottom);
        }
        if (x - 1 > -1) {
            // Left
            long leftTop = currentPositionTop >> shiftRight;
            long leftBottom = currentPositionBottom >> shiftRight;
            boardLibertiesTop |= (leftTop & spaceTop);
            boardLibertiesBottom |= (leftBottom & spaceBottom);
        }
        if (y + 1 < 10) {
            // Up
            long upTop = currentPositionTop << shiftUp;
            long upBottom = currentPositionBottom << shiftUp;
            boardLibertiesTop |= (upTop & spaceTop);
            boardLibertiesBottom |= (upBottom & spaceBottom);
        }
        if (y - 1 > -1) {
            // Down
            long downTop = currentPositionTop >> shiftUp;
            long downBottom = currentPositionBottom >> shiftUp;
            boardLibertiesTop |= (downTop & spaceTop);
            boardLibertiesBottom |= (downBottom & spaceBottom);
        }
        if (x + 1 < 10 && y + 1 < 10) {
            // Up Right
            long upRightTop = currentPositionTop << shiftUp + shiftRight;
            long upRightBottom = currentPositionBottom << shiftUp + shiftRight;
            boardLibertiesTop |= (upRightTop & spaceTop);
            boardLibertiesBottom |= (upRightBottom & spaceBottom);
        }
        if (x + 1 < 10 && y - 1 > -1) {
            // Down Right
            long downRightTop = currentPositionTop >> shiftUp - shiftRight;
            long downRightBottom = currentPositionBottom >> shiftUp - shiftRight;
            boardLibertiesTop |= (downRightTop & spaceTop);
            boardLibertiesBottom |= (downRightBottom & spaceBottom);
        }
        if (x - 1 > -1 && y + 1 < 10) {
            // Up Left
            long upLeftTop = currentPositionTop << shiftUp - shiftRight;
            long upLeftBottom = currentPositionBottom << shiftUp - shiftRight;
            boardLibertiesTop |= (upLeftTop & spaceTop);
            boardLibertiesBottom |= (upLeftBottom & spaceBottom);
        }
        if (x - 1 > -1 && y - 1 > -1) {
            // Down Left
            long downLeftTop = currentPositionTop >> shiftUp + shiftRight;
            long downLeftBottom = currentPositionBottom >> shiftUp + shiftRight;
            boardLibertiesTop |= (downLeftTop & spaceTop);
            boardLibertiesBottom |= (downLeftBottom & spaceBottom);
        }
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
