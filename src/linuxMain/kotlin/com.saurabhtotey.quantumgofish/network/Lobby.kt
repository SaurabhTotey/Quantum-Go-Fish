package com.saurabhtotey.quantumgofish.network

import com.saurabhtotey.quantumgofish.TerminalManager
import com.saurabhtotey.quantumgofish.logic.Game
import kotlinx.cinterop.*
import platform.posix.*

/**
 * A class that manages a group of users and running their games
 */
class Lobby(val terminalManager: TerminalManager, hostName: String, maxPlayers: Int, port: Int, val password: String) {

	//Whether the lobby is/should be running
	private var isActive = true

	//Creates a socket that all the clients will connect to; is a C socket handle
	private val socket = NetworkUtil.createSocket()

	//All users in the lobby: order corresponds to turn order for the game
	private val users = mutableListOf<User>(HostUser(this, hostName))

	//The current in-progress game if any
	private var game: Game? = null

	/**
	 * Binds the socket to the given port and listens for incoming connections
	 */
	init {
		//Binds the socket to the port given by constructor parameters
		memScoped {
			val addressDescription = NetworkUtil.describeAddress(port)
			if (bind(this@Lobby.socket, addressDescription.ptr.reinterpret(), addressDescription.size.convert()) == -1) {
				throw Error("Could not bind lobby socket to port $port. Make sure that $port is free.")
			}
		}
		//Sets the socket to listen for incoming connections
		if (listen(this@Lobby.socket, maxPlayers) == -1) {
			throw Error("Could not listen for up to $maxPlayers connections on the socket bound to port $port.")
		}
		this.terminalManager.print("Started a lobby as $hostName on ${NetworkUtil.getSelfAddress()}:$port with password \"$password\" that allows up to $maxPlayers players.\n", TerminalManager.Color.MAGENTA)
	}

	/**
	 * A method that checks to see if any players are trying to join
	 * If a player is trying to join, it will accept them and try to exchange initial information within the first 5 seconds
	 * If information cannot be exchanged in the first 5 seconds, the connection is terminated
	 */
	private fun acceptAnyJoiningPlayers() {
		memScoped {
			val clientInfo = cValue<sockaddr_in>()
			val sockAddrInSize = cValuesOf(sizeOf<sockaddr_in>().toUInt())
			val newSocket: Int = accept(this@Lobby.socket, clientInfo.ptr.reinterpret(), sockAddrInSize)
			if (newSocket == -1) {
				return
			}
			try {
				var initialResponseString = ""
				val startTime = time(null)
				while (initialResponseString.count { it == '\n' } < 2) {
					val initialResponse = this.allocArray<ByteVar>(32)
					recv(newSocket, initialResponse, 32.convert(), MSG_DONTWAIT)
					initialResponseString += initialResponse.toKString()
					this@Lobby.handleUserInputs()
					if (time(null) - startTime > 5) {
						throw Error("Connection couldn't be accepted in a timely fashion, so it was terminated.")
					}
				}
				val parts = initialResponseString.dropLast(1).split('\n')
				val name = parts[0]
				val givenPassword = parts[1]
				if (givenPassword != this@Lobby.password) {
					throw Error("Connection attempted with incorrect password.")
				}
				if (this@Lobby.users.any { it.name == name }) {
					throw Error("Connection attempted with a taken name.")
				}
				val newUser = RemoteClientUser(name, newSocket)
				this@Lobby.users.forEach { it.sendData("M\nTHE UNIVERSE\n$name is joining the lobby!\n") }
				newUser.sendData("M\nTHE UNIVERSE\nWelcome to the lobby!\n")
				this@Lobby.users.add(newUser)
				this@Lobby.users.forEach { it.sendData("M\nTHE UNIVERSE\nList of players in lobby is now [${this@Lobby.users.joinToString(", ") { it.name }}].\n") }
			} catch (e: Exception) {
				if (e is Error && e.message != null) {
					val returnMessage = "E\n${e.message!!.replace("\n", " ")}\n"
					send(newSocket, returnMessage.cstr, returnMessage.length.convert(), MSG_DONTWAIT)
				}
				shutdown(newSocket, SHUT_RDWR)
				close(newSocket)
			}
		}
	}

	/**
	 * Handles any incoming messages from users
	 */
	private fun handleUserInputs() {
		this.users.forEach { it.receiveData() }
		//TODO: get each user's .input and do something with it if necessary
	}

	/**
	 * A method that doesn't return until the lobby should be terminated
	 * Cleanly closes everything down once the lobby is done
	 */
	fun runUntilDone() {
		while (this.isActive) {
			this.acceptAnyJoiningPlayers()
			this.handleUserInputs()
		}
		shutdown(this.socket, SHUT_RDWR)
		close(this.socket)
	}

}
