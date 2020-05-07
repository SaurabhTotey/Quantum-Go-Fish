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

	/**
	 * Converts one of this player's unknown objects into the given type
	 */
	fun convertUnknownInto(type: GameObjectType) {
		val unknownToConvert = this.gameObjects.firstOrNull { !it.determinedType && type in it.possibleTypes }
				?: throw Error("$this cannot convertUnknownInto($type) because there are no valid unknown objects that can be converted to $type in ${this.gameObjects}.")
		unknownToConvert.determineType(type)
	}

}
