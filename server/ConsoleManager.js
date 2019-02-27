const IdentityManager = require("./IdentityManager");
const LobbyManager = require("./LobbyManager");
const Message = require("./Message");

/**
 * Returns an object with an action and the type of command it interpreted
 */
function interpretCommand(senderId, text) {
    if (text[0] === "/") {
        let action = () => sendMessageTo(senderId, new Message.Message(Message.defaultSender, "ERROR", "Sorry, the inputted command was invalid."));
        try {
            let arguments = text.split(" ");
            if (arguments[0] === "/join") {
                if (!isNaN(arguments[1])) {
                    action = () => {
                        let success = LobbyManager.join(senderId, parseInt(arguments[1]));
                        if (!success) {
                            sendMessageTo(senderId, new Message.Message(Message.defaultSender, "ERROR", "Sorry, I was unable to join you to that player's lobby."));
                        }
                    };
                } else if (arguments[1] === "any") {
                    action = () => LobbyManager.matchMake(senderId);
                } else {
                    action = () => sendMessageTo(senderId, new Message.Message(Message.defaultSender, "ERROR", "You must tell me where I should join you. Do '/join [playerId]' to join a specific player, '/join any' to join any lobby, or '/create' to make a lobby."));
                }
            } else if (arguments[0] === "/create") {
                action = () => LobbyManager.createLobby(senderId);
            } else if (arguments[0] === "/leave") {
                action = () => {
                    let lobbyId = IdentityManager.ids[senderId].currentLobbyId;
                    if (lobbyId != null) {
                        LobbyManager.lobbies[lobbyId].removePlayer(senderId);
                    } else {
                        sendMessageTo(senderId, new Message.Message(Message.defaultSender, "ERROR", "You are not in a lobby right now."));
                    }
                };
            }
        } catch (ignored) {}
        return {
            action: action,
            type: "COMMAND"
        };
    }
    return {
        action: () => {},
        type: "MESSAGE"
    };
}

/**
 * Handles sending a message text from a user
 * Routes the message to all necessary other users and interprets commands as necessary
 */
function handleUserMessage(id, text) {

    /*
     * Turns the given text into the appropriate command
     */
    let command = interpretCommand(id, text);
    let message = new Message.Message(id, "CHAT-" + command.type, text);

    sendMessageTo(id, message);

    /*
     * Runs the command specified by the text
     */
    command.action();

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
