import java.util.Vector;
import javax.websocket.Session;
import java.util.Random;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

public class GameManager extends Thread{
	public int ROUNDTIME = 30;
	public String[] LOCATIONOPTIONS = {"Courthouse", "Clocktower","Station", "Lighthouse", "Church", "Pier"};
	public Clock clock;
	public Vector<Player> players;
	public String location;
	public String phase;
	public String mode;
	//public Connection connection;
	public boolean gameOver;
	Connection connection = null;
    public final String CREDENTIALS_STRING = null;//DELETED
	//private ArrayList<String> messages;
	
	public GameManager(Vector<Player> p) {
		players = p;
		//choose random location
		phase = "MESSAGING";
		int rnd = new Random().nextInt(LOCATIONOPTIONS.length);
		location = LOCATIONOPTIONS[rnd];
		mode = "INGAME";
		gameOver = false;
		try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection(CREDENTIALS_STRING, "test", "test");
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
		
		//NEED TO ESTABLISH CONNECTION TO SQL
	}
	//Here's where the actual game-logic would go
	@SuppressWarnings("deprecation")
	public void run() {
		clock = new Clock(this, ROUNDTIME); //second parameter is what the clock counts down from
		clock.start();
		
		while (true) {
			//updateInGame();//must remove
			if (mode.equals("INGAME")) {
				//if (!updateInGame()) break;
				updateInGame();
				if (gameOver) break;
			}
			else if (mode.equals("WAITING")) {
				//if (!updateInGame()) break;
			}
			//for some reason resetting the clock does not work nicely here
			
			//hacker runs out of guesses
			//hacker guesses correctly
		}
		System.out.println("Game ended.");
		players.clear();
		clock.stop();
	}
	public boolean updateInGame() {
		//checking votes
		//checkVotes();
		//make a hacker if the game is still going adn there isnt one

		//win conditions
		if (players.size() >= 0) { //>= 3
			if (checkAgentKillWin()) {
				System.out.println("Agents win");
				agentsWin();
				return false;
			}
			//hacker kills all but one agent - need a way to disable killing more than once per round. Currently only looking if every other 
			//player is dead.
			else if (checkHackerKillWin()) {
				System.out.println("Hacker wins");
				hackerWins();
				return false;
			}
		}
		//not enough players left, hacker wins
		else {
			hackerWins();
			return false;
		}
		return true;
	}
	
	public void checkVotes() {
		//any vote over 50% of players results in that player getting killed
		Player withTheMost = null;
		for (Player p : players) {
			if (withTheMost == null) {
				withTheMost = p;
			}
			else if (p.votes > withTheMost.votes) {
				withTheMost = p;
			}
		}
		//if (withTheMost == null) return;
		if (withTheMost.votes >= players.size()/2) {
			//System.out.println(withTheMost.username + " " + withTheMost.votes);
			withTheMost.kill();
		}
		System.out.println("MOST" + withTheMost.username + withTheMost.votes);
	}
	
