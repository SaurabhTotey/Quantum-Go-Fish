
const defaultSender = "The Universe";

/**
 * A class that represents a message
 * Frontend accepts these messages and displays them nicely
 */
class Message {
    constructor(sender, type, contents) {
        let now = new Date();
        this.timeStamp = `${now.getUTCHours()}:${now.getUTCMinutes()}:${now.getUTCSeconds()}`;
        this.sender = sender;
        this.type = type;
        this.contents = contents;
    }
}

module.exports = {
    defaultSender: defaultSender,
    Message: Message
};
