
/**
 * A class that represents a single game of quantum go fish
 */
class QGFGame {

	/**
	 * The constructor of a game
	 * Takes in the player IDs of the players playing the game
	 */
	constructor(playerIds) {
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
			let firstReplacableIndex = this.types.indexOf(null);
			if (firstReplacableIndex < 0) {
				return false;
			}
			this.types[firstReplacableIndex] = type;
		}

		return true;

	}

	/**
	 * Answers a question posed from this.previousQuestioner from the viewpoint of this.targetId regarding this.targetType
	 * Returns whether the question answer is valid or not
	 */
	answerQuestion(containsType) {

		if (containsType) {

			/*
			 * Invalid response: player answered they had the type, but they don't
			 * Either they answered before that they didn't, or 4 of that type already exist with other players
			 */
			if (this.playerNegatives[this.targetId].includes(this.targetType)) {
				return false;
			}

			/*
			 * Gets the index of the first object of that type that the responder has
			 * If they don't have that object explicitly, but rather through having an unknown, gets the first index of an unknown instead
			 * If the player doesn't have the object explicitly and also doesn't have any unknowns, they have ended the game
			 */
			let indexOfType = this.playerObjects[this.targetId].indexOf(this.targetType);
			if (indexOfType < 0) {
				indexOfType = this.playerObjects[this.targetId].indexOf(null);
				if (indexOfType < 0) {
					return false;
				}
			}

			/*
			 * Gives the object from the responder to the questioner
			 */
			this.playerObjects[this.targetId].splice(indexOfType, 1);
			this.playerObjects[this.previousQuestioner].push(this.targetType);

			//TODO: check whether 4 of a type exist and add it to the negations of the players who don't have it

			return true;
		}

		//TODO: account for !containsType response

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
				if (arr[i] == elem) {
					count++;
				}
			}
			return count;
		}

		/*
		 * Check the first victory condition that any player has all 4 of any type
		 */
		for (let i = 0; i < this.playerIds.length; i++) {
			let id = this.playerIds[i];
			if (this.playerObjects[id].length >= 4 && this.playerObjects[id].some(obj => numberOfOccurences(this.playerObjects[id], obj) == 4)) {
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
