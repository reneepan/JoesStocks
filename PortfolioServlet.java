package reneepan_CSCI201_Assignment4;

import java.io.IOException;
import java.util.ArrayList;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;

@WebServlet("/PortfolioServlet")
public class PortfolioServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;
	
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        //get user ID
        String userIDString = request.getParameter("userID");
        int userID = 0;
        //check if userID is empty
        if (userIDString != null && !userIDString.isEmpty()) {
        	userID = Integer.parseInt(userIDString);
        }
        
        Gson gson = new Gson();
        
        //get user balance
        double balance = JDBCConnector.getBalance(userID);

        if (balance == -1) {
        	//didn't retrieve balance correctly, something went wrong
        	response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            String error = "Error occured";
            response.getWriter().write(gson.toJson(error));
        } else {
        	//balance retrieved
            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().write(gson.toJson(balance));
        }  
	}
	 
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setContentType("application/json");
	    response.setCharacterEncoding("UTF-8");
	     
	    User user = new Gson().fromJson(request.getReader(), User.class);
	    int userID = user.getUserID();
	        
	    Gson gson = new Gson();
	    
	    //get user's portfolio
	    ArrayList<Trade> portfolio = JDBCConnector.getPortfolio(userID);
	    
        response.setStatus(HttpServletResponse.SC_OK);
        response.getWriter().write(gson.toJson(portfolio));
	 }

}
