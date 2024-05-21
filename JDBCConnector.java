package reneepan_CSCI201_Assignment4;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import com.mysql.cj.xdevapi.PreparableStatement;

public class JDBCConnector {
	//function to register user
	public static int registerUser(String username, String password, String email) {
		try {
			Class.forName("com.mysql.cj.jdbc.Driver");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		
		Connection conn = null;
		java.sql.Statement st = null;
		ResultSet rs = null;
		
		int userID = -1;
		
		
		try {
			//connect to database
			conn = DriverManager.getConnection("jdbc:mysql://localhost/joesstocks?user=root&password=rigat0n1pA5tA");
			
			st = conn.createStatement();
			rs = st.executeQuery("SELECT * FROM users WHERE username='" + username + "'");
			//if username doesn't exist
			if (!rs.next()) {
				st = conn.createStatement();
				rs = st.executeQuery("SELECT * FROM users WHERE email='" + email + "'");
				//if email isn't registered
				if (!rs.next()) {
					rs.close();
					//create new user
					st.execute("INSERT INTO users (username, password, email, balance) VALUES ('" + username + "', '" 
							+ password + "','" + email + "', 50000)");
					rs = st.executeQuery("SELECT LAST_INSERT_ID()");
					rs.next();
					userID = rs.getInt(1);
				}
				else {
					userID = -2;
				}
			}
		} catch (SQLException e) {
			System.out.println("SQLException in registerUser. ");
			e.printStackTrace();
		} finally {
			try {
				if (rs != null) {
					rs.close();
				}
				if (st != null) {
					st.close();
				}
				if (conn != null) {
					conn.close();
				}
			} catch (SQLException e) {
				System.out.println("SQL error: " + e.getMessage());
			}
		}
		
		return userID;
	}
	
	//function to login user
	public static int loginUser(String username, String password) {
		try {
			Class.forName("com.mysql.cj.jdbc.Driver");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		
		Connection conn = null;
		java.sql.Statement st = null;
		ResultSet rs = null;
		
		int userID = -1;
		
		
		try {
			conn = DriverManager.getConnection("jdbc:mysql://localhost/joesstocks?user=root&password=rigat0n1pA5tA");
			
			st = conn.createStatement();
			rs = st.executeQuery("SELECT user_id FROM users WHERE username='" + username + "' AND password='" + password + "';");
			//if user with that username/password combination exists, retrieve userID
			if (rs.next()) {
				userID = rs.getInt("user_id");
			}
		} catch (SQLException e) {
			System.out.println("SQLException in loginUser.");
			e.printStackTrace();
		} finally {
			try {
				if (rs != null) {
					rs.close();
				}
				if (st != null) {
					st.close();
				}
				if (conn != null) {
					conn.close();
				}
			} catch (SQLException e) {
				System.out.println("SQL error: " + e.getMessage());
			}
		}
		return userID;
	}
	
	//execute trade
	public static double executeTrade(Trade trade) {
		try {
			Class.forName("com.mysql.cj.jdbc.Driver");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		//get trade information
		int userID = trade.getUserID();
		int stockNum = trade.getStockNum();
		double stockPrice = trade.getStockPrice();
		String ticker = trade.getTicker();
		
		Connection conn = null;
		java.sql.Statement st = null;
		ResultSet rs = null;
		
		double newBalance = -1;
		
		try {
			conn = DriverManager.getConnection("jdbc:mysql://localhost/joesstocks?user=root&password=rigat0n1pA5tA");
			
			st = conn.createStatement();
			rs = st.executeQuery("SELECT balance FROM users WHERE user_id='" + userID + "';");
			//balance retrieved
			if (rs.next()) {
				double currentBalance = rs.getDouble("balance");
				//buy stock
				if (stockNum > 0) {
					//if enough money
					if (currentBalance - stockNum * stockPrice >= 0) {
						newBalance = currentBalance - stockNum * stockPrice;
						
						//update user balance
						int update = st.executeUpdate("UPDATE users SET balance='" + newBalance + "' WHERE user_id='" + userID + "';");
						if (update < 0) {
							System.out.println("problem in executeTrade");
						}
						//update portfolio
						rs = st.executeQuery("SELECT numStock FROM portfolio WHERE user_id ='" + userID + "'AND ticker ='" + ticker + "';");
						if (rs.next()) {
							st.execute("UPDATE portfolio SET numStock = numStock + " + stockNum + " WHERE user_id = " + userID + " AND ticker ='" + ticker + "';");
							st.execute("UPDATE portfolio SET price = price + " + stockNum * stockPrice + " WHERE user_id = " + userID + " AND ticker ='" + ticker + "';");
						}
						else {
							st.execute("INSERT INTO portfolio (user_id, ticker, numStock, price) VALUES "
									+ "('" + userID + "','" + ticker + "','" + stockNum + "','" + stockPrice * stockNum + "')");
						}
					}
					else {
						newBalance = -2;
					}
				}
				//sell stock
				else {
					newBalance = currentBalance + -1 * stockNum * stockPrice;
					//update balance
					int update = st.executeUpdate("UPDATE users SET balance='" + newBalance + "' WHERE user_id='" + userID + "';");
					if (update < 0) {
						System.out.println("problem in executeTrade");
					}
					//update portfolio
					rs = st.executeQuery("SELECT numStock FROM portfolio WHERE user_id ='" + userID + "'AND ticker ='" + ticker + "';");
					if (rs.next()) {
						if (rs.getInt("numStock") + stockNum > 0) {
							st.execute("UPDATE portfolio SET numStock = numStock + " + stockNum + " WHERE user_id = " + userID + " AND ticker ='" + ticker + "';");
							st.execute("UPDATE portfolio SET price = price + " + stockNum * stockPrice + " WHERE user_id = " + userID + " AND ticker ='" + ticker + "';");
						}
						else {
							//delete stock from portfolio if quantity 0
							st.execute("DELETE FROM portfolio WHERE user_id = " + userID + " AND ticker ='" + ticker + "';");
						}
					}
					else {
						newBalance = -1;
					}
				}
			}
			
			
		} catch (SQLException e) {
			System.out.println("SQLException in executeTrade.");
			e.printStackTrace();
		} finally {
			try {
				if (rs != null) {
					rs.close();
				}
				if (st != null) {
					st.close();
				}
				if (conn != null) {
					conn.close();
				}
			} catch (SQLException e) {
				System.out.println("SQL error: " + e.getMessage());
			}
		}
		return newBalance;
	}
	
	//function to get list of stocks in a user's portfolio
	public static ArrayList<Trade> getPortfolio(int userID) {
		try {
			Class.forName("com.mysql.cj.jdbc.Driver");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		
		ArrayList<Trade> portfolioList = new ArrayList<Trade>();
		
		try {
			conn = DriverManager.getConnection("jdbc:mysql://localhost/joesstocks?user=root&password=rigat0n1pA5tA");
			
			ps = conn.prepareStatement("SELECT ticker, numStock, price FROM portfolio WHERE user_id = ?");
			ps.setInt(1,  userID);
			rs = ps.executeQuery();
			while (rs.next()) {
				//get trade info and add it to portfolio list
				System.out.println(rs.getString("ticker"));
				Trade trade = new Trade();
				trade.setTicker(rs.getString("ticker"));
				trade.setStockNum(rs.getInt("numStock"));
				trade.setStockPrice(rs.getDouble("price"));
				trade.setUserID(userID);
				portfolioList.add(trade);
			}
		} catch (SQLException e) {
			System.out.println("SQLException in getPortfolio.");
			e.printStackTrace();
		} finally {
			try {
				if (rs != null) {
					rs.close();
				}
				if (ps != null) {
					ps.close();
				}
				if (conn != null) {
					conn.close();
				}
			} catch (SQLException e) {
				System.out.println("SQL error: " + e.getMessage());
			}
		}
		//return list
		return portfolioList;
	}
	
	//function to get user's balance using userID
	public static double getBalance(int userID) {
		try {
			Class.forName("com.mysql.cj.jdbc.Driver");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		
		Connection conn = null;
		java.sql.Statement st = null;
		ResultSet rs = null;
		
		double balance = -1;
		
		try {
			conn = DriverManager.getConnection("jdbc:mysql://localhost/joesstocks?user=root&password=rigat0n1pA5tA");
			
			st = conn.createStatement();
			rs = st.executeQuery("SELECT balance FROM users WHERE user_id='" + userID + "';");
			//if userID exists
			if (rs.next()) {
				//select balance
				balance = rs.getDouble("balance");
			}
		} catch (SQLException e) {
			System.out.println("SQLException in executeTrade.");
			e.printStackTrace();
		} finally {
			try {
				if (rs != null) {
					rs.close();
				}
				if (st != null) {
					st.close();
				}
				if (conn != null) {
					conn.close();
				}
			} catch (SQLException e) {
				System.out.println("SQL error: " + e.getMessage());
			}
		}
		return balance;
	}
}
