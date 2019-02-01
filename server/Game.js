
/**
 * A class that represents a single game of quantum go fish
 */
class Game {

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
	}

}
