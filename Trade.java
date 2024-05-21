package reneepan_CSCI201_Assignment4;

//trade class
public class Trade {
	private String ticker;
	private int stockNum;
	private double stockPrice;
	private int userID;
	
	public String getTicker() {
		return ticker;
	}
	
	public void setTicker(String ticker) {
		this.ticker = ticker;
	}
	
	public int getStockNum() {
		return stockNum;
	}
	
	public void setStockNum(int stockNum) {
		this.stockNum = stockNum;
	}
	
	public double getStockPrice() {
		return stockPrice;
	}
	
	public void setStockPrice(double stockPrice) {
		this.stockPrice = stockPrice;
	}
	
	public int getUserID() {
		return userID;
	}
	
	public void setUserID(int userID) {
		this.userID = userID;
	}
}
