const IdentityManager = require("./IdentityManager");
const LobbyManager = require("./LobbyManager");
const Message = require("./Message");

/*
 * A map of command strings to their corresponding actions
 * Arguments of identity, lobby, and message sending function are automatically passed in
 * Further arguments are supplied by the user and are separated by spaces in their string
 */
const commands = {
    "join": (identity, lobby, messageSendFunction, target) => {
        if (lobby != null) {
            throw "You are already in a lobby!";
        }
        if (target === "any") {
            LobbyManager.matchMake(identity.id);
        } else if (!isNaN(target)) {
            let targetId = parseInt(target);
            if (!(targetId in IdentityManager.ids)) {
                throw `User ${targetId} doesn't exist!`;
            }
            if (!LobbyManager.join(identity.id, targetId)) {
                throw `You are unable to join the lobby of ${targetId} at this time.`;
            }
        } else {
            throw "Sorry, the command can only be called like '/join any' to join the best available lobby or '/join [playerId]' to join the lobby of an already lobbied player.";
        }
    },
    "create": (identity, lobby) => {
        if (lobby != null) {
            throw "You are already in a lobby!";
        }
        LobbyManager.createLobby(identity.id);
    },
    "leave": (identity, lobby) => {
        if (lobby == null) {
            throw "You cannot leave a lobby if you are not in a lobby!";
        }
        lobby.removePlayer(identity.id);
    },
    "ready": (identity, lobby) => {
        if (lobby == null) {
            throw "You cannot ready yourself to be in a game just by yourself! Join a lobby!"
        }
        lobby.readyPlayer(identity.id);
    }
};

/**
 * A class that represents how text was interpreted
 */
class InterpretTextResult {
    constructor(textType, textAction) {
        this.textType = textType;
        this.textAction = textAction;
    }
}

/**
 * The only exposed function of this file is the main function that interprets text as commands
 * Returns whether the action that the text should trigger and the type of how the text was interpreted
 */
module.exports.interpretText = (senderId, text, messageSendFunction) => {
    if (text[0] !== "/") {
        return new InterpretTextResult("MESSAGE", () => {});
    }
    let identity = IdentityManager.ids[senderId];
    let lobby = null;
    if (identity.currentLobbyId != null) {
        lobby = LobbyManager.lobbies[identity.currentLobbyId];
    }
    let arguments = text.split(" ");
    let command = arguments[0].substring(1, arguments[0].length);
    arguments = [identity, lobby, messageSendFunction].concat(arguments.slice(1, arguments.length));
    return new InterpretTextResult("COMMAND", () => {
        if (!(command in commands)) {
            messageSendFunction(senderId, new Message.Message(Message.defaultSender, "ERROR", "Sorry, the inputted command was invalid."));
            return;
        }
        try {
            commands[command].apply(null, arguments);
        } catch (errorMessage) {
            messageSendFunction(senderId, new Message.Message(Message.defaultSender, "ERROR", errorMessage));
        }
    });
};
