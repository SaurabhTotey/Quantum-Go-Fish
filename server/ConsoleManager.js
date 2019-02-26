const IdentityManager = require("./IdentityManager");
const LobbyManager = require("./LobbyManager");
const Message = require("./Message");

/**
 * Returns an object with an action and the type of command it interpreted
 */
function interpretCommand(senderId, text) {
    let isNumber = text => {
        try {
            return typeof(parseInt(text)) === "number";
        } catch (ignored) {
            return false;
        }
    };
    if (text[0] === "/") {
        let action = () => sendMessageTo(senderId, new Message.Message(Message.defaultSender, "ERROR", "Sorry, the inputted command was invalid."));
        try {
            let arguments = text.split(" ");
            if (arguments[0] === "/join") {
                if (isNumber(arguments[1])) {
                    //TODO: join the lobby of the person specified by arguments[1]
                } else if (arguments[1] === "any") {
                    //TODO: join any lobby
                }
            } else if (arguments[0] === "/create") {
                //TODO: create a lobby
            } else if (arguments[0] === "/clear") {
                action = () => {
                    IdentityManager.ids[senderId].log = [];
                    IdentityManager.ids[senderId].sendUpdates();
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
