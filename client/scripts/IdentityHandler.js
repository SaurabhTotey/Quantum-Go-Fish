
//A global variable to communicate with the backend
let socket = io();

//The current ID and password of the user
let id = undefined;
let password = undefined;

/**
 * Receives the identity that the server has for this user from the socket
 */
socket.on("identity", identityObjectString => {
    let identity = JSON.parse(identityObjectString);
    id = identity.id;
    password = identity.password;
});
