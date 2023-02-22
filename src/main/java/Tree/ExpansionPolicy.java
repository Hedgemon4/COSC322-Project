package Tree;

import State.Action;
import State.ActionGenerator;
import State.State;

public class ExpansionPolicy {
    public static Node expansionNode(Node node) {
        return libertyExpansionPolicy(node);
    }

    private static Node randomExpansion(Node node) {
        System.out.println(node.getState().boardToString());
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

    private static Node libertyExpansionPolicy(Node node) {
        // Want to prioritize moving queens which might get trapped
        int[][] nodeQueens = node.getState().getQueens(node.getColour());
        byte[][] board = node.getState().getBoard();
        int[][] nodeLiberty = calculateLiberties(nodeQueens, board);
        // Prioritize trapping enemy queens
        int otherColour = node.getColour() == State.BLACK_QUEEN ? State.WHITE_QUEEN : State.BLACK_QUEEN;
        int[][] otherQueens = node.getState().getQueens(otherColour);
        int[][] otherLiberty = calculateLiberties(otherQueens, board);
        // Don't make moves if trapped
        int[] actionWeight = new int[node.getPossibleActions().length];
        int i = 0;
        int pickedActionIndex = 0;
        int bestActionWeight = -999999;
        State state = node.getState();
        for (Action action : node.getPossibleActions()) {
            int queenX = action.getOldX();
            int queenY = action.getOldY();
            if (action.getNewX() == 0 || action.getNewX() == 9)
                actionWeight[i] -= 50;
            if (action.getNewY() == 0 || action.getNewY() == 9)
                actionWeight[i] -= 50;
            if (nodeLiberty[queenX][queenY] != 0) {
                if (nodeLiberty[queenX][queenY] < 3)
                    actionWeight[i] += 20;
            }
            State result = new State(node.getState(), action);
            int[][] resultingLiberty = calculateLiberties(otherQueens, result.getBoard());
            if (resultingLiberty[otherQueens[0][0]][otherQueens[0][1]] < 3) {
                actionWeight[i] += 10;
            }
            if (resultingLiberty[otherQueens[1][0]][otherQueens[1][1]] < 3) {
                actionWeight[i] += 10;
            }
            if (resultingLiberty[otherQueens[2][0]][otherQueens[2][1]] < 3) {
                actionWeight[i] += 10;
            }
            if (resultingLiberty[otherQueens[3][0]][otherQueens[3][1]] < 3) {
                actionWeight[i] += 10;
            }
            if (actionWeight[i] > bestActionWeight) {
                if(node.getChildren()[i] == null) {
                    bestActionWeight = actionWeight[i];
                    pickedActionIndex = i;
                    state = result;
                }
            }
            i++;
        }
        int colour = node.getColour() == State.BLACK_QUEEN ? State.WHITE_QUEEN : State.BLACK_QUEEN;
        Action[] actions = ActionGenerator.generateActions(state, colour, node.getDepth()).toArray(new Action[0]);
        Node expansion = new Node(state, node.getPossibleActions()[pickedActionIndex], node, colour, 0,
                0, actions, node.getDepth() + 1);
        node.getChildren()[pickedActionIndex] = expansion;
        System.out.println(expansion.getState().boardToString());
        return expansion;
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
