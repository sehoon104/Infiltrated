

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet implementation class Registration
 */
@WebServlet("/Registration")
public class Registration extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
	//static Connection connection = null;
	//public static final String CREDENTIALS_STRING = null;//deleted
	static Connection connection = null;
	public static final String CREDENTIALS_STRING = null;//deleted
	public Registration() {
        super();
        // TODO Auto-generated constructor stub
        try {
			Class.forName("com.mysql.cj.jdbc.Driver");
			connection = DriverManager.getConnection(CREDENTIALS_STRING, "test", "test");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		PrintWriter out = response.getWriter();
		boolean addUser = true;
		String fieldtoValidate = request.getParameter("field");
		if(fieldtoValidate != null && fieldtoValidate.equals("username")) {
			String username = request.getParameter("username");
			if(check_username(username)) {
				out.println("Username already taken. Please type a new one.");
			}
		}
		if(fieldtoValidate != null && fieldtoValidate.equals("password")) {
			String password = request.getParameter("password");
			String passwordConfirm = request.getParameter("passwordconfirm");
			if(!password.equals(passwordConfirm)) {
				out.println("Password doesn't match");
			}
		}
		
		if(fieldtoValidate != null && fieldtoValidate.equals("all")) {
			String username = request.getParameter("username");
			String password = request.getParameter("password");
			String passwordconfirm = request.getParameter("passwordconfirm");
			boolean valid = true;
			//USERNAME CHECKING
			if(check_username(username)) {
				valid = false;
			}
			
			//PASSWORD CHECKING
			else if(!password.equals(passwordconfirm)) {
				valid = false;
			}
			
			
			if(!valid) {
				out.println("Please fix the errors before submitting again.");
			}
			else if(valid) {
				CreateUser(username, password);
				out.println("Successfully created user");
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
	private void CreateUser(String username, String password) {
		//ign = username for now
		System.out.println("Creating user: " + username + password);
		String queryString = "INSERT INTO regusers(username, password, ign) VALUES('"+ username +"','" + password + "', '" + username + "');";
		try {
			PreparedStatement preparedStatement = connection.prepareStatement(queryString);
			preparedStatement.executeUpdate();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}


	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request, response);
	}

}
