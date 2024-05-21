package reneepan_CSCI201_Assignment4;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;

@WebServlet("/LoginServlet")
public class LoginServlet extends HttpServlet {
		
	private static final long serialVersionUID = 1L;

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
		
		PrintWriter pw = response.getWriter();
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		
		User user = new Gson().fromJson(request.getReader(), User.class);
		
		//get user information
        String username = user.getUsername();
        String password = user.getPassword();
		
		Gson gson = new Gson();
		
		//check if any information is missing
		if (username == null || username.isBlank() || password == null || password.isBlank()) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			String error = "Login info is missing";
			pw.write(gson.toJson(error));
			pw.flush();
		}
		else {
			//check if user can be logged in
			int userID = JDBCConnector.loginUser(username, password);
			
			if (userID == -1) {
				//user doesn't exist
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				String error = "Username and password combination not found";
				pw.write(gson.toJson(error));
				pw.flush();
			}
			else {
				response.setStatus(HttpServletResponse.SC_OK);
				pw.write(gson.toJson(userID));
				pw.flush();
			}
		}
	}	
}

