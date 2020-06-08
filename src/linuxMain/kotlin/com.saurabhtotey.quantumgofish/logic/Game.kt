package com.saurabhtotey.quantumgofish.logic

import com.saurabhtotey.quantumgofish.network.User

/**
 * A class that stores all the information for the game and handles/facilitates the logic
 */
class Game(val users: List<User>) {

	//The players of the game
	private val players = this.users.map { Player(it) }

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
	val questioner
		get() = this.players[questionerIndex].user

	//The game's type manager
	val typeManager = TypeManager(this.players)

	//The player who has won the game; returns null if no player satisfies any of the winning conditions of either owning all 4 of a type or asking a question that reveals the entire game state
	private val winner: User?
		get() {
			var winner = this.players.firstOrNull { player -> this.typeManager.gameObjectTypes.any { type -> player.countOf(type) == 4 } }?.user
			if (winner == null && typeManager.gameObjects.all { it.determinedType != null }) {
				winner = this.questioner
			}
			return winner
		}

	/**
	 * A method that should be called when the questioner wants to ask the given target whether they have the given type
	 */
	fun ask(target: User, type: GameObjectType) {
		TODO()
	}

	/**
	 * A method that should be called when the target of a question either answers yes or no
	 */
	fun answer(hasType: Boolean) {
		TODO()
	}

}
