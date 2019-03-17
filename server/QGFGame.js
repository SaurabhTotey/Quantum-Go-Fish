
/**
 * A set of constants for the game phases
 */
const GAME_PHASES = {
    AWAITING_QUESTION: 0,
    AWAITING_QUESTION_RESPONSE: 1,
    DONE: 2
};

/**
 * Counts how many times needle is in haystack
 */
const numberOfOccurrences = (haystack, needle) => {
	let count = 0;
	for (let i = 0; i < haystack.length; i++) {
		if (haystack[i] === needle) {
			count++;
		}
	}
	return count;
};

/**
 * A class that represents a single game of Quantum Go Fish
 */
class QGFGame {

	/**
	 * The constructor of a game
	 * Takes in the lobby of the game
	 */
	constructor(lobby) {
		this.lobby = lobby;
		this.playerIds = lobby.playerIds;
		this.playerObjects = {};
		this.playerNegatives = {};
		this.types = [];
		for (let i = 0; i < this.playerIds.length; i++) {
			this.playerObjects[this.playerIds[i]] = [null, null, null, null];
			this.playerNegatives[this.playerIds[i]] = [];
		}
		this.previousQuestioner = null;
		this.targetId = null;
		this.targetType = null;

		this.phase = GAME_PHASES.AWAITING_QUESTION;

		this.lobby.message(`Starting a game of Quantum Go Fish between ${this.playerIds}! Good luck, have fun, and may the best player win!`, "GAME");
		this.lobby.message(`Currently, Player ${this.currentPlayer()} must ask a question. A question can be asked with '/ask [targetPlayerId] [type]'.`, "GAME");
	}

	/**
	 * Poses a question from this.currentPlayer() to targetId whether they have an item of type
	 * Will set this.targetId so that the object knows from whom to get a response
	 * Will also set this.targetType so that the object knows what type the response is considering
	 * Returns whether the question is valid or not
	 * Invalid questions cause a game-wide loss
	 */
	poseQuestion(targetId, type) {

	    this.lobby.message(`Player ${this.currentPlayer()} asked Player ${targetId} if they have any objects of type ${type}.`, "GAME");
        this.phase = GAME_PHASES.AWAITING_QUESTION_RESPONSE;

		this.targetId = targetId;
		this.previousQuestioner = this.currentPlayer();
		this.targetType = type;

		/*
		 * If this is the first time that type is mentioned, add it to the list of types
		 * If the list of types already has all the types of the game, this question is invalid and the game is over
		 */
		if (!this.types.includes(type)) {
			if (this.types.length < this.playerIds.length) {
				this.types.push(type);
			} else {
				return false;
			}
		}

		/*
		 * If the player doesn't explicitly have that type, they are either wrong (game is lost), or they have revealed an unknown
		 */
		if (!this.playerObjects[this.previousQuestioner].includes(type)) {
			if (this.playerNegatives[this.previousQuestioner].includes(type) || !this.playerObjects[this.previousQuestioner].includes(null)) {
				return false;
			}
			this.playerObjects[this.previousQuestioner].splice(this.playerObjects[this.previousQuestioner].indexOf(null), 1);
			this.giveObject(this.previousQuestioner, type);
		}

        return this.isValid();

	}

	/**
	 * Returns whether this game state is valid or not
	 */
	isValid() {
		this.revealGuaranteedObjects();
		return this.types.every(type => {
			let count = 0;
			this.playerIds.forEach(playerId => count += numberOfOccurrences(this.playerObjects[playerId], type));
			return count <= 4;
		});
	}

