package com.saurabhtotey.quantumgofish.network

/**
 * The user that is hosting the given lobby
 */
class HostUser constructor(private val lobby: Lobby, name: String): User(name) {

	//An input from the host is an input from the terminal
	override val input: String
		get() = this.lobby.terminalManager.input

	/**
	 * Handles data immediately that was given to it because this user is the host
	 * No need to send it or anything
	 */
	override fun sendData(data: String) {
		NetworkUtil.handleMessageFromHost(data)
	}

	/**
	 * Receiving data from the host just means running the terminal (since the host inputs data through the terminal)
	 */
	override fun receiveData() {
		this.lobby.terminalManager.run()
	}

}
