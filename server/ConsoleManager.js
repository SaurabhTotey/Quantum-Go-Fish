const IdentityManager = require("./IdentityManager");
const LobbyManager = require("./LobbyManager");
const Message = require("./Message");

/**
 * Returns a function that runs the command specified by the text
 * A null return means the text wasn't a command
 */
function interpretCommand(senderId, text) {
    //TODO: figure out
    return null;
}

/**
 * Handles sending a message from a user
 * Routes the message to all necessary other users and interprets commands as necessary
 */
function sendUserMessage(id, text) {

    let command = interpretCommand(id, text);
    let message = new Message.Message(id, "CHAT-" + (command == null ? "MESSAGE" : "COMMAND"), text);

    /*
     * Sends the message to those who should see it
     */
    let lobby = IdentityManager.ids[id].currentLobbyId;
    if (lobby == null) {
        IdentityManager.ids[id].log.push(message);
        IdentityManager.ids[id].sendUpdates();
    } else {
        sendMessageToLobby(lobby, message);
    }

    /*
     * Runs the command specified by the text
     */
    if (command != null) {
        command();
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
        IdentityManager.ids[playerId].log.push(message);
        IdentityManager.ids[playerId].sendUpdates();
    }
}

module.exports = {
    sendUserMessage: sendUserMessage,
    sendMessageToLobby: sendMessageToLobby
};
