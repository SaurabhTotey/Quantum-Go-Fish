package com.saurabhtotey.quantumgofish.network

import com.saurabhtotey.quantumgofish.TerminalManager
import com.saurabhtotey.quantumgofish.TextUtil
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

	//The current input being sent from the host (that is not yet ready to be interpretted)
	private var currentInput = ""

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
		send(this.socket, firstMessage.cstr, firstMessage.length.convert(), MSG_DONTWAIT)
	}

	/**
	 * A method that doesn't return until the Client is done
	 * Cleanly closes everything down once the client is done
	 */
	fun runUntilDone() {
		while (this.isActive) {
			this.terminalManager.run()
			val terminalManagerInput = this.terminalManager.input
			if (terminalManagerInput.isNotBlank()) {
				val message = "$terminalManagerInput\n"
				send(this.socket, message.cstr, message.length.convert(), MSG_DONTWAIT)
			}
			this.currentInput += NetworkUtil.receiveIncomingFrom(this.socket)
			if (TextUtil.isValidHostMessage(this.currentInput)) {
				NetworkUtil.handleMessageFromHost(this.currentInput, this.terminalManager)
				this.currentInput = ""
			}
		}
		shutdown(this.socket, SHUT_RDWR)
		close(this.socket)
	}

}
