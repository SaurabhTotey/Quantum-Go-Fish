package com.saurabhtotey.quantumgofish

import com.saurabhtotey.quantumgofish.network.Client
import com.saurabhtotey.quantumgofish.network.Lobby
import com.saurabhtotey.quantumgofish.network.getSelfAddress
import platform.posix.system

fun main() {
	while (true) {
		//Clears the terminal
		system("setterm -reset")
		//Displays welcome message and starting instructions
		println("Welcome to Quantum Go Fish!\nTo host a lobby, enter \"/host [PLAYER_NAME] [MAX_PLAYERS] [PORT_NUMBER] [PASSWORD]\".\nTo instead join a lobby, enter \"/join [PLAYER_NAME] [HOST_ADDRESS] [PORT_NUMBER] [PASSWORD]\".\nPlayer names must be alphabetic and will be uppercased.\nPort numbers must exceed 1024 and be lower than 65535; default port is 6669.\nDefault amount for maximum number of players is 8.\nIf no password is specified, no password is used.")
		//Reads input to check whether user wants to join a lobby or host a lobby
		var input: String
		while(true) {
			input = readLine() ?: return
			if (input == "/exit") {
				return
			}
			if (input.startsWith("/host ") || input.startsWith("/join ")) {
				break
			}
			println("Sorry, \"$input\" was not understood...")
		}
		//Clears the terminal again and then validates input
		system("setterm -reset")
		val args = input.split(" ")
		if (args.size < 2 || args[1].isEmpty() || args[1].any { !it.isLetter() }) {
			println("Incorrect arguments to ${args[0]}... Press Enter to return...")
			readLine()
			continue
		}
		val name = args[1].toUpperCase()
		val password = if (args.size >= 5) args[4] else ""
		val port = if (args.size >= 4 && args[3].toIntOrNull() != null && 1024 < args[3].toInt() && args[3].toInt() < 65535) args[3].toInt() else 6669
		//More input validation and then creation of a Lobby or Client that controls what happens until user leaves lobby
		if (input.startsWith("/h")) {
			val maxPlayers = if (args.size >= 3 && args[2].toIntOrNull() != null && args[2].toInt() > 1) args[2].toInt() else 8
			println("Creating a lobby as $name which allows up to $maxPlayers players on ${getSelfAddress()}:$port with password $password.")
			Lobby(name, maxPlayers, port, password)
			//TODO: some sort of blocking method
		} else {
			if (args.size < 3) {
				println("No address specified to join... Press Enter to return...")
				readLine()
				continue
			}
			val address = args[2]
			println("Joining the lobby at $address:$port as $name with a password of $password.")
			Client(name, address, port, password)
			//TODO: some sort of blocking method
		}
	}
}
