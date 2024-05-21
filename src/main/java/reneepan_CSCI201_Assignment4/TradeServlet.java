package reneepan_CSCI201_Assignment4;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;

@WebServlet("/TradeServlet")
public class TradeServlet extends HttpServlet{

	private static final long serialVersionUID = 1L;
	
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
     
        Trade trade = new Gson().fromJson(request.getReader(), Trade.class);
        
        Gson gson = new Gson();
        
        double newBalance = JDBCConnector.executeTrade(trade);
        
        //something went wrong, usually invalid quantity
        if (newBalance == -1) {
        	response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        	String error = "FAILED: Purchase not possible.";
        	response.getWriter().write(gson.toJson(error));
        } 
        //not enough balance
        else if (newBalance == -2){
        	response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        	String error = "FAILED: Purchase not possible. Insufficient balance";
        	response.getWriter().write(gson.toJson(error));
        } 
        //new balance retrieved means trade successfully executed
        else {
        	response.setStatus(HttpServletResponse.SC_OK);
        	response.getWriter().write(gson.toJson(newBalance));
        }  
	}
}
