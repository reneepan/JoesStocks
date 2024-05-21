function parseFinnHub() {
    const ticker = document.getElementById('searchInput').value.trim();
    //check if ticker is valid
    if (!ticker) {
        alert('Ticker must not be empty');
        return;
    }
	
	//finnhub URLs
    const profileUrl = `https://finnhub.io/api/v1/stock/profile2?symbol=` + ticker + `&token=cnr5ob9r01qs2jr5h4ugcnr5ob9r01qs2jr5h4v0`;
    const quoteUrl = `https://finnhub.io/api/v1/quote?symbol=` + ticker + `&token=cnr5ob9r01qs2jr5h4ugcnr5ob9r01qs2jr5h4v0`;
    
    Promise.all([
        fetch(profileUrl).then(res => res.json()),
        fetch(quoteUrl).then(res => res.json())
    ])
    .then(([profileData, quoteData]) => {
		//update values of the HTML code
        document.getElementById('companyTicker').textContent = ticker.toUpperCase();
        document.getElementById('companyName').textContent = profileData.name;
        document.getElementById('companyExchange').textContent = profileData.exchange;
        document.getElementById('lastPrice').textContent = quoteData.c;
        document.getElementById('change').textContent = quoteData.d;
        document.getElementById('changePercent').textContent = quoteData.dp;
        document.getElementById('currentTimestamp').textContent = getCurrentDateTime();
        document.getElementById('highPrice').textContent = quoteData.h;
        document.getElementById('lowPrice').textContent = quoteData.l; 
        document.getElementById('openPrice').textContent = quoteData.o;
        document.getElementById('closePrice').textContent = quoteData.pc;
        document.getElementById('ipoDate').textContent = profileData.ipo;
        document.getElementById('marketCap').textContent = profileData.marketCapitalization;
        document.getElementById('sharesOutstanding').textContent = profileData.shareOutstanding;
        document.getElementById('companyWebsite').textContent = profileData.weburl;
        document.getElementById('companyPhone').textContent = profileData.phone;
        
        //if positive change in price
        if (quoteData.d < 0) {
			//change caret orientation and color
			document.getElementById('changeArrow').className = 'fa fa-caret-down';
			Array.from(document.getElementsByClassName('stockDetails')).forEach(element => {element.style.color = 'red'});
			document.getElementById('currentTimestamp').style.color = 'red';
		}
		//if negative change in price
		else if (quoteData.d > 0) {
			//change caret orientation and color
			document.getElementById('changeArrow').className = 'fa fa-caret-up';
			Array.from(document.getElementsByClassName('stockDetails')).forEach(element => {element.style.color = 'green'});
			document.getElementById('currentTimestamp').style.color = 'green';
		}
		else {
			//change color
			Array.from(document.getElementsByClassName('stockDetails')).forEach(element => {element.style.color = 'black'});
			document.getElementById('currentTimestamp').style.color = 'black';
		}
		
		//check if market is open
		if (isMarketOpen()) {
			//set market to open
			document.getElementById('market').textContent = 'open';
		}
		else {
			//set market to closed and hide timestamp
			document.getElementById('market').textContent = 'closed';
			document.getElementById('currentTimestamp').style.display = 'none';
		}
		
		//show the search results and hide the search bar
        document.getElementById('searchResults').style.display = 'block';
        document.getElementById('searchStocks').style.display = 'none';
    })
    .catch(error => {
        console.error('Error fetching stock data:', error);
        alert('Failed to fetch data. Please try again.');
    });
}

function checkLoginStatus() {
	const userID = localStorage.getItem('userID');
	
	//if there is a userID, that means a user is logged in
	if (userID) {
		//show user css/html lines and hide guest
		document.body.classList.remove('guest');
		document.body.classList.add('user');
		document.querySelectorAll('.user').forEach(element => element.style.display = 'block');
		document.querySelectorAll('.guest').forEach(element => element.style.display = 'none');		
	}
	//guest mode
	else {
		//show guest css/html lines and hide user
		document.body.classList.remove('user');
		document.body.classList.add('guest');
		document.querySelectorAll('.user').forEach(element => element.style.display = 'none');
		document.querySelectorAll('.guest').forEach(element => element.style.display = 'block');
	}
}

//check login status when page loads
document.addEventListener('DOMContentLoaded', checkLoginStatus);

//logout function
function logout() {
	//remove userID localstorage and reload page
	localStorage.removeItem('userID');
	window.location.href = 'index.html';
}

//current time for market timestamp
function getCurrentDateTime() {
  var now = new Date();

  var year = now.getFullYear();
  
  //add 1 because JavaScript months start at 0
  var month = (now.getMonth() + 1).toString().padStart(2, '0'); 
  var day = now.getDate().toString().padStart(2, '0');

  var hours = now.getHours().toString().padStart(2, '0');
  var minutes = now.getMinutes().toString().padStart(2, '0');
  var seconds = now.getSeconds().toString().padStart(2, '0');

  //format the date and time string
  var dateTimeString = month + '-' + day + '-' + year + ' ' + hours + ':' + minutes + ':' + seconds;

  return dateTimeString;
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

function buyStock() {
	//get stock info
	var stockNum = parseInt(document.getElementById('quantityInput').value);
	var stockPrice = parseFloat(document.getElementById('lastPrice').textContent);
	var ticker = document.getElementById('companyTicker').textContent;
	const userID = localStorage.getItem('userID');
	
	//check for errors
	if (!stockNum) {
		alert('Please enter a quantity');
		return;
	}
	else if (stockNum < 1) {
		alert('FAILED: Purchase not possible');
		return;
	}
	
	if (!isMarketOpen()) {
		alert('Market is Closed');
		return;
	}
	
	//connect to trade servlet to execute trade
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
		//check if the text is a number (the new balance)
	    if (!isNaN(text)) {
	    	//alert that stock was bought
	    	alert('SUCCESS: Bought ' + stockNum + ' shares of ' + ticker + ' for $' + (stockPrice * stockNum).toFixed(2));
	    } else {
			//if the text is not a number, it's an error message
	        throw new Error(text); 
	    }
	})
	.catch(error => {
	    alert('Error: ' + error.message);
	});
}

