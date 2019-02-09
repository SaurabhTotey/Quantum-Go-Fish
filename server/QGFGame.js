
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
		for (let i = 0; i < playerIds.length; i++) {
			this.playerObjects[playerIds[i]] = [null, null, null, null];
		}
		this.previousQuestioner = null;
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
