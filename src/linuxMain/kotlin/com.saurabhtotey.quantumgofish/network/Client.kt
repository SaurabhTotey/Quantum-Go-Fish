package com.saurabhtotey.quantumgofish.network

import com.saurabhtotey.quantumgofish.TerminalManager
import kotlinx.cinterop.convert
import kotlinx.cinterop.cstr
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.reinterpret
import platform.posix.*

/**
 * A client class that manages the connection and networking to a host/lobby
 */
class Client(private val terminalManager: TerminalManager, clientName: String, hostAddress: String, port: Int, password: String) {

	//Whether the client is/should be running
	private var isActive = true

	//C socket handle
	private val socket = NetworkUtil.createSocket()

	/**
	 * Connects the client to the lobby host and sends the first message of the client's name and password
	 */
	init {
		//Attempts connection to the host
		memScoped {
			val addressDescription = NetworkUtil.describeAddress(port, hostAddress)
			val startTime = time(null)
			while (connect(this@Client.socket, addressDescription.ptr.reinterpret(), addressDescription.size.convert()) == -1) {
				this@Client.terminalManager.run()
				if (time(null) - startTime > 5) {
					throw Error("Could not connect to $hostAddress:$port.")
				}
			}
		}
		//Sends an initial message that the host is expecting of the client's name and password
		val firstMessage = "$clientName\n$password\n"
		send(this@Client.socket, firstMessage.cstr, firstMessage.length.convert(), MSG_DONTWAIT)
	}

	fun runUntilDone() {
		while (this.isActive) {
			this.terminalManager.run()
			//TODO: send this.terminalManager.input to host if not blank
			NetworkUtil.handleMessageFromHost(NetworkUtil.receiveIncomingFrom(this.socket), this.terminalManager)
		}
		shutdown(this.socket, SHUT_RDWR)
		close(this.socket)
	}

}
