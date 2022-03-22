

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;

/**
 * Servlet implementation class LoadStats
 */
@WebServlet("/LoadStats")

public class LoadStats extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
	static Connection connection = null;
	public static final String CREDENTIALS_STRING = null;//DELETED
    public LoadStats() {
        super();
        try {
			Class.forName("com.mysql.cj.jdbc.Driver");
			connection = DriverManager.getConnection(CREDENTIALS_STRING, "test", "test");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		PrintWriter out = response.getWriter();
		//response.getWriter().append("Served at: ").append(request.getContextPath());
		String username = request.getParameter("username");
		System.out.println("Username stats checking:" + username);
		ArrayList<String> stats = getStats(username);
		Gson gson = new Gson();
		String result = gson.toJson(stats);
		out.println(result);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request, response);
	}
	
	public ArrayList<String> getStats(String user) {
    	
    	ArrayList<String> Stats = new ArrayList<>();
    	
		try {
			Statement st = connection.createStatement();
			PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM regusers WHERE username= '" + user + "';");
			ResultSet resultSet = preparedStatement.executeQuery();
			
			while (resultSet.next()) {
				//a little un-necessary but just in case
				Stats.add(resultSet.getString("username"));
				Stats.add(resultSet.getString("wins"));
				Stats.add(resultSet.getString("gamesPlayed"));
				Stats.add(resultSet.getString("timesHacker"));
				Stats.add(resultSet.getString("timesAgent"));
				Stats.add(resultSet.getString("hackerWins"));
				Stats.add(resultSet.getString("agentWins"));
				
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
    	
		return Stats;
		
    }

}
