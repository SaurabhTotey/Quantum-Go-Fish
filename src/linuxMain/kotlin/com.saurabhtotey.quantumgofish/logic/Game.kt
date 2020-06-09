package com.saurabhtotey.quantumgofish.logic

import com.saurabhtotey.quantumgofish.network.User

/**
 * A class that stores all the information for the game and handles/facilitates the logic
 */
class Game(val users: List<User>) {

	//The players of the game
	val players = this.users.map { Player(it) }

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

	//The user who can currently ask a question: is assumed to be responsible behind calling ask
	val questioner
		get() = this.players[questionerIndex].user

	//The type that is being asked about by questioner to answerer (null if none)
	var typeInQuestion: GameObjectType? = null
		private set

	//The user who must answer to questioner whether they have an object of typeInQuestion (null if none)
	var answerer: User? = null
		private set

	//The game's type manager
	val typeManager = TypeManager(this.players)

	//The user who has won the game; returns null if no player satisfies any of the winning conditions of either owning all 4 of a type or asking a question that reveals the entire game state; null if a question has to be answered
	val winner: User?
		get() {
			if (this.answerer != null || this.typeInQuestion != null) {
				return null
			}
			var winner = this.players.firstOrNull { player -> this.typeManager.gameObjectTypes.any { type -> player.countOf(type) == 4 } }?.user
			if (winner == null && typeManager.gameObjects.all { it.determinedType != null }) {
				this.questionerIndex -= 1
				winner = this.questioner
			}
			return winner
		}

	/**
	 * A method that should be called when the questioner wants to ask the given target whether they have the given type
	 */
	fun ask(target: User, type: GameObjectType) {
		if (this.answerer != null || this.typeInQuestion != null) {
			throw Exception("Cannot ask a question if there is a current question standing.")
		}
		val asker = this.players[this.questionerIndex]
		if (type !in asker.possibleOwnedTypes) {
			throw Exception("Player asked a question about a type they do not own.")
		}
		if (asker.gameObjects.all { it.determinedType != type }) {
			asker.convertUnknownInto(type)
		}
		this.answerer = target
		this.typeInQuestion = type
		this.typeManager.determineKnowableTypes()
	}

	/**
	 * A method that should be called when the target of a question either answers yes or no
	 */
	fun answer(hasType: Boolean) {
		if (this.answerer == null || this.typeInQuestion == null) {
			throw Exception("Cannot answer a question if there is no standing question.")
		}
		val answerer = this.players.first { it.user == this.answerer }
		if (hasType && this.typeInQuestion!! !in answerer.possibleOwnedTypes) {
			throw Exception("Answerer answered that they have a type that they cannot possibly have.")
		}
		if (!hasType && answerer.gameObjects.any { it.determinedType == this.typeInQuestion }) {
			throw Exception("Answerer answered that they do not have a type that they actually do have.")
		}
		if (hasType) {
			var objectToGive = answerer.gameObjects.firstOrNull { it.determinedType == this.typeInQuestion }
			if (objectToGive == null) {
				objectToGive = answerer.gameObjects.first { this.typeInQuestion!! in it.possibleTypes }
				objectToGive.determineType(this.typeInQuestion!!)
			}
			answerer.gameObjects.remove(objectToGive)
			this.players[this.questionerIndex].gameObjects.add(objectToGive)
		} else {
			answerer.gameObjects.filter { it.determinedType == null }.forEach { it.removeTypePossibility(this.typeInQuestion!!) }
		}
		this.typeInQuestion = null
		this.answerer = null
		this.questionerIndex += 1
		this.typeManager.determineKnowableTypes()
	}

}
