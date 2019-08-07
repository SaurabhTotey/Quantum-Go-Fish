const IdentityManager = require("./IdentityManager");
const Message = require("./Message");
const QGFGame = require("./QGFGame");

//A function that sends a message to an entire lobby: initialized from ConsoleManager in Lobby constructor
let lobbyMessageFunction;

/**
 * A class that represents a lobby
 * A lobby is a list of players waiting for/in a game
 */
class Lobby {

	/**
	 * Makes a lobby with the given id
	 * Lobby IDs are separate from player IDs
	 * Lobby IDs are the position of the lobby in the list of all lobbies
	 */
	constructor(lobbyId) {

		this.lobbyId = lobbyId;
		this.playerIds = [];
		this.isJoinable = true;
		this.game = null;
		this.nonReadyPlayers = [];

		/*
		 * Takes the lobby message function from the Console Manager
		 * ConsoleManager is not a permanent dependency because it would then cause a cyclic dependency
		 * Therefore, the lobby message function needs to be initialized in a time afterward, so lazy initialization is used
		 * Message function is only loaded after the first lobby is made
		 */
		if (lobbyMessageFunction === undefined) {
			lobbyMessageFunction = require("./ConsoleRouter").sendMessageToLobby;
		}

	}

	/**
	 * Adds the player with the given ID to this lobby
	 */
	addPlayer(id) {
		if (this.playerIds.includes(id)) {
			throw `PLAYER ${id} ALREADY EXISTS IN THIS LOBBY WITH ${this.playerIds}.`;
		}
		this.playerIds.push(id);
		this.nonReadyPlayers.push(id);
		IdentityManager.ids[id].currentLobbyId = this.lobbyId;
		IdentityManager.ids[id].log = [];
		this.message(`Please welcome Player ${id} to lobby ${this.lobbyId}! The players currently in this lobby are ${this.playerIds}.`);
		if (this.playerIds.length > 1) {
			this.message("Now that there are multiple players in this lobby, a game of Quantum Go Fish will start when all players mark themselves as ready with '/ready'.");
		}
	}

	/**
	 * Removes the player with the given ID from this lobby
	 * Players leaving will mark all players as unready and stop any ongoing games
	 */
	removePlayer(id) {
		IdentityManager.ids[id].currentLobbyId = null;
		IdentityManager.ids[id].log = [];
		this.playerIds.splice(this.playerIds.indexOf(id), 1);
		this.nonReadyPlayers = this.playerIds;
		this.isJoinable = true;
		this.game = null;
		this.message(`Player ${id} has just left this lobby. The players currently in this lobby are ${this.playerIds}. Any ongoing games of Quantum Go Fish in this lobby have been ended and all players have been marked unready.`);
	}

	/**
	 * Marks the player of the given id as ready to play a game
	 * Game will start when all players have been marked as ready
	 */
	readyPlayer(id) {
		if (this.playerIds.length === 1) {
			throw `Please wait for at least one other player to join the lobby before readying up.`;
		}
		if (!this.nonReadyPlayers.includes(id)) {
			throw `Player ${id} is already ready!`;
		}
		this.nonReadyPlayers.splice(this.nonReadyPlayers.indexOf(id), 1);
		if (this.nonReadyPlayers.length > 0) {
			this.message(`Player ${id} is now ready to play Quantum Go Fish! Before the game starts, ${this.nonReadyPlayers} need to ready up.`);
		} else {
			this.startGame();
		}
	}

	/**
	 * Sends the given text as a message to all players in this lobby
	 */
	message(text, type = "INFO") {
		lobbyMessageFunction(this.lobbyId, new Message.Message(Message.defaultSender, type, text));
	}

	/**
	 * Makes the lobby enter a game of QGF
	 */
	startGame() {
		this.isJoinable = false;
		for (let i = 0; i < this.playerIds.length; i++) {
			IdentityManager.ids[this.playerIds[i]].log = [];
		}
		this.message(`Everyone in the lobby has readied up!`);
		this.game = new QGFGame.QFGGame(this);
	}

	/**
	 * Runs a game command and manages outputs accordingly
	 * gameCommand should return true or false: true means game will continue and false will mean the game is over because it is invalid
	 */
	handleGameCommand(gameCommand) {

		/*
		 * Checks if the game is valid and ends it if necessary
		 */
		if (!gameCommand()) {
			this.game.phase = QGFGame.GAME_PHASES.DONE;
			this.message("The game has entered an impossible state. Everyone has lost 😢.", "GAME");
		}

		/*
		 * Checks if the game has a winner
		 * Ends the game if there is a winner
		 */
		let winner = this.game.winner();
		if (winner != null) {
			this.game.phase = QGFGame.GAME_PHASES.DONE;
			this.message(`The game has now been won by ${winner}. Congratulations!`);
		}

		/*
		 * Sends a message instructing current game phase and whose turn it is
		 */
		if (this.game.phase === QGFGame.GAME_PHASES.AWAITING_QUESTION) {
			this.message(`Now awaiting a question from Player ${this.game.currentPlayer()}. A question can be asked with '/ask [targetPlayerId] [type]'.`, "GAME");
		} else if (this.game.phase === QGFGame.GAME_PHASES.AWAITING_QUESTION_RESPONSE) {
			this.message(`Now awaiting a response from Player ${this.game.targetId} for Player ${this.game.previousQuestioner} regarding whether they have any objects of type ${this.game.targetType}. A response can be submitted with '/answer [y/n]'.`, "GAME");
		}

		/*
		 * Resets the lobby to allow more games if the current game is done
		 */
		else if (this.game.phase === QGFGame.GAME_PHASES.DONE) {
			this.game = null;
			this.nonReadyPlayers = this.playerIds;
		}

	}


}

//A list of lobbies
let lobbies = [];

/**
 * Creates a lobby with the user with firstId
 * Will try and use an existing empty lobby if possible
 */
function createLobby(firstId) {
	let emptyLobby = lobbies.find(lobby => lobby.playerIds.length === 0);
	if (emptyLobby === undefined) {
		emptyLobby = new Lobby(lobbies.length);
		lobbies.push(emptyLobby);
	}
	emptyLobby.addPlayer(firstId);
}

/**
 * Joins the given id to the best lobby that it can join
 * Will create a lobby if necessary
 */
function matchMake(id) {
	let joinableLobbies = lobbies.filter(lobby => lobby.isJoinable).sort((a, b) => a.playerIds.length - b.playerIds.length);
	if (joinableLobbies.length === 0) {
		createLobby(id);
	} else {
		joinableLobbies[0].addPlayer(id);
	}
}

/**
 * Joins the joiningId to the lobby that alreadyLobbiedId exists in
 * Returns whether the join was successful
 */
function join(joiningId, alreadyLobbiedId) {
	if (!(alreadyLobbiedId in IdentityManager.ids) || IdentityManager.ids[alreadyLobbiedId].currentLobbyId == null || IdentityManager.ids[joiningId].currentLobbyId != null) {
		return false;
	}
	let lobbyToJoin = lobbies[IdentityManager.ids[alreadyLobbiedId].currentLobbyId];
	if (!lobbyToJoin.isJoinable) {
		return false;
	}
	lobbyToJoin.addPlayer(joiningId);
	return true;
}

module.exports = {
	lobbies: lobbies,
	createLobby: createLobby,
	matchMake: matchMake,
	join: join
};
