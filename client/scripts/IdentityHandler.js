
//A global variable to communicate with the backend
let socket = io();

//The current ID of the user
let id = undefined;

/**
 * Receives the identity that the server has for this user from the socket
 */
socket.on("identity", incomingId => {
	id = incomingId;
});

window.onunload = () => socket.removeAllListeners();
