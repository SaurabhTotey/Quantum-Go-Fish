package com.saurabhtotey.quantumgofish.network

import com.saurabhtotey.quantumgofish.logic.Game
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

/**
 * A class that manages a group of users and running their games
 */
class Lobby(hostName: String) {

	//All users in the lobby: order corresponds to turn order for the game
	val users = mutableListOf<User>(HostUser(this, hostName))

	//The current in-progress game if any
	var game: Game? = null

	/**
	 * Starts a new thread that continually pings each user to ensure that they are connected
	 * TODO: listen for connections and add them to users as RemoteClientUsers
	 */
	init {
		GlobalScope.launch {
			while (true) {
				this@Lobby.users.forEach { user ->
					//TODO: ping user and await response
				}
			}
		}
	}

}
