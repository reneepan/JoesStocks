package reneepan_CSCI201_Assignment4;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.google.gson.Gson;

@WebServlet("/RegisterServlet")
public class RegisterServlet extends HttpServlet {
    
    private static final long serialVersionUID = 1L;

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
     
        User user = new Gson().fromJson(request.getReader(), User.class);

        String username = user.getUsername();
        String password = user.getPassword();
        String email = user.getEmail();
        
        Gson gson = new Gson();
        //Check if the user information is missing
        if (username == null || username.isBlank() || password == null || password.isBlank() || email == null || email.isBlank()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            String error = "Register info missing";
            response.getWriter().write(gson.toJson(error));
        } else {
            //register the user using JDBCConnector
            int userID = JDBCConnector.registerUser(username, password, email);
            
            //username taken
            if (userID == -1) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                String error = "Username is taken";
                response.getWriter().write(gson.toJson(error));
            //email taken
            } else if (userID == -2) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                String error = "Email is already registered";
                response.getWriter().write(gson.toJson(error));
            //user successfully registered
            } else {
                response.setStatus(HttpServletResponse.SC_OK);
                response.getWriter().write(gson.toJson(userID));
            }
        }
    }
}

