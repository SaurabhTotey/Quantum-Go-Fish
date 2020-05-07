package com.saurabhtotey.quantumgofish.logic

/**
 * A class that manages type information and updating type information
 */
class TypeManager(val players: List<Player>) {

	//A list of all the types in the owning game
	val gameObjectTypes = List(this.players.size) { GameObjectType(it) }

	//A list of all the game objects in the game: gets them off of the players
	val gameObjects
		get() = this.players.flatMap { it.gameObjects }

	/**
	 * Gives game objects to all the players
	 */
	init {
		this.players.forEach { it.gameObjects.addAll(List(4) { GameObject(this.gameObjectTypes.toMutableSet()) }) }
	}

	/**
	 * Registers the given string as a name for one of the types for objects in the owning game
	 * name must be an alphabetic string (only characters from the alphabet) and must be upper-cased
	 */
	fun registerTypeName(name: String) {
		if (name.any { !it.isLetter() }) {
			throw Error("Cannot register \"$name\" because it contains non-alphabetic characters.")
		}
		if (name != name.toUpperCase()) {
			throw Error("Cannot register \"$name\" because it is not entirely upper-cased.")
		}
		if (this.gameObjectTypes.any { it.name == name }) {
			throw Error("Cannot register \"$name\" because it is already registered.")
		}
		val updatedIndex = this.gameObjectTypes.indexOfFirst { it.name == UNKNOWN_TYPE_STRING }
		this.gameObjectTypes[updatedIndex].name = name
	}

	/**
	 * Determines the type of any GameObjects that may have their types determinable
	 * Exploits game rules to try and infer as much as possible about the game
	 */
	fun determineKnowableTypes() {
		//TODO: if only one player can have a certain type, convert their unknown objects into the remaining amount of that type
		//TODO: if the remaining amount of a type guarantees that a certain player has at some of them, convert an unknown into that type
		//TODO: figure out more rules/ways for objects to be determinable
		//TODO: consider maybe even generating all possible valid cases and then saving the intersection of them
	}

}
