import State.State;
import Tree.MonteCarloTree;
import ygraph.ai.smartfox.games.BaseGameGUI;
import ygraph.ai.smartfox.games.GameClient;
import ygraph.ai.smartfox.games.GameMessage;
import ygraph.ai.smartfox.games.GamePlayer;
import ygraph.ai.smartfox.games.amazons.AmazonsGameMessage;
import ygraph.ai.smartfox.games.amazons.HumanPlayer;

import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Map;

import State.*;

public class Main extends GamePlayer {

    private GameClient gameClient = null;
    private final BaseGameGUI gamegui;

    private String userName;
    private final String passwd;

    private State state;
    private Action action;
    private int colour;

    private MonteCarloTree monteCarloTree;
    private final double cValue = 2.0;
    private int depth = 0;
    private static int[] moveDictionary;

    /**
     * A test main method
     *
     * @param args Optional. If you want a bot, put a username and password (currently, any string would work), else don't put anything
     */
    public static void main(String[] args) {
        GamePlayer player;
        if (args.length == 2)
            player = new Main(args[0] + "-" + ((int) (Math.random() * 1000)), args[1]);
        else
            player = new HumanPlayer();

        if (player.getGameGUI() == null) {
            player.Go();
        } else {
            BaseGameGUI.sys_setup();
            java.awt.EventQueue.invokeLater(player::Go);
        }

        // Load Move Dictionary
        try {
            ObjectInputStream objectInputStream = new ObjectInputStream(new FileInputStream("bin/training/moveDictionary.ser"));
            moveDictionary = (int[]) objectInputStream.readObject();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Any name and passwd
     *
     * @param userName Username of your choice
     * @param passwd   Password can be anything, isn't checked
     */
    public Main(String userName, String passwd) {
        this.userName = userName;
        this.passwd = passwd;

        //To make a GUI-based player, create an instance of BaseGameGUI
        //and implement the method getGameGUI() accordingly
        this.gamegui = new BaseGameGUI(this);
    }


    @Override
    public void onLogin() {
        userName = gameClient.getUserName();
        if (gamegui != null) {
            gamegui.setRoomInformation(gameClient.getRoomList());
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean handleGameMessage(String messageType, Map<String, Object> msgDetails) {
        //This method will be called by the GameClient when it receives a game-related message
        //from the server.

        //For a detailed description of the message types and format,
        //see the method GamePlayer.handleGameMessage() in the game-client-api document.

        switch (messageType) {
            case GameMessage.GAME_ACTION_START:
                // If we are black, we move first
                boolean isBlack = msgDetails.get(AmazonsGameMessage.PLAYER_BLACK).equals(getGameClient().getUserName());
                colour = isBlack ? State.BLACK_QUEEN : State.WHITE_QUEEN;
                monteCarloTree = new MonteCarloTree(state, cValue, colour, depth, moveDictionary);
                if (isBlack)
                    makeMove();
                break;
            case GameMessage.GAME_STATE_BOARD:
                getGameGUI().setGameState((ArrayList<Integer>) msgDetails.get(AmazonsGameMessage.GAME_STATE));
                state = new State((ArrayList<Integer>) msgDetails.get(AmazonsGameMessage.GAME_STATE));
                System.out.println(state.boardToString());
                break;
            case GameMessage.GAME_ACTION_MOVE:
                getGameGUI().updateGameState(msgDetails);
                depth++;
                Action opponentAction = new Action(msgDetails);
                action = opponentAction;
                System.out.println("Opponent action: " + opponentAction);
                if (!ActionChecker.validMove(state, opponentAction)) {
                    for (int i = 0; i < 10; i++) {
                        System.out.println("INVALID MOVE!");
                    }
                }
                state = new State(state, opponentAction);
                makeMove();
                if (ActionGenerator.generateActions(state, colour == State.BLACK_QUEEN ? State.WHITE_QUEEN : State.BLACK_QUEEN).size() == 0) {
                    System.out.println("We won Mr. Stark");
                }

                break;
            default:
                assert (false);
        }
        return true;
    }

    private void makeMove() {
        makeMonteCarloMove();
        depth++;
    }

    private void makeMonteCarloMove() {
        long start = System.currentTimeMillis();
        if (action != null)
            monteCarloTree.updateRoot(state, action, colour, depth);
        Action definitelyTheBestAction = monteCarloTree.search();
        if (definitelyTheBestAction == null) {
            System.out.println("OPPONENT WINS!!");
            System.exit(0);
        }
        state = new State(state, definitelyTheBestAction);
        getGameClient().sendMoveMessage(definitelyTheBestAction.toServerResponse());
        getGameGUI().updateGameState(definitelyTheBestAction.toServerResponse());

        long end = System.currentTimeMillis();

        System.out.println(state.boardToString());
        System.out.println("Bot action: " + definitelyTheBestAction);
        System.out.printf("Time taken: %dms\n", (end - start));
    }

    private void makeRandomMove() {
        long start = System.nanoTime();
        ArrayList<Action> actions = ActionGenerator.generateActions(state, colour);
        Action selectedAction = actions.get((int) (Math.random() * actions.size()));
        state = new State(state, selectedAction);
        getGameClient().sendMoveMessage(selectedAction.toServerResponse());
        getGameGUI().updateGameState(selectedAction.toServerResponse());
        long end = System.nanoTime();
        System.out.println(state.boardToString());
        System.out.println("Bot action: " + selectedAction);
        System.out.printf("Time taken: %.1fms\n", (end - start) / 1000000.);
    }

    @Override
    public String userName() {
        return userName;
    }

    @Override
    public GameClient getGameClient() {
        return this.gameClient;
    }

    @Override
    public BaseGameGUI getGameGUI() {
        return this.gamegui;
    }

    @Override
    public void connect() {
        gameClient = new GameClient(userName, passwd, this);
    }


}//end of class
