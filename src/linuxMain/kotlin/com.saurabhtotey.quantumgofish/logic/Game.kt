package com.saurabhtotey.quantumgofish.logic

import com.saurabhtotey.quantumgofish.network.User

/**
 * A class that stores all the information for the game and handles/facilitates the logic
 */
class Game(users: List<User>) {

	//The players of the game
	val players = users.map { Player(it) }

	//The game's type manager
	val typeManager = TypeManager(this.players)

	/**
	 * TODO: runs a turn of the game and returns the winner if any
	 */
	fun executeTurn(): Player? {

		return null
	}

}
