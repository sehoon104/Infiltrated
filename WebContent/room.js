//receives "start game" command and stores session data.

var socket;
var isGuest = true;
var role = "HACKER"; //or AGENT
var phase = "MESSAGING"; //use for some logic in the javascript //messaging, voting.
var haveKilled = false;
var haveVoted = false;
var haveGuessed = false;
function connectToServer() {
	socket = new WebSocket("ws://localhost:8085/Final_Prototype/ws");
	socket.onopen = function(event) {
		document.getElementById("debug").innerHTML += "Connected!<br />";
	}
	socket.onmessage = function(event) {
		console.log("got a message");
		//new style of communication
		debug("RAW : " + event.data);
		var evinfo = event.data.split("*=");
		var command = evinfo[0];
		var data = evinfo[1];
		if (command == "SETNAME") {
			//set("name", data);
			localStorage.setItem("name", data);
		}
		else if (command == "START"){
			window.location.href = "game.html";
		}
		else if (command == "GETNAME") {
			socket.send("MYNAME*=" + localStorage.getItem("username"));//document.getElementById("name").innerHTML);
		}
		else if (command == "STATUSES") {
			setStatuses(data);
		}
		else {
			addTo("debug", data);
		}

	
	}
	socket.onclose = function(event) {
		document.getElementById("debug").innerHTML += "Disconnected!<br />";
	}
}

function set(pos, thing){
	document.getElementById(pos).innerHTML = thing;
}
function addTo(pos, thing){
	document.getElementById(pos).innerHTML += thing;
}
function debug(thing){
	addTo("debug", thing);
}

function setStatuses(data) {
	set("statuses", "");
	var dataspl = data.split(".=.");
	var secondspl;
	var i;
	for (i = 0; i < dataspl.length - 1; i++) {
		secondspl = dataspl[i].split("==");
		if (secondspl[1] == "true") {
			let li = document.createElement("li");
			li.innerHTML = secondspl[0];
			document.querySelector("#rm-pl-ol").appendChild(li);//addTo("statuses", "<li>" + secondspl[0] + " is alive." + "</li>");
		}
		else {
			let li = document.createElement("li");
			li.innerHTML = secondspl[0];
			document.querySelector("#rm-pl-ol").appendChild(li);
			//addTo("statuses", "<li>" + secondspl[0] + " is dead." + "</li>");
		}
	}
}

function startGame(){
	socket.send("START*= ");
}
