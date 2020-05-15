package com.saurabhtotey.quantumgofish.network

import kotlinx.cinterop.convert
import kotlinx.cinterop.cstr
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.reinterpret
import platform.posix.*

//TODO: handle incoming data
class Client(clientName: String, hostAddress: String, port: Int, password: String) {

	val socket = NetworkUtil.createSocket()

	init {
		memScoped {
			val addressDescription = NetworkUtil.describeAddress(port, hostAddress)
			if (connect(this@Client.socket, addressDescription.ptr.reinterpret(), addressDescription.size.convert()) == -1) {
				throw Error("Could not connect to $hostAddress:$port")
			}
			val firstMessage = clientName.padEnd(15) + password.padEnd(15)
			send(this@Client.socket, firstMessage.cstr, firstMessage.length.convert(), MSG_DONTWAIT)
		}
	}

	fun runUntilDone() {
		while (true) {}
		shutdown(this.socket, SHUT_RDWR)
		close(this.socket)
	}

}
