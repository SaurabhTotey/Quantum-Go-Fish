package com.saurabhtotey.quantumgofish.network

import kotlinx.cinterop.convert
import kotlinx.cinterop.cstr
import platform.posix.MSG_DONTWAIT
import platform.posix.send

/**
 * A user that represents someone connected remotely
 * Manages seneding data to them and waiting for data from them
 * TODO: take in IP/port and other info needed to complete sendData and getResponse
 */
class RemoteClientUser(name: String, private val socket: Int) : User(name) {

	/**
	 * Sends data over the socket (DOES NOT BLOCK)
	 */
	override fun sendData(data: String) {
		if (send(this@RemoteClientUser.socket, data.cstr, data.length.convert(), MSG_DONTWAIT).convert<Int>() == -1) {
			throw Error("Could not send data '$data' to $name.")
		}
	}

}
