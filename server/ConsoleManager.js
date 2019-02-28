const CommandInterpreter = require("./CommandInterpreter");
const IdentityManager = require("./IdentityManager");
const LobbyManager = require("./LobbyManager");
const Message = require("./Message");

/**
 * Handles sending a message text from a user
 * Routes the message to all necessary other users and interprets commands as necessary
 */
function handleUserMessage(id, text) {

    /*
     * Turns the given text into the appropriate command
     */
    let command = CommandInterpreter.interpretText(id, text, sendMessageTo);
    let message = new Message.Message(id, "CHAT-" + command.textType, text);

    sendMessageTo(id, message);

    /*
     * Runs the command specified by the text
     */
    command.textAction();

}

/**
 * Sends a message object to a single ID
 * Does absolutely no extra routing
 */
function sendMessageToIndividual(id, message) {
    let identity = IdentityManager.ids[id];
    identity.log.push(message);
    identity.sendUpdates();
}

/**
 * Sends the given message object to id
 * Handles routing the message object to those who should see it
 */
function sendMessageTo(id, message) {
    let lobbyId = IdentityManager.ids[id].currentLobbyId;
    if (lobbyId == null) {
        sendMessageToIndividual(id, message);
    } else {
        sendMessageToLobby(lobbyId, message);
    }
}

/**
 * Sends the given message to all users in the lobby
 * Expects message to be a message object
 */
function sendMessageToLobby(lobbyId, message) {
    let lobbyPlayerIds = LobbyManager.lobbies[lobbyId].playerIds;
    for (let i = 0; i < lobbyPlayerIds.length; i++) {
        let playerId = lobbyPlayerIds[i];
        sendMessageToIndividual(playerId, message);
    }
}

module.exports = {
    handleUserMessage: handleUserMessage
};
