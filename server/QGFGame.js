
/**
 * A class that represents a single game of quantum go fish
 */
class QGFGame {

	/**
	 * The constructor of a game
	 * Takes in the player IDs of the players playing the game
	 */
	constructor(playerIds) {
		this.log = [`Starting a game of Quantum Go Fish with ${playerIds}.`];
		this.playerIds = playerIds;
		this.playerObjects = {};
		this.playerNegatives = {};
		this.types = [];
		for (let i = 0; i < playerIds.length; i++) {
			this.playerObjects[playerIds[i]] = [null, null, null, null];
			this.playerNegatives[playerIds[i]] = [];
			this.types.push(null);
		}
		this.previousQuestioner = null;
		this.targetId = null;
		this.targetType = null;
	}

	/**
	 * Poses a question from this.currentPlayer() to targetId whether they have an item of type
	 * Will set this.targetId so that the object knows from whom to get a response
	 * Will also set this.targetType so that the object knows what type the response is considering
	 * Returns whether the question is valid or not
	 * Invalid questions cause a game-wide loss
	 */
	poseQuestion(targetId, type) {

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

		/*
		 * If any player has no unknown objects, their unknowns cannot be of any type
		 * In such a case, their type negations are all types
		 */
		for (let i = 0; i < this.playerIds.length; i++) {
			let playerId = this.playerIds[i];
			if (this.playerObjects[playerId].includes(null)) {
				continue;
			}
			for (let j = 0; j < this.types.length; j++) {
				let type = this.types[j];
				if (!this.playerObjects[type].includes(type)) {
					this.giveNegation(playerId, type);
				}
			}
		}

		return this.isValid();

	}

	/**
	 * Returns whether this game state is valid or not
	 */
	isValid() {
		let counts = {};
		for (let i = 0; i < this.types.length; i++) {
			let type = this.types[i];
			if (type in counts) {
				counts[type]++;
			} else {
				counts[type] = 1;
			}
			if (counts[type] > 4) {
				return false;
			}
		}
		return true;
	}

	/**
	 * A method for giving an object to a player
	 * Makes sure that if 4 of an object exist, necessary negations are added
	 */
	giveObject(playerId, type) {

		this.playerObjects[playerId].push(type);

		/*
		 * Checks whether 4 objects of type are confirmed to exist
		 */
		let count = 0;
		for (let i = 0; i < this.playerIds.length; i++) {
			let playerId = this.playerIds[i];
			let objects = this.playerObjects[playerId];
			for (let j = 0; j < objects.length; j++) {
				if (objects[j] === type) {
					count++;
				}
				if (count === 4) {
					break;
				}
			}
			if (count === 4) {
				break;
			}
		}

		/*
		 * If 4 of type type are confirmed, then adds type to the negation of all players
		 * Players cannot turn unknowns into type anymore
		 */
		if (count === 4) {
			for (let i = 0; i < this.playerIds.length; i++) {
				let playerId = this.playerIds[i];
				if (!this.playerNegatives[playerId].includes(type)) {
					this.giveNegation(playerId, type);
				}
			}
		}

	}

	/**
	 * A method for adding a type to a negation
	 * Makes sure that if a negation is added, all revealable types are revealed
	 */
	giveNegation(playerId, type) {

		this.playerNegatives[playerId].push(type);

		/*
		 * Gets the number of objects that exist of type and also gets which players can still have type
		 */
		let idsWithoutNegation = [];
		let typeCount = 0;
		for (let i = 0; i < this.playerIds.length; i++) {
			let playerId = this.playerIds[i];
			if (!this.playerNegatives[playerId].includes(type)) {
				idsWithoutNegation.push(playerId);
			}
			let objects = this.playerObjects[playerId];
			for (let j = 0; j < objects.length; j++) {
				if (objects[j] === type) {
					typeCount++;
				}
			}
		}

		/*
		 * If more of type can exist, and only one player can have it, convert some of the player's unknowns into the remaining amount of type
		 */
		if (idsWithoutNegation.length === 1 && typeCount < 4) {
			while (typeCount < 4) {
				let objects = this.playerObjects[idsWithoutNegation[0]];
				objects.splice(objects.indexOf(null), 1);
				this.giveObject(idsWithoutNegation[0], type);
				typeCount++;
			}
		}

		/*
		 * If this player has all negations except one, convert the rest of their unknowns into the remaining amount of type
		 */
		if (this.playerNegatives[playerId].length === this.types.length - 1 && this.types.length === this.playerIds.length) {
			let missingType = null;
			for (let i = 0; i < this.types.length; i++) {
				let type = this.types[i];
				if (!this.playerNegatives[playerId].includes(type)) {
					missingType = type;
					break;
				}
			}
			let objects = this.playerObjects[playerId];
			while (objects.includes(null)) {
				objects.splice(objects.indexOf(null), 1);
				this.giveObject(playerId, missingType);
			}

		}

	}

	/**
	 * Answers a question posed from this.previousQuestioner from the viewpoint of this.targetId regarding this.targetType
	 * Returns whether the question answer is valid or not
	 */
	answerQuestion(containsType) {

		if (containsType) {

			/*
			 * Gets the index of the first object of that type that the responder has
			 * If they don't have that object explicitly, but rather through having an unknown, gets the first index of an unknown instead
			 * If the player doesn't have the object explicitly and also doesn't have any unknowns, they have ended the game
			 */
			let indexOfType = this.playerObjects[this.targetId].indexOf(this.targetType);
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
			 * If losing the object has caused the responder to not have any unknown objects, the responder has all type negations
			 * The responder can no longer have unknowns that are of any type
			 */
			this.playerObjects[this.targetId].splice(indexOfType, 1);
			if (!this.playerObjects[this.targetId].includes(null)) {
				for (let i = 0; i < this.types.length; i++) {
					let type = this.types[i];
					if (!this.playerNegatives[this.targetId].includes(type)) {
						this.giveNegation(this.targetId, type);
					}
				}
			}
			this.giveObject(this.previousQuestioner, this.targetType);

			return this.isValid();

		} else {

			//TODO: account for !containsType response

			return this.isValid();

		}

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
		 * Counts the number of times that elem appears in arr
		 */
		let numberOfOccurences = (arr, elem) => {
			let count = 0;
			for (let i = 0; i < arr.length; i++) {
				if (arr[i] === elem) {
					count++;
				}
			}
			return count;
		};

		/*
		 * Check the first victory condition that any player has all 4 of any type
		 */
		for (let i = 0; i < this.playerIds.length; i++) {
			let id = this.playerIds[i];
			if (this.playerObjects[id].length >= 4 && this.playerObjects[id].some(obj => numberOfOccurences(this.playerObjects[id], obj) === 4)) {
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
