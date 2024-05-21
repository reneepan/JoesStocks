document.addEventListener('DOMContentLoaded', getPortfolioData);

function getPortfolioData() {
    const userID = localStorage.getItem('userID');
    let accountBalance = 0;
    let totalAccountValue = 0;
    const portfolioDetails = document.querySelector('#portfolioDetails');
    //clear portfolio html once before appending all stock items
    portfolioDetails.innerHTML = ''; 

    //fetch account balance and portfolio details
    Promise.all([
        fetch('http://localhost:8080/reneepan_CSCI201_Assignment4/PortfolioServlet?userID=' + userID, {
            method: 'GET',
            headers: { 'Content-Type': 'application/json' },
        }),
        fetch('PortfolioServlet', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ userID: userID }),
        })
    ])
    .then(([balanceResponse, portfolioResponse]) => Promise.all([balanceResponse.json(), portfolioResponse.json()]))
    .then(([balanceData, portfolioData]) => {
		//set account balance details 
        accountBalance = parseFloat(balanceData);
        totalAccountValue = accountBalance;
        document.getElementById('cashBalance').textContent = accountBalance.toFixed(2);
        //check if porfolio data is empty 
        if (Array.isArray(portfolioData) && portfolioData.length === 0) {
			document.getElementById('totalValue').textContent = totalAccountValue.toFixed(2);
			return Promise.reject(new Error('You have nothing in your portfolio.'));
		}

        // Map each trade to a promise that resolves to its HTML
        const stockItemPromises = portfolioData.map(trade => {
            const profileUrl = `https://finnhub.io/api/v1/stock/profile2?symbol=` + trade.ticker + `&token=cnr5ob9r01qs2jr5h4ugcnr5ob9r01qs2jr5h4v0`;
            const quoteUrl = `https://finnhub.io/api/v1/quote?symbol=` + trade.ticker + `&token=cnr5ob9r01qs2jr5h4ugcnr5ob9r01qs2jr5h4v0`;

            return Promise.all([
                fetch(profileUrl).then(res => res.json()),
                fetch(quoteUrl).then(res => res.json())
            ])
            .then(([profileData, quoteData]) => {
				//check change positive or negative
				var changeColor = '';
				var arrowClass = '';
				//set change color and arrow orientation accordingly
				if (quoteData.d < 0) {
					changeColor = 'red';
					arrowClass = 'fa-caret-down';
				}
				else if (quoteData.d > 0) {
					changeColor = 'green';
					arrowClass = 'fa-caret-up';
				}
				//if no change, set to black with no arrow
				else {
					changeColor = 'black';
				}
				//update total account value with each trade
                totalAccountValue += quoteData.c * trade.stockNum;

                //create stock item and dynamically load in the html info
                const stockItem = document.createElement('div');
                stockItem.className = 'stock-item';
                stockItem.innerHTML = `
                    <div class="stock-header">${trade.ticker} - ${profileData.name}</div>
		                <div class="stock-body">
		                    <div class="stock-info">
		                        <p>Quantity: <span class="quantity">${trade.stockNum}</span></p>
		                        <p>Avg. Cost / Share: <span class="avg-cost">${(trade.stockPrice / trade.stockNum).toFixed(2)}</span></p>
		                        <p>Total Cost: <span class="total-cost">${(trade.stockPrice).toFixed(2)}</span></p>
		                    </div>
		                    <div class="stock-transaction">
		                        <p>Change: <i id = "changeArrow" class ="fa ${arrowClass}" style="color:${changeColor};"></i><span class="change">${quoteData.d.toFixed(2)}</span></p>
		                        <p>Current Price: <span class="current-price">${quoteData.c.toFixed(2)}</span></p>
		                        <p>Market Value: <span class="market-value">${(quoteData.c * trade.stockNum).toFixed(2)}</span></p>
		                    </div>
		                </div>
		                <form class="transaction-form" onsubmit="processTransaction(event, '${trade.ticker}', ${quoteData.c}, ${trade.stockNum})">
					            <label>Quantity:<input type="number" class="quantity-input" name="quantity"></label>
					            <div class="radioButtons">
					               	<label><input type="radio" name="transactionType-${trade.ticker}" value="BUY" checked> Buy</label>
					                <label><input type="radio" name="transactionType-${trade.ticker}" value="SELL"> Sell</label>
					            </div>
					            <button type="submit" class="submit-button">Submit</button> 
					    </form>
                `;
                //set the color of the change
                stockItem.querySelector('.change').style.color = changeColor;
                
                return stockItem;
            });
        });

        //wait for all stock item promises to resolve
        return Promise.all(stockItemPromises);
    })
    .then(stockItems => {
        //append all stock items to the html document div
        const fragment = document.createDocumentFragment();
        stockItems.forEach(item => fragment.appendChild(item));
        portfolioDetails.appendChild(fragment);

        //ppdate the total account value
        document.getElementById('totalValue').textContent = totalAccountValue.toFixed(2);
    })
    .catch(error => {
        alert(error.message);
    });
}

//process buy/sell
function processTransaction(event, ticker, stockPrice, stockTot) {
	event.preventDefault();
	
	//get form information
	const userID = localStorage.getItem('userID');
	const form = event.target;
	const transactionType = form.querySelector(`input[name="transactionType-${ticker}"]:checked`).value;
    var stockNum = parseInt(form.querySelector(`input[name="quantity"]`).value);
    //if no quantity
    if (isNaN(stockNum)) {
		alert('Transaction not possible. Please enter a quantity.');
		return;
	}
	//if market is closed
	if (!isMarketOpen()) {
		alert('Market is closed.');
		return;
	}
	//negative stock 
	else if (stockNum < 0) {
		alert('FAILED: transaction not possible');
		return;
	}
	//check when selling stock
    if (transactionType === 'SELL') {
		//if trying to sell more stock than the user has
		if (stockNum > stockTot) {
			alert('FAILED: transaction not possible');
			return;
		}
		//turn stock to negative for trade function in trade servlet
		stockNum = -1 * stockNum;
	}
	
	//send to trade servlet for connection to database
	fetch('TradeServlet', {
		method: 'POST',
		headers: {
			'Content-Type': 'application/json',
		},
		body: JSON.stringify({
			userID: userID,
			stockPrice: stockPrice,
			stockNum: stockNum,
			ticker: ticker
		}),
	})
	.then(response => response.text())
	.then(text => {
		//check if the text is a number (balance)
	    if (!isNaN(text)) {
			//buy message
	        if (stockNum > 0) {
				//alert and reload page for updated information
				alert('SUCCESS: Bought ' + stockNum + ' ' + ticker + ' stock(s)');
				window.location.href = 'portfolio.html';
			}
			//sell message
			else {
				//alert and reload page for updated information
				alert('SUCCESS: Sold ' + -1 * stockNum + ' ' + ticker + ' stock(s)');
				window.location.href = 'portfolio.html';
			}
		//if the text is not a number, it's an error message
	    } else {
	        throw new Error(text); 
	    }
	})
	.catch(error => {
	    alert('Error: ' + error.message);
	});
}

//check if market is open
function isMarketOpen() {
	var now = new Date();
	var hour = now.getHours();
	var min = now.getMinutes();
	
	var currTime = hour + (min/60);
	if (currTime >= 6.5 && currTime < 13) {
		return true;
	}
	else {
		return false;
	}
}

//logout function
function logout() {
	//remove local storage and take to index page
	localStorage.removeItem('userID');
	window.location.href = 'index.html';
}