	public void tryAddPlayer(Player p) {
		System.out.println(players.size());
		addPlayer(p);
		updatePlayerInfoAllInGame();
		//having lots of issues with switching pages 
		/*if (mode.equals("WAITING")) {
			addPlayer(p);
		}
		else {
			System.out.println("rejected a player");
			p.sendGameInSessionMessage();
		}*/
	}
	public void addPlayer(Player p) {
		p.setName(autoName());
		//if (p.username == null) {
		//	p.setName(autoName());
		//}
		p.sendPhase();
		players.add(p);
		
		//broadcast change - should be consolidated into a function in the game manager
		if (players.size() == 1) {
			p.setRole("HACKER");
			//p.sendWin();//debugging
		}
		else {
			p.setRole("AGENT");
		}
		updatePlayerInfoAllWaiting();
		/*if (playerVector.size() >= 2) {
			gameManager.sendStatusesAll();
		}*/
		System.out.println("Added player " + p.username + " " + p.session);
	}
	public void handleCommand(String message, Session session) {
		if (mode.equals("INGAME")) {
			handleCommandInGame(message, session);
		}
		else if (mode.equals("WAITING")) {
			handleCommandWaiting(message, session);
		}
	}
	public void startGame() {
		sendAll("START*= ");
		//players.clear();
		updatePlayerInfoAllInGame();
		//doesnt seem to work
	}
	public void handleCommandWaiting(String message, Session session) {
		String[] event = message.split("\\*=");
		if (event.length < 2) {
			System.out.println("Problem: Command with less than two components.");
			return;
		}
		String command = event[0];
		String data = event[1];
		//chat
		if (command.equals("START")) {
			startGame();
			//sendAll("START*= ");
			//updatePlayerInfoAllInGame();
		}
	}
	public void handleCommandInGame(String message, Session session) {
		//break it down
		String[] event = message.split("\\*=");
		if (event.length < 2) {
			System.out.println("Problem: Command with less than two components.");
			return;
		}
		String command = event[0];
		String data = event[1];
		//chat
		if (command.equals("TEXT")) {
			sendMessageAll(getPlayerFromSession(session).username + " says: " + data);
		}
		//setting player name server-side
		else if (command.equals("MYNAME") && !data.equals("null")) {
			System.out.println("GOT NAME");
			getPlayerFromSession(session).username = data;
			//also reset name
			//sendKillFormAll();
			//sendStatusesAll();
			updatePlayerInfoAllInGame();
		}
		//killing an agent
		else if (command.equals("KILL")) {
			getPlayerFromName(data).kill();
			updatePlayerInfoAllInGame();
		}
		//voting
		else if (command.equals("VOTE")) {
			//VOTING CODE
			System.out.println(getPlayerFromSession(session).username + " voted for " + data);
			checkVotes();
			getPlayerFromName(data).votes ++;
		}
		else if (command.equals("GUESS")) {
			//don't need a whole function for this
			if (data.equals(location)) {
				hackerWins();
			}
		}
		//can put in conditions for votes, kills, location choices
		
		
		//non-game
		else if (command.equals("START")) {
			mode = "INGAME";
		}
	}
	//only sends to hackers - it's kind of stupid to have this in the game manager but whatever
	public void sendKillFormAll() {
		for (Player p : players) {
			String tosend = "";
			if (p.isHacker) {
				for (Player q : players) {
					if (p != q && q.isAlive) {
						System.out.println("Added name " + q.username);
						tosend += q.username + ".=.";
					}
				}
				p.sendKillForm(tosend);
			}
		}
	}
	public void sendVoteFormAll() {
		for (Player p : players) {
			String tosend = "";
			for (Player q : players) {
				if (p != q && q.isAlive) {
					System.out.println("Added name to vote form " + q.username);
					tosend += q.username + ".=.";
				}
			}
			p.sendVoteForm(tosend);
		}
	}
	public void resetVotesAll() {
		for (Player p : players) {
			p.votes = 0;
		}
	}
	public void sendStatusesAll() {
		for (Player p : players) {
			String tosend = "";
			for (Player q : players) {
				if (p != q) {
					tosend += q.username + "=="+ q.isAlive + ".=.";
				}
			}
			
			//tosend = tosend.substring(0, tosend.length() - 3); //the clunkiest way of removing that last bit
			p.sendStatuses(tosend);
		}
	}
	public void sendMessageAll(String s) {
		for (Player p : players) {
			p.sendMessage(s);
		}
	}
	public void sendTimeAll() {
		for (Player p : players) {
			p.sendTime();
		}
	}
	public void sendLocationAll() {
		for (Player p : players) {
			p.sendLocation();
		}
	}
	public void sendPhaseAll() {
		for (Player p : players) {
			p.sendPhase();
		}
	}
	public void sendAll(String s) {
		for (Player p : players) {
			p.send(s);
		}
	}
	public void updatePlayerInfoAllWaiting() {
		sendStatusesAll();
	}
	public void updatePlayerInfoAllInGame() {
		sendKillFormAll();
		sendVoteFormAll();
		sendStatusesAll();
		sendLocationAll();
	}
	public void agentsWin() {
		gameOver =true;
		for (Player p : players) {
			if (!p.isHacker) {
				p.sendWin();
				p.incAgentWins();
			}
			else {
				try {
					Thread.sleep(5);
				}
				catch (Exception e) {
					System.out.println("sleep issue line 305 in GM.");
				}
				p.sendLoss();
				p.incHackerLosses();
			}
		}
	}
	public void hackerWins() {
		gameOver = true;
		for (int i=0; i < players.size(); i ++) {
			Player p = players.get(i);
			if (p.isHacker) {
				//System.out.println("GOT TO HERE");
				p.sendWin();
				p.incHackerWins();
			}
			else {
				try {
					Thread.sleep(5);
				}
				catch (Exception e) {
					System.out.println("sleep issue line 318 in GM.");
				}
				//if (p.isAlive) {
				//	System.out.println("THIS IS THE ISSUE");
				p.sendLoss(); //dead can't receive at the moment
				//}
				p.incAgentLosses();
			}
		}
	}
	public Player getPlayerFromSession(Session session) {
		for (Player p : players) {
			if (p.session == session) {
				return p;
			}
		}
		return null;
	}
	public Player getPlayerFromName(String name) {
		for (Player p : players) {
			if (p.username.equals(name)) {
				return p;
			}
		}
		return null;
	}
	public String autoName() {
		int num = 0;
		boolean good = false;
		String basename = "Guest ";
		String newname = "Placeholder";
		while (!good) {
			good = true;
			num ++;
			newname = basename + num;
			for (Player p : players) {
				if (newname.equals(p.username)) {
					good = false;
				}
			}
		}
		return newname;
	}
	public void switchPhase() {
		if (phase.equals("MESSAGING")) {
			phase = "VOTING";
		}
		else {
			phase = "MESSAGING";
			//reset votes
			resetVotesAll();
		}
		sendPhaseAll();
		//this could also be where state management takes place
	}
	public boolean checkAgentKillWin() {
		for (Player p : players) {
			if (p.isHacker && !p.isAlive) {
				return true;
			}
		}
		return false;
	}
	public boolean checkHackerKillWin() {
		if (players.size() <= 1) {
			return false;
		}
		for (Player p : players) {
			if (!p.isHacker && p.isAlive) {
				return false;
			}
		}
		return true;
	}
	
