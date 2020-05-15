package com.saurabhtotey.quantumgofish.network

import com.saurabhtotey.quantumgofish.logic.Game
import kotlinx.cinterop.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import platform.posix.*

/**
 * A class that manages a group of users and running their games
 */
class Lobby(hostName: String, maxPlayers: Int, port: Int, password: String) {

	//Creates a socket that all the clients will connect to
	private val socket = NetworkUtil.createSocket()

	//The actual 15 character password that is used
	private val passString = password.padEnd(15, ' ')

	//A mutex that is supposed to be used whenever modifying the users list
	private val usersModificationMutex = Mutex()

	//All users in the lobby: order corresponds to turn order for the game
	private val users = mutableListOf<User>(HostUser(this, hostName.padEnd(15, ' ')))

	//The current in-progress game if any
	private var game: Game? = null

	//The thread that accepts incoming connections
	private var acceptPlayersThread: Job

	/**
	 * TODO: everything
	 */
	init {
		//Binds the socket to the port given by constructor parameters
		memScoped {
			val addressDescription = NetworkUtil.describeAddress(port)
			if (bind(this@Lobby.socket, addressDescription.ptr.reinterpret(), addressDescription.size.convert()) == -1) {
				throw Error("Could not bind lobby socket to port $port.")
			}
		}
		//Sets the socket to listen for incoming connections
		listen(this@Lobby.socket, maxPlayers)
		//Starts a new thread to accept players in the background
		this.acceptPlayersThread = GlobalScope.launch { while (true) memScoped {
			val clientInfo = cValue<sockaddr_in>()
			val sockAddrInSize = cValuesOf(sizeOf<sockaddr_in>().toUInt())
			val newSocket = accept(this@Lobby.socket, clientInfo.ptr.reinterpret(), sockAddrInSize)
			if (newSocket == -1) {
				return@memScoped
			}
			var isSafeFromTimeout = false
			try {
				val initialResponse = this.allocArray<ByteVar>(30)
				launch {
					delay(1000 * 30)
					if (!isSafeFromTimeout) {
						throw Error("Connection couldn't be accepted in a timely fashion, so it was terminated.")
					}
				}
				recv(newSocket, initialResponse, 30.convert(), 0)
				isSafeFromTimeout = true
				val initialResponseString = initialResponse.toKString()
				val name = initialResponseString.substring(0, 15)
				val givenPassword = initialResponseString.substring(15, 30)
				if (givenPassword != this@Lobby.passString) {
					throw Error("Connection attempted with incorrect password.")
				}
				if (this@Lobby.users.any { it.name == name }) {
					throw Error("Connection attempted with a taken name.")
				}
				val newUser = RemoteClientUser(name, newSocket)
				this@Lobby.usersModificationMutex.withLock {
					this@Lobby.users.forEach { it.sendData("MESSAGETHE UNIVERSE   ${name.trimEnd()} is joining the lobby!") }
					this@Lobby.users.add(newUser)
					newUser.sendData("MESSAGETHE UNIVERSE   Welcome to the lobby!")
					 this@Lobby.users.forEach { it.sendData("MESSAGETHE UNIVERSE   List of players in lobby is now [${this@Lobby.users.joinToString(",") { it.name.trimEnd() }}].") }
				}
			} catch (e: Exception) {
				if (e is Error && e.message != null) {
					send(newSocket, e.message!!.cstr, e.message!!.length.convert(), MSG_DONTWAIT)
				}
				shutdown(newSocket, SHUT_RDWR)
				close(newSocket)
			}
		} }
	}

}
