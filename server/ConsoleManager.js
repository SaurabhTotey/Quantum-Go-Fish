let IdentityManager = require("./IdentityManager");

//The sender name that the game will use to send messages
let defaultSender = "The Universe";

/**
 * Defines a class that contains all the fields a log message should have
 */
class LogMessage {
    constructor(message, sender, type) {
        this.timeStamp = new Date();
        this.message = message;
        this.sender = sender;
        this.type = type;
    }
}

/**
 * A function that returns an error log message
 */
function makeErrorMessage(errorMessage) {
    return new LogMessage(errorMessage, defaultSender, "ERROR");
}

/**
 * A function that returns an informational log message
 */
function makeInfoMessage(infoMessage) {
    return new LogMessage(infoMessage, defaultSender, "INFORMATION");
}

/**
 * Pushes a log message to all game participants from the game itself
 * Is for communicating game information generally
 */
function logGameMessage(logMessage, game) {
    //TODO: make
}

/**
 * Takes a message and a sender and sends the chat message to those who should be able to see it
 */
function pushChatMessage(chatMessage, senderId, isCommand) {
    let senderIdentity = IdentityManager.ids[senderId];
    let message = new LogMessage(chatMessage, senderIdentity.id, "CHAT" + (isCommand? "-COMMAND" : ""));
    if (senderIdentity.currentLobbyId != null) {
        let senderLobbyPlayerIds = IdentityManager.lobbies[senderIdentity.currentLobbyId].playerIds;
        for (let i = 0; i < senderLobbyPlayerIds.length; i++) {
            IdentityManager.ids[senderLobbyPlayerIds.playerIds[i]].log.push(message);
        }
    } else {
        senderIdentity.log.push(message);
    }
}

module.exports = {
    defaultSender: defaultSender,
    LogMessage: LogMessage,
    makeErrorMessage: makeErrorMessage,
    makeInfoMessage: makeInfoMessage,
    logGameMessage: logGameMessage,
    pushChatMessage: pushChatMessage
};