	public void UpdateDatabase(String msg) {
		System.out.println(msg);
		try {
			PreparedStatement preparedStatement = connection.prepareStatement(msg);
			preparedStatement.execute();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	public String getPassword(String username) {
			
			String passwordString = "";
			try {
				PreparedStatement preparedStatement = connection.prepareStatement("SELECT password FROM regusers WHERE username= '" + username + "';");
				ResultSet resultSet = preparedStatement.executeQuery();
			
				while (resultSet.next()) {
					passwordString = resultSet.getString("password");
				
				}
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			return passwordString;
		}
	
	public void setUserInfo(String queryString) {
		try {
			
			PreparedStatement preparedStatement = connection.prepareStatement(queryString);
			preparedStatement.executeUpdate();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}
	
	public boolean checkUsername(String queryString) {
		try {
			PreparedStatement preparedStatement = connection.prepareStatement(queryString);
			ResultSet resultSet = preparedStatement.executeQuery();
			
			if (resultSet.next() == false) {
				//username doesnt exist. result set is empty
				return false;
			}
			else {
				return true;
			}
	
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//should never reach here
		return false;
		
	}
	
	public ArrayList<String> getStats(String user) {
    	
    	ArrayList<String> Stats = new ArrayList<>();
    	
		try {
			Statement st = connection.createStatement();
			PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM regusers WHERE username= '" + user + "';");
			ResultSet resultSet = preparedStatement.executeQuery();
			
			while (resultSet.next()) {
				//a little un-necessary but just in case
				Stats.add(resultSet.getString("entryID"));
				Stats.add(resultSet.getString("username"));
				Stats.add(resultSet.getString("password"));
				Stats.add(resultSet.getString("ign"));
				Stats.add(resultSet.getString("create_time"));
				Stats.add(resultSet.getString("wins"));
				Stats.add(resultSet.getString("gamesPlayed"));
				Stats.add(resultSet.getString("timesHacker"));
				Stats.add(resultSet.getString("timesAgent"));
				Stats.add(resultSet.getString("hackerWins"));
				Stats.add(resultSet.getString("agentWins"));
				Stats.add(resultSet.getString("hackerLosses"));
				Stats.add(resultSet.getString("agentLosses"));
				
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
    	
		return Stats;
		
    }
	
}
