package com.saurabhtotey.quantumgofish.logic

//The default name for a GameObjectType that has no name yet
const val UNKNOWN_TYPE_STRING = "?"

/**
 * A small data class that represents the type of a GameObject
 * GameObjects may have multiple possible GameObjectTypes
 * The type has an integer ID which is used to ensure that certain types are the same
 * The name of a type is just for display
 */
data class GameObjectType(val id: Int, var name: String = UNKNOWN_TYPE_STRING) {

	//How many objects of this type are determined to be specifically this type and nothing possibly else
	var determinedCount = 0
		set(value) {
			if (value > 4) {
				throw Error("The count for objects that are determined to be of type $this has become $value which exceeds the maximum of 4.")
			}
			if (value < field) {
				throw Error("The count for objects that are detetermined to be of type $this has become $value which is less than $field; the count should never go down.")
			}
			field = value
		}

}

/**
 * A small data class that represents a GameObject
 * GameObjects are the objects that each player starts off with 4 of that have a superposition of types (denoted by possibleTypes)
 * A GameObject's type is only determined when possibleTypes has only 1 entry
 */
data class GameObject(val possibleTypes: MutableSet<GameObjectType>) {

	//Returns the determined type of this object if any (null otherwise)
	val determinedType
		get() = if (this.possibleTypes.size > 1) null else this.possibleTypes.first()

	/**
	 * Makes the GameObject determined with only one possible type (no superposition)
	 * This gives the GameObject a definite type
	 */
	fun determineType(type: GameObjectType) {
		if (this.determinedType != null) {
			throw Error("Cannot determineType of $this to be $type because it already has type ${this.determinedType}.")
		}
		if (type !in this.possibleTypes) {
			throw Error("Cannot determineType of $this to be $type because it is not contained in the list of possible types ${this.possibleTypes}.")
		}
		this.possibleTypes.clear()
		this.possibleTypes.add(type)
		type.determinedCount += 1
	}

	/**
	 * Removes the possiblity of this GameObject being the given type
	 */
	fun removeTypePossibility(type: GameObjectType) {
		if (type !in this.possibleTypes) {
			throw Error("Cannot remove $type from $this because it is not in the possible types ${this.possibleTypes}.")
		}
		if (this.determinedType != null) {
			throw Error("Cannot remove $type from $this because the type is already determined.")
		}
		this.possibleTypes.remove(type)
		val determinedType = this.determinedType
		if (determinedType != null) {
			determinedType.determinedCount += 1
		}
	}

}
