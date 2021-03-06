package com.saurabhtotey.quantumgofish.logic

import com.saurabhtotey.quantumgofish.TextUtil

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
	 * Returns the type that had its name changed
	 */
	fun registerTypeName(name: String): GameObjectType {
		val validatorResponse = TextUtil.isValidName(name)
		if (validatorResponse.isNotEmpty()) {
			throw Exception(validatorResponse)
		}
		if (this.gameObjectTypes.any { it.name == name }) {
			throw Exception("Cannot register \"$name\" because it is already registered.")
		}
		val updatedIndex = this.gameObjectTypes.indexOfFirst { it.name == UNKNOWN_TYPE_STRING }
		if (updatedIndex == -1) {
			throw Exception("Cannot register \"$name\" because all types have names.")
		}
		this.gameObjectTypes[updatedIndex].name = name
		return this.gameObjectTypes[updatedIndex]
	}

	/**
	 * Determines the type of any GameObjects that may have their types determinable
	 * Exploits game rules to try and infer as much as possible about the game
	 * Will call itself if it changed anything to make sure that it cannot continue to determine more changes
	 */
	tailrec fun determineKnowableTypes() {
		var anythingChanged: Boolean

		//If a type is justFinished (has a count of 4 meaning no other objects can become that type), remove it as a possibility from all remaining objects
		val finishedTypes = this.gameObjectTypes.filter { it.justFinished }.toMutableSet()
		this.gameObjects.filter { it.determinedType !in finishedTypes }.forEach { obj ->
			finishedTypes.filter { it in obj.possibleTypes }.forEach {
				obj.removeTypePossibility(it)
			}
		}
		anythingChanged = finishedTypes.isNotEmpty()
		finishedTypes.forEach { it.justFinished = false }

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
