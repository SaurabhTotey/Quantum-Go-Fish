package com.saurabhtotey.quantumgofish.network

import com.saurabhtotey.quantumgofish.TerminalManager
import com.saurabhtotey.quantumgofish.TextUtil
import com.saurabhtotey.quantumgofish.logic.Game
import kotlinx.cinterop.*
import platform.posix.*

/**
 * A class that manages a group of users and running their games
 */
class Lobby(private val terminalManager: TerminalManager, hostName: String, maxPlayers: Int, port: Int, private val password: String) {

	//Whether the lobby is/should be running
	private var isActive = true

	//Creates a socket that all the clients will connect to; is a C socket handle
	private val socket = NetworkUtil.createSocket()

	//All users in the lobby: order corresponds to turn order for the game
	private val users = mutableListOf<User>(HostUser(hostName, this.terminalManager))

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
				throw Exception("Could not bind lobby socket to port $port. Make sure that $port is free.")
			}
		}
		//Sets the socket to listen for incoming connections
		if (listen(this@Lobby.socket, maxPlayers) == -1) {
			throw Exception("Could not listen for up to $maxPlayers connections on the socket bound to port $port.")
		}
		this.terminalManager.print("Started a lobby as $hostName on ${NetworkUtil.getSelfAddress()}:$port with password \"$password\" that allows up to $maxPlayers players.\n\n", TerminalManager.Color.BLUE)
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
						throw Exception("Connection couldn't be accepted in a timely fashion, so it was terminated.")
					}
				}
				val parts = initialResponseString.dropLast(1).split('\n')
				val name = parts[0]
				val givenPassword = parts[1]
				if (givenPassword != this@Lobby.password) {
					throw Exception("Connection attempted with incorrect password.")
				}
				if (this@Lobby.users.any { it.name == name }) {
					throw Exception("Connection attempted with a taken name.")
				}
				val newUser = RemoteClientUser(name, newSocket)
				this@Lobby.broadcast("I\n$name is joining the lobby!\n")
				newUser.sendData("I\nWelcome to the lobby!\n")
				this@Lobby.users.add(newUser)
				this@Lobby.broadcast("I\nList of players in lobby is now [${this@Lobby.users.joinToString(", ") { it.name }}].\n")
			} catch (e: Exception) {
				if (e.message != null) {
					val returnMessage = "E\n${e.message!!.replace("\n", " ")}\n"
					send(newSocket, returnMessage.cstr, returnMessage.length.convert(), MSG_DONTWAIT)
				}
				shutdown(newSocket, SHUT_RDWR)
				close(newSocket)
			}
		}
	}

	/**
	 * Sends the given broadcast to all users
	 * Broadcast still needs to conform to being interpretable as a host message
	 */
	private fun broadcast(broadcast: String) {
		if (!TextUtil.isValidHostMessage(broadcast)) {
			throw Error("Cannot broadcast \"$broadcast\" because it is not a valid host message!")
		}
		this.users.forEach { it.sendData(broadcast) }
	}

	/**
	 * A method that handles running a game command safely
	 * Will check for winners or if the game has been lost and also handles exceptions
	 * Returns if the game is done
	 */
	private fun runGameCommand(gameCommand: () -> Unit): Boolean {
		//TODO:
		return false
	}

	/**
	 * Handles any incoming messages from users
	 */
	private fun handleUserInputs() {
		this.users.forEach { it.receiveData() }
		val usersToDisconnect = this.users.filterNot { it.isConnected }
		this.users.removeAll(usersToDisconnect)
		usersToDisconnect.forEach { disconnectedUser -> this.broadcast("I\n${disconnectedUser.name} has disconnected.\n") }
		if (usersToDisconnect.any { this.game?.users?.contains(it) == true }) {
			this.broadcast("E\nThe current game has been stopped.\n")
			this.game = null
		}
		this.users.forEach { user ->
			val sender = user.name
			val input = user.input
			if (input.isBlank()) {
				return@forEach
			}
			if (input == "/leave") {
				if (user !is HostUser) {
					throw Exception("Received a call to '/leave' from a remote user. This should not be possible unless receiving messages from a different program.")
				}
				this.isActive = false
				return
			}
			if (!input.startsWith("/")) {
				this.broadcast("M\n$sender\n$input\n")
				return@forEach
			}
			this.broadcast("C\n$sender\n$input\n")
			val args = input.split(" ")
			when {
				args[0] == "/help" -> {
					this.broadcast("I\nAnything typed in will be interpretted as a chat message unless prepended with a forward-slash.\n")
					this.broadcast("I\nEntering \"/leave\" will allow you to leave the lobby. If a host leaves, the lobby is shutdown.\n")
					this.broadcast("I\nIf anyone leaves during a game, the game is closed.\n")
					this.broadcast("I\nTo start a game, the host must enter \"/start [EASY_MODE]\".\n")
					this.broadcast("I\nThe easy mode argument must be 'y' or 'n', where 'y' means yes and 'n' means no. The default is no.\n")
					this.broadcast("I\nEasy mode just shows the state of the game after every turn.\n")
					this.broadcast("I\nAll players in the lobby will join the game and the turn order will be randomized.\n")
					this.broadcast("I\nTo see the rules of the game, please visit https://stacky.net/wiki/index.php?title=Quantum_Go_Fish.\n")
					this.broadcast("I\nEnter \"/ask [PLAYER_NAME] [TYPE_NAME]\" to ask the given player about the given type.\n")
					this.broadcast("I\nEnter \"/answer [ANSWER]\" to answer any question directed towards yourself. Answer must be 'y' for yes or 'n' for no.\n")
				}
				args[0] == "/start" -> {
					if (this.game != null) {
						this.broadcast("E\nCannot start game while another game is in process.\n")
						return@forEach
					}
					if (user !is HostUser) {
						this.broadcast("E\nOnly the host may start the game.\n")
						return@forEach
					}
					//TODO: easy mode stuff
					val playerOrder = this.users.shuffled()
					this.broadcast("V\nG\nStarting a game with player order [${playerOrder.joinToString(", ") { it.name }}].\n")
					this.game = Game(playerOrder)
					this.broadcast("G\nIt is ${this.game!!.questioner.name}'s turn to ask a question!\n")
				}
				args[0] == "/ask" -> {
					if (this.game == null) {
						this.broadcast("E\nCannot ask a question when no game is in progress.\n")
						return@forEach
					}
					if (user != this.game!!.questioner) {
						this.broadcast("E\nCannot ask a question if it is not your turn to ask a question!\n")
						return@forEach
					}
					//TODO: check that they haven't already asked a question
					if (args.size != 3) {
						this.broadcast("E\nIncorrect number of arguments.\n")
						return@forEach
					}
					if (!this.game!!.users.any { it.name == args[1] }) {
						this.broadcast("E\nThere is no player with the name \"${args[1]}\" in the current game.\n")
						return@forEach
					}
					val validatorResponse = TextUtil.isValidName(args[2])
					if (validatorResponse.isNotEmpty()) {
						this.broadcast("E\nGiven type name is not valid. $validatorResponse\n")
						return@forEach
					}
					var type = this.game!!.typeManager.gameObjectTypes.firstOrNull { it.name == args[2] }
					if (type == null) {
						if (this.runGameCommand { type = this.game!!.typeManager.registerTypeName(args[2]) }) {
							return@forEach
						}
					}
					this.broadcast("G\n${this.game!!.questioner.name} has asked ${args[1]} if they have any objects of type $type.\n")
					this.runGameCommand { this.game!!.ask(this.users.find { it.name == args[1] }!!, type!!) }
				}
				args[0] == "/answer" -> {
					//TODO: do game stuff and print game info
					this.broadcast("I\nTODO\n")
				}
				else -> {
					this.broadcast("E\nWas not able to interpret the command...\n")
				}
			}
		}
	}

	/**
	 * A method that doesn't return until the lobby should be terminated
	 * Cleanly closes everything down once the lobby is done
	 */
	fun runUntilDone() {
		try {
			while (this.isActive) {
				this.acceptAnyJoiningPlayers()
				this.handleUserInputs()
			}
		} finally {
			this.users.filterIsInstance<RemoteClientUser>().forEach {
				it.sendData("E\nThe lobby is being shut down. You are being kicked.")
				shutdown(it.socket, SHUT_RDWR)
				close(it.socket)
			}
			shutdown(this.socket, SHUT_RDWR)
			close(this.socket)
		}
	}

}
