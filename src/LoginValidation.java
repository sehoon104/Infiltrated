

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet implementation class LoginValidation
 */
@WebServlet("/LoginValidation")
public class LoginValidation extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
	static Connection connection = null;
	public static final String CREDENTIALS_STRING = NULL;//DELETED
	//public static final String CREDENTIALS_STRING = NULL;//DELETED
    
	public LoginValidation() {
    	super();
    	
       
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection(CREDENTIALS_STRING, "test", "test");
    	}
		
		catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// TODO Auto-generated method stub
		PrintWriter out = response.getWriter();
		String username = request.getParameter("username");
		String password = request.getParameter("password");
		System.out.println("Login Validation: " + username + password);
		//do jdbc stuff here, take the name parameter and password parameter and check against the database
		if(!check_username(username)) {
			out.println("Username doesn't exist");
		}
		else {
			if(checkPassword(username, password)) {
				out.println("Logged in successfully!");
			}
			else {
				out.println("Password incorrect");
			}
		}
		
		
	}	
	
	private boolean check_username(String username) {
		if(checkUsername(username)) {
			return true;
		}
		else {
			return false;
		}
	}
	private boolean checkPassword(String username, String password) {
		if(password.equalsIgnoreCase(getPassword(username))) {
			return true;
		}
		else {
			return false;
		}
	}
	
	private String getPassword(String username) {
		
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
	
	private boolean checkUsername(String username) {
		try {
			PreparedStatement preparedStatement = connection.prepareStatement("SELECT username FROM regusers where username = ?");
			preparedStatement.setString(1, username);
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

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request, response);
	}

}