	/**
	 * Checks all players for whether any unknowns can be converted to objects that they are guaranteed to have
	 */
	revealGuaranteedObjects() {

		/*
		 * If any player has no unknown objects, their unknowns cannot be of any type
		 * In such a case, their type negations are all types
		 */
		this.playerIds.filter(playerId => !this.playerObjects[playerId].includes(null)).forEach(playerId => {
			this.types.filter(type => !this.playerNegatives[playerId].includes(type)).forEach(type => this.giveNegation(playerId, type));
		});

		/*
		 * Goes through all types and all players to check whether there are more rules that could be enforced that could narrow down the rules
		 */
		this.types.forEach(type => {

			/*
			 * Checks whether 4 objects of type are confirmed to exist
			 */
			let totalTypeCount = 0;
			this.playerIds.forEach(playerId => totalTypeCount += numberOfOccurrences(this.playerObjects[playerId], type));

			/*
             * If 4 of type type are confirmed, then adds type to the negation of all players
             * Players cannot turn unknowns into type anymore
             */
			if (totalTypeCount === 4) {
				this.playerIds.filter(playerId => !this.playerNegatives[playerId].includes(type)).forEach(playerId => this.giveNegation(playerId, type));
			}

			/*
             * If more of type can exist, and only one player can have it, convert some of the player's unknowns into the remaining amount of type
             */
			let idsWithoutNegation = this.playerIds.filter(playerId => !this.playerNegatives[playerId].includes(type));
			if (idsWithoutNegation.length === 1 && totalTypeCount < 4) {
				while (totalTypeCount < 4) {
					let objects = this.playerObjects[idsWithoutNegation[0]];
					objects.splice(objects.indexOf(null), 1);
					this.giveObject(idsWithoutNegation[0], type);
					totalTypeCount++;
				}
			}

			/*
			 * If any player has all negations except one, convert the rest of their unknowns into the remaining amount of type
			 */
			this.playerIds.filter(
				playerId => this.playerNegatives[playerId].length === this.types.length - 1 && this.types.length === this.playerIds.length
			).forEach(playerId => {
				let missingType = this.types.find(type => !this.playerNegatives[playerId].includes(type));
				let objects = this.playerObjects[playerId];
				while (objects.includes(null)) {
					objects.splice(objects.indexOf(null), 1);
					this.giveObject(playerId, missingType);
				}
			});

		});

	}

	/**
	 * A method for giving an object to a player
	 * Makes sure that if 4 of an object exist, necessary negations are added
	 */
	giveObject(playerId, type) {

		this.playerObjects[playerId].push(type);

		this.revealGuaranteedObjects();

	}

	/**
	 * A method for adding a type to a negation
	 * Makes sure that if a negation is added, all revealable types are revealed
	 */
	giveNegation(playerId, type) {

		this.playerNegatives[playerId].push(type);

		this.revealGuaranteedObjects();

	}

	/**
	 * Answers a question posed from this.previousQuestioner from the viewpoint of this.targetId regarding this.targetType
	 * Returns whether the question answer is valid or not
	 */
	answerQuestion(containsType) {

	    this.lobby.message(`Player ${this.targetId} has told player ${this.previousQuestioner} that they ${"do" + (containsType? "" : "n't")} have an object of type ${this.targetType}`, "GAME");
        this.phase = GAME_PHASES.AWAITING_QUESTION;

		let indexOfType = this.playerObjects[this.targetId].indexOf(this.targetType);

		if (containsType) {

			/*
			 * If the responder don't have the object explicitly, but rather through having an unknown, gets the first index of an unknown instead
			 * If the player doesn't have the object explicitly and also doesn't have any unknowns, they have ended the game
			 */
			if (indexOfType < 0) {

				/*
				 * Invalid response: player answered they had the type, but they don't
				 * Either they answered before that they didn't, or 4 of that type already exist with other players
				 */
				if (this.playerNegatives[this.targetId].includes(this.targetType)) {
					return false;
				}

				indexOfType = this.playerObjects[this.targetId].indexOf(null);
				if (indexOfType < 0) {
					return false;
				}

			}

			/*
			 * Gives the object from the responder to the questioner
			 */
			this.playerObjects[this.targetId].splice(indexOfType, 1);
			this.giveObject(this.previousQuestioner, this.targetType);

		} else {

			/*
			 * Player has ended the game: they have said that they don't have the requested object, but they do
			 */
			if (indexOfType >= 0) {
				return false;
			}

			this.giveNegation(this.targetId, this.targetType);

		}

        return this.isValid();

	}

	/**
	 * Gets the ID of the player whose turn it is right now
	 */
	currentPlayer() {
		if (this.previousQuestioner == null) {
			return this.playerIds[0];
		}
		return this.playerIds[(this.playerIds.indexOf(this.previousQuestioner) + 1) % this.playerIds.length];
	}

	/**
	 * Gets the ID of the game's winner if any
	 * If there is no game winner yet, returns null
	 */
	winner() {

		/*
		 * Check the first victory condition that any player has all 4 of any type
		 */
		for (let i = 0; i < this.playerIds.length; i++) {
			let id = this.playerIds[i];
			if (this.playerObjects[id].length >= 4 && this.playerObjects[id].some(obj => obj != null && numberOfOccurrences(this.playerObjects[id], obj) === 4)) {
				return id;
			}
		}

		/*
		 * Check the second victory condition that the previous questioner revealed the entire game state
		 */
		if (this.playerIds.every(id => this.playerObjects[id].every(obj => obj != null))) {
			return this.previousQuestioner;
		}

		/*
		 * Neither victory condition is met: nobody has won so return null
		 */
		return null;

	}

}

module.exports = {
    GAME_PHASES: GAME_PHASES,
	QFGGame: QGFGame
};
