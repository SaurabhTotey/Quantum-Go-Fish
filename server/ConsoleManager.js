
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
 * Pushes a log message to all game participants
 */
function logGameMessage(logMessage, game) {
    //TODO: make
}

module.exports = {
    defaultSender: defaultSender,
    LogMessage: LogMessage,
    makeErrorMessage: makeErrorMessage,
    makeInfoMessage: makeInfoMessage,
    logGameMessage: logGameMessage
};
