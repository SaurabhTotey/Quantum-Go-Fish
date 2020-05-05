package com.saurabhtotey.quantumgofish.logic

import com.saurabhtotey.quantumgofish.network.User

/**
 * A player class that stores a user's information for the current in-session game
 */
class Player (val user: User) {

	//A mutable list of all the objects this player currently has: is subject to change and mutate
	val gameObjects = mutableListOf<GameObject>()

	//A getter that returns all possible types this player owns
	val possibleOwnedTypes
		get() = this.gameObjects.flatMap { it.possibleTypes }.toSet()
}
