package com.saurabhtotey.quantumgofish.network

/**
 * A user that represents someone connected remotely
 * Manages seneding data to them and waiting for data from them
 * TODO: take in IP/port and other info needed to complete sendData and getResponse
 */
class RemoteClientUser(name: String, private val socket: Int) : User(name) {

	/**
	 * TODO: send data over port
	 */
	override fun sendData(data: String) {

	}

}
