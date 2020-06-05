package com.saurabhtotey.quantumgofish

import com.saurabhtotey.quantumgofish.network.Client
import com.saurabhtotey.quantumgofish.network.Lobby

/**
 * Entry point for the program
 * Handles main flow/logic for the user
 */
fun main() {
	val terminalManager = TerminalManager()
	mainLoop@ while (true) {

		//Prints initial instructions
		terminalManager.clear()
		terminalManager.print("Welcome to Quantum Go Fish!\n", TerminalManager.Color.MAGENTA)
		terminalManager.print("\n")
		terminalManager.print("To host a lobby, enter \"/host [PLAYER_NAME] [MAX_PLAYERS] [PORT_NUMBER] [PASSWORD]\".\n")
		terminalManager.print("To instead join a lobby, enter \"/join [PLAYER_NAME] [HOST_ADDRESS] [PORT_NUMBER] [PASSWORD]\".\n")
		terminalManager.print("\n")
		terminalManager.print("Valid names for players and types are those that are alphabetic and at most 15 characters. All names will be uppercased.\n", TerminalManager.Color.BLUE)
		terminalManager.print("Port numbers must exceed 1024 and be lower than 65535; default port is 6669.\n", TerminalManager.Color.BLUE)
		terminalManager.print("Default amount for maximum number of players is 8.\n", TerminalManager.Color.BLUE)
		terminalManager.print("If no password is specified, an empty password is used. Passwords can be at most 15 characters.\n", TerminalManager.Color.BLUE)
		terminalManager.print("\n")
		terminalManager.print("For help, enter \"/help\".\n")
		terminalManager.print("To quit, enter \"/exit\". This command is disabled while in a lobby.\n")
		terminalManager.print("If you are in a lobby, you may enter \"/leave\" to return back to this menu. Leaving a game will stop the game for everyone. Leaving a lobby as a host closes the lobby.\n")
		terminalManager.print("\n")

		//An input loop that runs until the user enters valid interpretable input
		var input: String? = null
		var isInputValid = false
		inputLoop@ while (!isInputValid) {
			input = null
			while (input == null) {
				terminalManager.run()
				input = terminalManager.input
			}
			terminalManager.print("$input\n", TerminalManager.Color.GREEN)
			if (!input.startsWith("/")) {
				terminalManager.print("Input must be a command while not in a lobby.\n", TerminalManager.Color.RED)
				continue@inputLoop
			}
			if (input == "/exit") {
				break@mainLoop
			}
			if (input == "/help") {
				terminalManager.print("TODO: this is supposed to be some sort of help message.\n")
				continue@inputLoop
			}
			if (!input.startsWith("/host ") && !input.startsWith("/join ")) {
				terminalManager.print("Unable to interpret inputted command.\n", TerminalManager.Color.RED)
				continue@inputLoop
			}
			var args = input.split(" ")
			if (input.startsWith("/host") && args.size < 2 || input.startsWith("/join") && args.size < 3 || args.size > 5) {
				terminalManager.print("Incorrect number of arguments.\n", TerminalManager.Color.RED)
				continue@inputLoop
			}
			if (input.startsWith("/host") && args.size < 3) {
				input += " 8"
			}
			if (args.size < 4) {
				input += " 6669"
			}
			if (args.size < 5) {
				input += " "
			}
			args = input.split(" ")
			val validNameResponse = TextUtil.isValidName(args[1].toUpperCase())
			if (validNameResponse.isNotBlank()) {
				terminalManager.print("$validNameResponse\n", TerminalManager.Color.RED)
				continue@inputLoop
			}
			val validPortResponse = TextUtil.isValidPort(args[3])
			if (validPortResponse.isNotBlank()) {
				terminalManager.print("$validPortResponse\n", TerminalManager.Color.RED)
				continue@inputLoop
			}
			val validPasswordResponse = TextUtil.isValidPassword(args[4])
			if (validPasswordResponse.isNotBlank()) {
				terminalManager.print("$validPasswordResponse\n", TerminalManager.Color.RED)
				continue@inputLoop
			}
			if (input.startsWith("/host")) {
				val validMaxPlayersResponse = TextUtil.isValidNumberOfMaxPlayers(args[2])
				if (validMaxPlayersResponse.isNotBlank()) {
					terminalManager.print("$validMaxPlayersResponse\n", TerminalManager.Color.RED)
					continue@inputLoop
				}
			} else {
				val validAddressResponse = TextUtil.isValidAddress(args[2])
				if (validAddressResponse.isNotBlank()) {
					terminalManager.print("$validAddressResponse\n", TerminalManager.Color.RED)
					continue@inputLoop
				}
			}
			isInputValid = true
		}

		//Now that all that input has been validated, start the game
		val validArgs = input!!.split(" ")
		val playerName = validArgs[1].toUpperCase()
		val port = validArgs[3].toInt()
		val password = validArgs[4]
		terminalManager.clear()
		try {
			if (input.startsWith("/host")) {
				Lobby(terminalManager, playerName, validArgs[2].toInt(), port, password).runUntilDone()
			} else {
				Client(terminalManager, playerName, validArgs[2], port, password).runUntilDone()
			}
		} catch (e: Exception) {
			terminalManager.print("${e.message ?: "An Exception was thrown with no message."}\n", TerminalManager.Color.RED)
			terminalManager.print("Press enter to continue...", TerminalManager.Color.RED)
			while (terminalManager.input == null) {
				terminalManager.run()
			}
		}

	}
	terminalManager.end()
}
