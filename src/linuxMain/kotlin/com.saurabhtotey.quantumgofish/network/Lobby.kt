package com.saurabhtotey.quantumgofish.network

import com.saurabhtotey.quantumgofish.logic.Game

/**
 * A class that manages a group of users and running their games
 */
class Lobby(hostName: String, maxPlayers: Int, port: Int, val password: String) {

	//All users in the lobby: order corresponds to turn order for the game
	val users = mutableListOf<User>(HostUser(this, hostName))

	//The current in-progress game if any
	var game: Game? = null

	/**
	 * TODO: everything
	 */
	init {

	}

}
