package com.saurabhtotey.quantumgofish.logic

/**
 * A class that manages type information and updating type information
 * Is useful because types have mutable state, and it is helpful to make the state global
 * Therefore, all types that are used in the game should be coming from TypeManager which holds/manages those global instances
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
	 * Will call itself if it changed anything to make sure that it cannot continue to determine more changes
	 */
	tailrec fun determineKnowableTypes() {
		var anythingChanged = false

		//If a type is 'finished' (has a count of 4 meaning no other objects can become that type), remove it as a possibility from all remaining objects
		val finishedTypes = this.gameObjectTypes.filter { it.determinedCount == 4 }.toMutableSet()
		this.gameObjects.filter { it.determinedType !in finishedTypes }.forEach { obj ->
			finishedTypes.filter { type -> type in obj.possibleTypes }.forEach { type ->
				obj.removeTypePossibility(type)
				anythingChanged = true
			}
		}

		//For each player, consider all its possible types: if the remaining players cannot possibly fill in the remaining amount of that possible type, this player must have some of that type
		this.players.forEach { player ->
			player.possibleOwnedTypes.forEach { type ->
				val amountAlreadyOwnedByPlayer = player.gameObjects.count { it.determinedType == type }
				val amountOfTypePossibleByOthers = this.players.filter { it != player }.flatMap { it.gameObjects }.count { type in it.possibleTypes }
				val amountOfTypeToGiveToPlayer = 4 - amountOfTypePossibleByOthers - amountAlreadyOwnedByPlayer
				if (amountOfTypeToGiveToPlayer > 0) {
					repeat(amountOfTypeToGiveToPlayer) { player.convertUnknownInto(type) }
					anythingChanged = true
				}
			}
		}

		if (anythingChanged) {
			this.determineKnowableTypes()
		}
	}

}
