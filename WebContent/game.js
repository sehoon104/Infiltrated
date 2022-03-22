var socket;
var isGuest = true;//localStorage.getItem("loggedin");//true;//temp
var role = "UA"; //or AGENT
var phase = "MESSAGING"; //use for some logic in the javascript //messaging, voting.
var haveKilled = false;
var haveVoted = false;
var haveGuessed = false;
function connectToServer() {
	socket = new WebSocket("wss://aee1e67ca829.ngrok.io/Final_Prototype/ws");
	//socket = new WebSocket("ws://localhost:8085/Final_Prototype/ws");
	//http://7efb5d142a11.ngrok.io/Final_Prototype/login.html

	socket.onopen = function(event) {
		document.getElementById("chat").innerHTML += "Connected!<br />";
	}
	socket.onmessage = function(event) {
		//new style of communication
		debug("RAW : " + event.data);
		var evinfo = event.data.split("*=");
		var command = evinfo[0];
		var data = evinfo[1];
		if (command == "TEXT") {
			addTo("chat-log", data);
		}
		else if (command == "TIME") {
			set("time", data);
		}
		else if (command == "SETNAME") {
			console.log("USERNAME + " + localStorage.getItem("username"));
			if (localStorage.getItem("username") == null){
				set("name", data);
				localStorage.setItem("guestign", data);
			}
			else {
				set("name", localStorage.getItem("username"));
				sendName();
			}	
		}
		else if (command == "GETNAME") {
			socket.send("MYNAME*=" + document.getElementById("name").innerHTML);
		}
		else if (command == "KILLOPTIONS") { //handle whether the client ever receives this on the server side.
			setKillForm(data);
		}
		else if (command == "VOTEOPTIONS") {
			setVoteForm(data);
		}
		else if (command == "KILL") {
			//socket.close();
			die();
		}
		else if (command == "STATUSES") {
			setStatuses(data);
		}
		else if (command == "ROLE") {
			localStorage.setItem("role", data);
			role = data;
			debug(data);
		}
		else if (command == "LOC") {
			set("specloc", data);
		}
		else if (command == "WIPE") {
			console.log("WIPING " + data);
			set(data, "");
		}
		else if (command == "PHASE") {
			phase = data;
			set("phase", data);
			//reset kill
			haveKilled = false;
			//reset vote
			haveVoted = false;
			//reset guess
			haveGuessed = false;
		}
		else if (command == "LOCATIONOPTIONS") {
			debug("got locs");
			setGuessForm(data);
		}
		else if (command == "WIN") {
			console.log("Just won.");
			socket.close();
			if (role == "HACKER") {
				winHacker();
			}
			else {
				winAgent();
			}
		}
		else if (command == "LOSS"){
			console.log("Just won.");
			socket.close();
			if (role == "HACKER") {
				loseHacker();
			}
			else {
				loseAgent();
			}
		}
		else {
			console.log(data);
			//addTo("debug", data);
		}
		//also have a command to rebuild the html for win/loss screens

		//other junk to blank out parts of the html
		console.log(role);
		if (role == "AGENT") {
			set("murder-card", "");
			set("location-cards", "");
			set("role", "AGENT");
		}
		else if (role == "HACKER"){
			set("role", "HACKER");
			set("location", "");
		}

		//if guest, blank name form
		if (localStorage.getItem("username") == null) {
			//set("nameform", "");
			set("loggedin", "Not logged in.");
		}
		else {
			set("loggedin", "Logged in.");
			set("name", localStorage.getItem("username"));
		}
	}
	socket.onclose = function(event) {
		console.log("Disconnecting");
		//document.getElementById("chat").innerHTML += "Disconnected!<br />";
	}
}
function die() {
	console.log("DYING");
	window.localStorage.setItem("role", role);
	window.localStorage.setItem("win", "false");
	//if (window.localStorage.getItem("username") == null){
		//window.localStorage.setItem("guestign", )
	//}
	window.location.href = "results.html";
	//$("body").html('<object data="results.html"/>');
	//$("results").load("results.html");
	//$("allelse").hide();
	//onLoad();
	//document.write("<div class='endscreen'>You have been compromised!</div>");
}
function winAgent() {
	window.localStorage.setItem("role", role);
	window.localStorage.setItem("win", "true");
	window.location.href = "results.html";
	/*if (localStorage.getItem("username") != null){
		document.write("<div class='endscreen'>Congratulations, Agent " + + get("name") + ". The Hacker has been neutralized!</div>");
	}
	else{
		document.write("<div class='endscreen'>Congratulations, Agent. The Hacker has been neutralized!</div>");
	}*/
}
function winHacker() {
	window.localStorage.setItem("role", role);
	window.localStorage.setItem("win", "true");
	window.location.href = "results.html";
	//document.write("<div class='endscreen'>Congratulations, Hacker, the Agency's information has been stolen!</div>");
}
function loseAgent() {
	window.localStorage.setItem("role", role);
	window.localStorage.setItem("win", "false");
	window.location.href = "results.html";
	/*if (localStorage.getItem("username") != null){
		document.write("<div class='endscreen'>Bad news, Agent " + + get("name") + ". We have failed our mission. The information is in the Hacker's hands.'</div>");
	}
	else{
		document.write("<div class='endscreen'>Bad news, Agent. We have failed our mission. The information is in the Hacker's hands.'</div>");
	}*/
}
function loseHacker() {
	window.localStorage.setItem("role", role);
	window.localStorage.setItem("win", "false");
	window.location.href = "results.html";
	//document.write("<div class='endscreen'>Disappointing display, Hacker. You've been found out.'</div>");
}

