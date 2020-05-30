package com.saurabhtotey.quantumgofish.logic

import com.saurabhtotey.quantumgofish.network.User

/**
 * A class that stores all the information for the game and handles/facilitates the logic
 */
class Game(users: List<User>) {

	//The players of the game
	private val players = users.map { Player(it) }

	//The index of the player who gets to ask the current question
	private var questionerIndex = 0
		set(value) {
			if (value > players.size) {
				throw Exception("Attempt to set questionerIndex to $value, but it should never be set to a value greater than ${players.size}.")
			}
			if (value < -1) {
				throw Exception("Attempt to set questionerIndex to $value, but it should never be set to a value less than -1.")
			}
			field = if (value == players.size) 0 else if (value == -1) players.size - 1 else value
		}

	//The player who can currently ask a question whenever executeTurn is called
	private val questioner
		get() = this.players[questionerIndex]

	//The game's type manager
	val typeManager = TypeManager(this.players)

	//The player who has won the game; returns null if no player satisfies any of the winning conditions of either owning all 4 of a type or asking a question that reveals the entire game state
	private val winner: Player?
		get() {
			var winner = this.players.firstOrNull { player -> this.typeManager.gameObjectTypes.any { type -> player.countOf(type) == 4 } }
			if (winner == null && typeManager.gameObjects.all { it.determinedType != null }) {
				winner = this.questioner
			}
			return winner
		}

	/**
	 * TODO: runs a turn of the game and returns the winner if any
	 */
	fun executeTurn(): Player? {
		if (this.winner != null) {
			throw Exception("Cannot executeTurn if the game is already won by ${this.winner}.")
		}

		//TODO: allow questioner to ask question

		val winner = this.winner
		if (winner == null) {
			this.questionerIndex += 1
		}
		return winner
	}

}
