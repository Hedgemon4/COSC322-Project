package Tree;

import State.Action;
import State.ActionGenerator;
import State.State;

import java.util.ArrayList;
import java.util.PriorityQueue;

public class ExpansionPolicy {
    public static Node[] expansionNode(Node node, int numToExpand) {
        return libertyExpansionPolicy(node, numToExpand);
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

    private static Node[] libertyExpansionPolicy(Node node, int numToExpand) {
        // Want to prioritize moving queens which might get trapped
        int[][] nodeQueens = node.getState().getQueens(node.getColour());
        byte[][] board = node.getState().getBoard();
        int[][] nodeLiberty = calculateLiberties(nodeQueens, board);
        // Prioritize trapping enemy queens
        int otherColour = node.getColour() == State.BLACK_QUEEN ? State.WHITE_QUEEN : State.BLACK_QUEEN;
        int[][] otherQueens = node.getState().getQueens(otherColour);
        int[][] otherLiberty = calculateLiberties(otherQueens, board);
        // Don't make moves if trapped

        // Store each potential node in this max-heap for easily finding the top nodes
        PriorityQueue<Object[]> topActions = new PriorityQueue<>((o1, o2) -> ((int) o2[1]) - ((int) o1[1]));

        int i = 0;
        for (Action action : node.getPossibleActions()) {
            // Don't expand if this action has already been expanded
            if(node.getChildren()[i] != null) {
                i++;
                continue;
            }

            int actionWeight = 0;

            int queenX = action.getOldX();
            int queenY = action.getOldY();

            // Punish moving to the edge
            if (action.getNewX() == 0 || action.getNewX() == 9)
                actionWeight -= 50;
            if (action.getNewY() == 0 || action.getNewY() == 9)
                actionWeight -= 50;

            // Punish moving to a spot with less than 3 liberties
            if (nodeLiberty[queenX][queenY] != 0) {
                if (nodeLiberty[queenX][queenY] < 3)
                    actionWeight += 20;
            }
            State result = new State(node.getState(), action);
            int[][] resultingLiberty = calculateLiberties(otherQueens, result.getBoard());
            if (resultingLiberty[otherQueens[0][0]][otherQueens[0][1]] < 3) {
                actionWeight += 10;
            }
            if (resultingLiberty[otherQueens[1][0]][otherQueens[1][1]] < 3) {
                actionWeight += 10;
            }
            if (resultingLiberty[otherQueens[2][0]][otherQueens[2][1]] < 3) {
                actionWeight += 10;
            }
            if (resultingLiberty[otherQueens[3][0]][otherQueens[3][1]] < 3) {
                actionWeight += 10;
            }

            // Add relevant data to the heap
            topActions.offer(new Object[]{i, actionWeight, result});
            i++;
        }

        // Find top nodes to expand (or all if there are less than numToExpand)
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