function setKillForm(data) {
	//wipe form
	var select = document.getElementById("murder-select");
	var length = select.options.length;
	for (i = length - 1; i >= 0; i--) {
		select.options[i] = null;
	}
	//rebuild form
	var dataspl = data.split(".=.");
	var i;
	//the minus one is dumb but it works 
	for (i = 0; i < dataspl.length - 1; i++) {
		addToSelect("murder-select", dataspl[i]);
	}
}
function setVoteForm(data) {
	//wipe form
	var select = document.getElementById("vote-select");
	var length = select.options.length;
	for (i = length - 1; i >= 0; i--) {
		select.options[i] = null;
	}
	//rebuild form
	var dataspl = data.split(".=.");
	var i;
	//the minus one is dumb but it works 
	for (i = 0; i < dataspl.length - 1; i++) {
		addToSelect("vote-select", dataspl[i]);
	}
}
function setGuessForm(data) {
	//wipe form
	var select = document.getElementById("guessselect");
	var length = select.options.length;
	for (i = length - 1; i >= 0; i--) {
		select.options[i] = null;
	}
	//rebuild form
	var dataspl = data.split(".=.");
	var i;
	//the minus one is dumb but it works 
	for (i = 0; i < dataspl.length - 1; i++) {
		addToSelect("guessselect", dataspl[i]);
	}
}
function setStatuses(data) {
	set("statuses", "");
	var dataspl = data.split(".=.");
	var secondspl;
	var i;
	for (i = 0; i < dataspl.length - 1; i++) {
		secondspl = dataspl[i].split("==");
		if (secondspl[1] == "true") {
			addTo("statuses", "<li>" + secondspl[0] + " is alive." + "</li>");
		}
		else {
			addTo("statuses", "<li>" + secondspl[0] + " is dead." + "</li>");
		}
	}
}
function addToSelect(form, data) {
	// get reference to select element
	var sel = document.getElementById(form);

	// create new option element
	var opt = document.createElement("option");

	// create text node to add to option element (opt)
	opt.appendChild(document.createTextNode(data));

	// set value property of opt
	opt.value = data;

	// add opt to end of select box (sel)
	sel.appendChild(opt);
}
function sendChat() {
	//socket.sendCommand("TEXT", document.chatform.message.value);
	//return false;
	debug("sending chat");
	socket.send("TEXT\*=" + document.getElementById("msg-in").value);
	//blank out msg-in
	document.getElementById("msg-in").value = "";
	//socket.send(document.getElementById("name").innerHTML + "*=" + document.chatform.message.value);
	return false;
}
function sendName() {
	console.log("sending name" + localStorage.getItem("username"));
	socket.send("MYNAME\*=" + localStorage.getItem("username"));//document.nameform.name.value);
	//also changes name at top
	document.getElementById("name").innerHTML = document.nameform.name.value;
	return false;
}
function sendKill() {
	//can only kill during messaging phase
	var select = document.getElementById("murder-select");
	if (!haveKilled && phase == "MESSAGING" && select.options.length > 0) {
		debug("sending kill");
		var select = document.getElementById("murder-select");
		var chosenoption = select.options[select.selectedIndex].value;
		haveKilled = true;
		socket.send("KILL\*=" + chosenoption);
		alert(chosenoption + " has been neutralized."); //can remove later if have a bit more time
	}
	else if (haveKilled) {
		alert("You can only murder one agent per round!");
	}
	else if (phase != "MESSAGING") {
		alert("You can only murder agents during the messaging phase!");
	}
	return false;
}
function sendVote() {
	//can only vote during voting phase
	var select = document.getElementById("vote-select");
	if (!haveVoted && phase == "VOTING" && select.options.length > 0) {
		debug("sending vote");
		var select = document.getElementById("vote-select");
		var chosenoption = select.options[select.selectedIndex].value;
		haveVoted = true;
		socket.send("VOTE\*=" + chosenoption);
		alert("You voted for " + chosenoption + "."); //can remove later if have a bit more time
	}
	else if (haveVoted) {
		alert("You can only vote once per round!");
	}
	else if (phase != "VOTING") {
		alert("You can only vote during the voting phase!");
	}
	return false;
}
//need to be limited
function sendGuess() {
	//can only guess during messaging phase
	var select = document.getElementById("guessselect");
	if (!haveGuessed && phase == "MESSAGING" && select.options.length > 0) {
		var select = document.getElementById("guessselect");
		var chosenoption = select.options[select.selectedIndex].value;
		hasGuessed = true;
		socket.send("GUESS\*=" + chosenoption);
		alert("You guessed " + chosenoption + "."); //can remove later if have a bit more time
	}
	else if (haveGuessed) {
		alert("You can only guess once per round!");
	}
	else if (phase != "MESSAGING") {
		alert("You can only guess during the messaging phase!");
	}
	return false;
}
//guessing when a button is clicked
function sendGuess2(guess) {
	//can only guess during messaging phase
	//var select = document.getElementById( "guessselect" );
	if (!haveGuessed && phase == "MESSAGING") {
		//var select = document.getElementById( "guessselect" );
		//var chosenoption = select.options[ select.selectedIndex ].value;
		console.log(guess);
		haveGuessed = true;
		socket.send("GUESS\*=" + guess);
		alert("You guessed " + guess + "."); //can remove later if have a bit more time
	}
	else if (haveGuessed) {
		alert("You can only guess once per round!");
	}
	else if (phase != "MESSAGING") {
		alert("You can only guess during the messaging phase!");
	}
	return false;
}
//currently busted for some reason
function sendCommand(command, data) {
	socket.send(command + "*=" + data);  //document.getElementById("name") + "*=" + 
	//return false;
}
function set(position, data) {
	document.getElementById(position).innerHTML = data;
}
function get(position) {
	return document.getElementById(position).innerHTML;
}
function addTo(position, data) {
	document.getElementById(position).innerHTML += data;
	document.getElementById(position).innerHTML += "<br>";
}
function debug(s) {
	return;
}