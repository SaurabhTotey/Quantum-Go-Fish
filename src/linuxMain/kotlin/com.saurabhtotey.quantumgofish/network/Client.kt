package com.saurabhtotey.quantumgofish.network

import kotlinx.cinterop.convert
import kotlinx.cinterop.cstr
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.reinterpret
import platform.posix.*

/**
 * A client class that manages the connection and networking to a host/lobby
 */
class Client(clientName: String, hostAddress: String, port: Int, password: String) {

	//C socket handle
	private val socket = NetworkUtil.createSocket()

	/**
	 * Connects the client to the lobby host and sends the first message of the client's name and password
	 */
	init {
		//Attempts connection to the host
		memScoped {
			val addressDescription = NetworkUtil.describeAddress(port, hostAddress)
			if (connect(this@Client.socket, addressDescription.ptr.reinterpret(), addressDescription.size.convert()) == -1) {
				throw Error("Could not connect to $hostAddress:$port.")
			}
		}
		//Sends an initial message that the host is expecting of the client's name and password
		val firstMessage = clientName.padEnd(15) + password.padEnd(15)
		send(this@Client.socket, firstMessage.cstr, firstMessage.length.convert(), MSG_DONTWAIT)
	}

	fun runUntilDone() {
		while (true) {}
		shutdown(this.socket, SHUT_RDWR)
		close(this.socket)
	}

}
