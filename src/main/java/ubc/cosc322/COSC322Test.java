
package ubc.cosc322;

import State.State;
import ygraph.ai.smartfox.games.BaseGameGUI;
import ygraph.ai.smartfox.games.GameClient;
import ygraph.ai.smartfox.games.GameMessage;
import ygraph.ai.smartfox.games.GamePlayer;
import ygraph.ai.smartfox.games.amazons.AmazonsGameMessage;
import ygraph.ai.smartfox.games.amazons.HumanPlayer;

import java.util.ArrayList;
import java.util.Map;

import State.*;

/**
 * An example illustrating how to implement a GamePlayer
 * @author Yong Gao (yong.gao@ubc.ca)
 * Jan 5, 2021
 *
 */
public class COSC322Test extends GamePlayer {

    private GameClient gameClient = null; 
    private final BaseGameGUI gamegui;
	
    private String userName;
    private final String passwd;


	/**
     * The main method
     * @param args for name and passwd (current, any string would work)
     */
    public static void main(String[] args) {
		GamePlayer player;
		if (args[2].equals("bot"))
    		player = new COSC322Test(args[0], args[1]);
		else
			player = new HumanPlayer();
    	
    	if(player.getGameGUI() == null) {
    		player.Go();
    	}
    	else {
    		BaseGameGUI.sys_setup();
            java.awt.EventQueue.invokeLater(player::Go);
    	}
    }
	
    /**
     * Any name and passwd 
     * @param userName Username of your choice
      * @param passwd Password can be anything, isn't checked
     */
    public COSC322Test(String userName, String passwd) {
    	this.userName = userName;
    	this.passwd = passwd;
    	
    	//To make a GUI-based player, create an instance of BaseGameGUI
    	//and implement the method getGameGUI() accordingly
    	this.gamegui = new BaseGameGUI(this);
    }
 


    @Override
    public void onLogin() {
		userName = gameClient.getUserName();
		if(gamegui != null) {
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
				break;
			case GameMessage.GAME_STATE_BOARD:
				getGameGUI().setGameState((ArrayList<Integer>) msgDetails.get(AmazonsGameMessage.GAME_STATE));
				State s = new State((ArrayList<Integer>) msgDetails.get(AmazonsGameMessage.GAME_STATE));
				System.out.println(s.boardToString());
				System.out.println(ActionGenerator.generateActions(s, State.WHITE_QUEEN));
				break;
			case GameMessage.GAME_ACTION_MOVE:
				getGameGUI().updateGameState(msgDetails);
				break;
			default:
				assert(false);
		}
    	return true;   	
    }
    
    
    @Override
    public String userName() {
    	return userName;
    }

	@Override
	public GameClient getGameClient() {
		// TODO Auto-generated method stub
		return this.gameClient;
	}

	@Override
	public BaseGameGUI getGameGUI() {
		// TODO Auto-generated method stub
		return  this.gamegui;
	}

	@Override
	public void connect() {
		// TODO Auto-generated method stub
    	gameClient = new GameClient(userName, passwd, this);			
	}

 
}//end of class
