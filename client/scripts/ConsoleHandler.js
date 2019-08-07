
let log = document.getElementById("log");
let input = document.getElementById("user-text-input");

/**
 * Handles receiving messages and appending them to the log
 */
socket.on("message", messageLog => {
	log.innerHTML = "";
	for (let i = 0; i < messageLog.length; i++) {
		let message = messageLog[i];
		log.innerHTML += `<p>[<span class="timestamp">${message.timeStamp}</span>, <span class="sender">${message.sender}</span>]: <span class="${message.type}">${message.contents}</span></p>`;
	}
	log.scrollTop = log.scrollHeight;
});

/**
 * When the submit button is clicked or when enter is typed, the user's input is submitted and the input is cleared
 */
let submitMethod = () => {
	if (input.value === "") {
		return;
	}
	socket.emit("message", input.value);
	input.value = "";
	input.focus();
};
document.getElementById("user-submit-button").onclick = submitMethod;
input.onkeyup = keyEvent => {
	if (keyEvent.key === "Enter") {
		submitMethod();
	}
};
