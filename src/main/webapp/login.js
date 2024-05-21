function submitLogin() {
	//get login info
    const username = document.getElementById('username');
    const password = document.getElementById('password');

	//connect to login servlet to access database
    fetch('LoginServlet', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(
			{username: username.value, 
			password: password.value})
    })
    .then(response => response.json())
	.then(data => {
		const userID = parseInt(data);
		//if number is returned, it is the userID
		if (Number.isInteger(userID)) {
			//set local storage
		    localStorage.setItem('userID', userID);
		    alert('Login successful.');
		    //send to index page
		    window.location.href = 'index.html';
		//if not a number, it is an error message
		} else {
		    alert("Error: " + data);
		}

	})
    .catch(error => {
        alert('Failed to connect: ' + error.message);
    });
}

function submitSignup() {
	//get registration info
    const username = document.getElementById('newUsername');
    const password = document.getElementById('newPassword');
    const confirmPassword = document.getElementById('confirmPassword');
    const email = document.getElementById('newEmail');
    
    //check if passwords match
    if (password.value !== confirmPassword.value) {
        alert('Passwords do not match');
        return;
    }
	
	//connect to register servlet to access database
    fetch('RegisterServlet', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ 
			username: username.value,
			password: password.value, 
			email: email.value })
    })
    .then(response => response.json())
	.then(data => {
		const userID = parseInt(data);
		//if data returned is an integer, it is the userID
		if (Number.isInteger(userID)) {
			//set local storage
		    localStorage.setItem('userID', userID);
		    alert('Registration successful.');
		    //send to index page
		    window.location.href = 'index.html';
		//if not an integer, it is an error message
		} else {
		    alert("Error: " + data);
		}

	})
    .catch(error => {
        alert('Failed to connect: ' + error.message);
    });
}

