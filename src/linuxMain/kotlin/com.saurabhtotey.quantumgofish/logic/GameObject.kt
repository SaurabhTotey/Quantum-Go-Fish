package com.saurabhtotey.quantumgofish.logic

//The default name for a GameObjectType that has no name yet
const val UNKNOWN_TYPE_STRING = "?"

/**
 * A small data class that represents the type of a GameObject
 * GameObjects may have multiple possible GameObjectTypes
 * The type has an integer ID which is used to ensure that certain types are the same
 * The name of a type is just for display
 */
data class GameObjectType(val id: Int, var name: String = UNKNOWN_TYPE_STRING)

/**
 * A small data class that represents a GameObject
 * GameObjects are the objects that each player starts off with 4 of that have a superposition of types (denoted by possibleTypes)
 * A GameObject's type is only determined when possibleTypes has only 1 entry
 * A GameObject may change owners
 */
data class GameObject(val possibleTypes: MutableList<GameObjectType>, var owner: Player)
