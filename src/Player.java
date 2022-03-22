import javax.websocket.Session;
import java.util.ArrayList;

public class Player {
	public Session session;
	public String username;
	public boolean isHacker;
	public boolean isAlive;
	public boolean isRegistered;
	public int votes;
	private GameManager gameManager;

	public Player(Session s, GameManager g) {
		session = s;
		gameManager = g;
		username = "John"; //they are all John
		//name = g.autoName();
		isAlive = true;
		isHacker = false;
		isRegistered = true;
		votes = 0;
	}
	public void sendTime() {
		send("TIME*=" + gameManager.clock.get());
	}
	public void sendMessage(String message) {
		send("TEXT*=" + message);
	}
	public void sendKillForm(String names) {
		send("KILLOPTIONS*=" + names);
	}
	public void sendVoteForm(String names) {
		send("VOTEOPTIONS*=" + names);
	}
	public void sendStatuses(String statuses) {
		send("STATUSES*=" + statuses);
	}
	public void sendPhase() {
		send("PHASE*=" + gameManager.phase);
	}
	
	
	//needs something to check that there aren't duplicate names
	public void setName(String n) {
		System.out.println("Setting name + " + n);
		username = n;
		send("SETNAME*=" + n);
	}
	//public String getName(String n) {
	//	
	//}
	public void setRole(String role) {
		if (role.equals("HACKER")) {
			isHacker = true;
		}
		else {
			isHacker = false;
		}
		send("ROLE*=" + role);
	}
	//sends location options to hacker and actual location to agents
	public void sendLocation() {
		if (!isHacker) {
			send("LOC*=" + gameManager.location);
			send("WIPE*=guessdiv");
		}
		else {
			send("WIPE*=locationArea");
			sendLocOptions();
		}
	}
	public void sendLocOptions() {
		String tosend = "";
		for (String s : gameManager.LOCATIONOPTIONS) {
			System.out.println("Sent loc " + s);
			tosend += s + ".=.";
		}
		send("LOCATIONOPTIONS*=" + tosend);
	}
	public void kill() {
		session.getAsyncRemote().sendText("KILL*= ");
		System.out.println("killing " + username);
		isAlive = false;
		
		gameManager.updatePlayerInfoAllInGame();
	}
	public void sendGameInSessionMessage() {
		send("INGAME*= ");
	}
	public void send(String s) {
		//session.getAsyncRemote().sendText(s);
		//trying sync
		try{
			session.getBasicRemote().sendText(s);
		}
		catch (Exception e) {
			System.out.println("Exception sending with remote.");
		}
	}
	//end state stuff
	public void sendWin() {
		send("WIN*= ");
		System.out.println(this.username + " sending win");
		if (this.isRegistered) {
			String query = "UPDATE regusers SET Wins = Wins + 1 WHERE username = '" + this.username +"'";
			gameManager.UpdateDatabase(query);
		}
		send("WIN*= ");
		//temp maybe
		isAlive = false;
	}
	public void incGamesPlayed() {
		if (this.isRegistered) {
			String query = "UPDATE regusers SET gamesPlayed = gamesPlayed + 1 WHERE username = '" + this.username +"'";
			gameManager.UpdateDatabase(query);
		}
	}
	public void incHackerPlayed() {
		if (this.isRegistered) {
			String query = "UPDATE regusers SET timesHacker = timesHacker + 1 WHERE username = '" + this.username +"'";
			gameManager.UpdateDatabase(query);
		}
	}
	public void incAgentPlayed() {
		if (this.isRegistered) {
			String query = "UPDATE regusers SET timesAgent = timesAgent + 1 WHERE username = '" + this.username +"'";
			gameManager.UpdateDatabase(query);
		}
	}
	public void sendLoss() {
		System.out.println("Sending loss to " + username + ".");
		send("LOSS*= ");
	}
	public void incAgentWins() {
		if (this.isRegistered) {
			String query = "UPDATE regusers SET AgentWins = AgentWins + 1 WHERE username = '" + this.username +"'";
			gameManager.UpdateDatabase(query);
			query = "UPDATE regusers SET timesAgent = timesAgent + 1 WHERE username = '" + this.username +"'";
			gameManager.UpdateDatabase(query);
			query = "UPDATE regusers SET gamesPlayed = gamesPlayed + 1 WHERE username = '" + this.username +"'";
			gameManager.UpdateDatabase(query);
		}
		
	}
	public void incAgentLosses() {
		if (this.isRegistered) {
			String query = "UPDATE regusers SET AgentLosses = AgentLosses + 1 WHERE username = '" + this.username +"'";
			gameManager.UpdateDatabase(query);
			query = "UPDATE regusers SET timesAgent = timesAgent + 1 WHERE username = '" + this.username +"'";
			gameManager.UpdateDatabase(query);
			query = "UPDATE regusers SET gamesPlayed = gamesPlayed + 1 WHERE username = '" + this.username +"'";
			gameManager.UpdateDatabase(query);
		}
	}
	public void incHackerWins() {
		if (this.isRegistered) {
			String query = "UPDATE regusers SET HackerWins = HackerWins + 1 WHERE username = '" + this.username +"'";
			gameManager.UpdateDatabase(query);
			query = "UPDATE regusers SET timesHacker = timesHacker + 1 WHERE username = '" + this.username +"'";
			gameManager.UpdateDatabase(query);
			query = "UPDATE regusers SET gamesPlayed = gamesPlayed + 1 WHERE username = '" + this.username +"'";
			gameManager.UpdateDatabase(query);
		}
	}
	public void incHackerLosses() {
		if (this.isRegistered) {
			String query = "UPDATE regusers SET HackerLosses = HackerLosses + 1 WHERE username = '" + this.username +"'";
			gameManager.UpdateDatabase(query);
			query = "UPDATE regusers SET timesHacker = timesHacker + 1 WHERE username = '" + this.username +"'";
			gameManager.UpdateDatabase(query);
			query = "UPDATE regusers SET gamesPlayed = gamesPlayed + 1 WHERE username = '" + this.username +"'";
			gameManager.UpdateDatabase(query);
		}
	}
	//returns the stats for user in the same order they appear in the database
	public ArrayList<String> getStatistics() {
		ArrayList<String> stats = new ArrayList<>();
		
		stats = gameManager.getStats(this.username);
		
		return stats;
		
	}
	
	//updates by incrementing selected field
	public void updateByField(String valueToUpdate, String username) {
		String queryString = "UPDATE regusers SET " + valueToUpdate + " = " + valueToUpdate + " + 1 WHERE username = '" + username + "';";
		gameManager.UpdateDatabase(queryString);
			
	}
	
	
	public String getPassword(String username) {
		String passwordGivenUser = gameManager.getPassword(username);
		
		return passwordGivenUser;
	}
	
	public void setUserInfo (String username, String password, String ign) {
		String queryString = "INSERT INTO regusers(username, password, ign) VALUES('"+ username +"','" + password + "', '" + ign + "');";
		gameManager.setUserInfo(queryString);
		
	}
	
	public boolean checkUsername(String username) {
		
		String query = "SELECT * FROM regusers WHERE username = '" + username + "'";
		boolean exists = gameManager.checkUsername(query);
		
		return exists;
	}
}
