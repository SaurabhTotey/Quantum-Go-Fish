package com.saurabhtotey.quantumgofish.logic

import com.saurabhtotey.quantumgofish.network.User

/**
 * A class that stores all the information for the game and handles/facilitates the logic
 */
class Game(users: List<User>) {
	val players = users.map { Player(it) }
}
