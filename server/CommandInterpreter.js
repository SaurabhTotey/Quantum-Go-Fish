const IdentityManager = require("./IdentityManager");
const LobbyManager = require("./LobbyManager");
const Message = require("./Message");
const QGFGame = require("./QGFGame");

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
			throw "You cannot ready yourself to be in a game just by yourself! Join a lobby!";
		}
		lobby.readyPlayer(identity.id);
	},
	"ask": (identity, lobby, messageSendFunction, target, type) => {
		if (lobby == null || lobby.game == null) {
			throw "You can only ask questions in a game!";
		}
		if (lobby.game.phase !== QGFGame.GAME_PHASES.AWAITING_QUESTION) {
			throw "Questions cannot be asked yet!";
		}
		if (lobby.game.currentPlayer() !== identity.id) {
			throw `Only Player ${lobby.game.currentPlayer()} can ask a question right now!`;
		}
		let wrongTargetErrorMessage = "Target must be the ID of another player in this game!";
		if (isNaN(target)) {
			throw wrongTargetErrorMessage;
		}
		let targetId = parseInt(target);
		if (!lobby.playerIds.includes(targetId) || targetId === identity.id) {
			throw wrongTargetErrorMessage;
		}
		lobby.handleGameCommand(() => lobby.game.poseQuestion(targetId, type));
	},
	"answer": (identity, lobby, messageSendFunction, answer) => {
		if (lobby == null || lobby.game == null) {
			throw "You can only answer questions in a game!";
		}
		if (lobby.game.phase !== QGFGame.GAME_PHASES.AWAITING_QUESTION_RESPONSE) {
			throw "Questions cannot be answered yet!";
		}
		if (lobby.game.targetId !== identity.id) {
			throw `Only Player ${lobby.game.targetId} can ask a question right now!`;
		}
		if (answer === undefined || answer.length < 1 || !["y", "n", "t", "f"].includes(answer[0].toLowerCase())) {
			throw "Answers must come in a form that begins with y, n, t, or f [(Y)es, (N)o, (T)rue, (F)alse]."
		}
		lobby.handleGameCommand(() => lobby.game.answerQuestion(["y", "t"].includes(answer[0].toLowerCase())));
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
 * TODO: sanitize text and make sure no HTML script or iframe tags are included
 *     https://github.com/apostrophecms/sanitize-html
 */
module.exports.interpretText = (senderId, text, messageSendFunction) => {

	/*
	 * Commands can only start with "/"
	 * Text without a "/" in the beginning aren't commands
	 */
	if (text[0] !== "/") {
		return new InterpretTextResult("MESSAGE", () => {});
	}

	/*
	 * Gets the sender's ID and lobby
	 */
	let identity = IdentityManager.ids[senderId];
	let lobby = null;
	if (identity.currentLobbyId != null) {
		lobby = LobbyManager.lobbies[identity.currentLobbyId];
	}

	/*
	 * Parses the command into the actual command and its arguments
	 * Commands are "/commandName [arg1] [arg2] ..."
	 */
	let arguments = text.split(" ");
	let command = arguments[0].substring(1, arguments[0].length);

	/*
	 * Changes the arguments variable into the function arguments for all the command functions defined in commands
	 * First arguments are always identity, lobby, and messageSendFunction
	 * Remaining arguments are text provided by the user
	 */
	arguments = [identity, lobby, messageSendFunction].concat(arguments.slice(1, arguments.length));
	return new InterpretTextResult("COMMAND", () => {

		/*
		 * Sends a message that the command was invalid if the command doesn't exist
		 */
		if (!(command in commands)) {
			messageSendFunction(senderId, new Message.Message(Message.defaultSender, "ERROR", "Sorry, the inputted command was invalid."));
			return;
		}

		/*
		 * Tries running the command with the given arguments
		 * Commands may throw error messages
		 * If an error is thrown, the error message is sent out
		 */
		try {
			commands[command].apply(null, arguments);
		} catch (errorMessage) {
			messageSendFunction(senderId, new Message.Message(Message.defaultSender, "ERROR", errorMessage));
		}

	});

};
