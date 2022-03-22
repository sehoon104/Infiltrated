import java.util.Vector;

import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

@ServerEndpoint(value = "/ws")
public class WebSocket {

	//private static Vector<Session> sessionVector = new Vector<Session>();
	private static Vector<Player> playerVector = new Vector<Player>();
	private static GameManager gameManager = new GameManager(playerVector);
	
	@OnOpen
	public void open(Session session) {
		if (playerVector.size() == 0) {
			//opening first connection starts the manager
			System.out.println("CONNECT!");
			//reset manager 
			gameManager = new GameManager(playerVector);
			gameManager.start();
		} //clunky
		System.out.println("Connection made!");
		Player p = new Player(session, gameManager);
		gameManager.tryAddPlayer(p);
		//playerVector.add(p);
		
	}
	
	@OnMessage
	public void onMessage(String message, Session session) {
		System.out.println(message);
		gameManager.handleCommand(message, session);
	}
	
	@OnClose
	public void close(Session session) {
		System.out.println("Disconnecting!");
		//remove player
		Player leavingPlayer  =gameManager.getPlayerFromSession(session);
		if (leavingPlayer != null) {
			System.out.println("Leaving is non null");
			if (leavingPlayer.isHacker) gameManager.agentsWin();
			playerVector.remove(leavingPlayer);
		}
		//sessionVector.remove(session);
	}
	
	@OnError
	public void error(Throwable error) {
		System.out.println("Error!" + error);
	}
}
