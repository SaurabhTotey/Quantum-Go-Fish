package com.saurabhtotey.quantumgofish.network

import com.saurabhtotey.quantumgofish.TerminalManager

/**
 * The user that is hosting a lobby
 */
class HostUser constructor(name: String, private val terminalManager: TerminalManager): User(name) {

	//An input from the host is an input from the terminal
	override val input: String
		get() = this.terminalManager.input

	/**
	 * Handles data immediately that was given to it because this user is the host
	 * No need to send it or anything
	 */
	override fun sendData(data: String) {
		NetworkUtil.handleMessageFromHost(data, this.terminalManager)
	}

	/**
	 * Receiving data from the host just means running the terminal (since the host inputs data through the terminal)
	 */
	override fun receiveData() {
		this.terminalManager.run()
	}

}
