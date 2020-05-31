package com.saurabhtotey.quantumgofish.network

import kotlinx.cinterop.convert
import kotlinx.cinterop.cstr
import platform.posix.MSG_DONTWAIT
import platform.posix.send

/**
 * A user that represents someone connected remotely
 * Manages sending data to them and getting data from them
 * Is only used by the lobby: the client on their machine uses the Client class to communicate with the lobby
 */
class RemoteClientUser(name: String, val socket: Int) : User(name) {

	//The current message being built from network by this user
	private var currentInput = ""

	//All unhandled messages received from this user
	private val inputQueue = mutableListOf<String>()

	//Gets input from the inputQueue if any
	override val input: String
		get() = if (this.inputQueue.size > 0) this.inputQueue.removeAt(0) else ""

	/**
	 * Sends data over the socket (DOES NOT BLOCK)
	 */
	override fun sendData(data: String) {
		if (send(this@RemoteClientUser.socket, data.cstr, data.length.convert(), MSG_DONTWAIT).convert<Int>() == -1) {
			throw Exception("Could not send data '$data' to $name.")
		}
	}

	/**
	 * Tries to receive data from this user
	 */
	override fun receiveData() {
		this.currentInput += NetworkUtil.receiveIncomingFrom(this.socket)
		if (this.currentInput.endsWith('\n')) {
			this.inputQueue.add(this.currentInput.dropLast(1))
			this.currentInput = ""
		}
	}

}
