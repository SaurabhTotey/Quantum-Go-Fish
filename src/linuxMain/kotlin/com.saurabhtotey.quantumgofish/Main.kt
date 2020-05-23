package com.saurabhtotey.quantumgofish

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration
import kotlin.time.ExperimentalTime

/**
 * A method that executes the given action over the given time
 * If the action fails to complete in time, it is interrupted and the failAction is executed
 * Returns whether the action finished execution
 */
@ExperimentalTime fun doActionOnTimeout(timeout: Duration, action: () -> Unit, failAction: () -> Unit = {}): Boolean {
	class TimeOutInternalException : Error()
	return try {
		val errorJob = GlobalScope.launch {
			delay(timeout)
			throw TimeOutInternalException()
		}
		action()
		errorJob.cancel()
		true
	} catch (e: TimeOutInternalException) {
		failAction()
		false
	}
}

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
		terminalManager.print("To host a lobby, enter \"/host [PLAYER_NAME] [MAX_PLAYERS] [PORT_NUMBER] [PASSWORD]\".\n")
		terminalManager.print("To instead join a lobby, enter \"/join [PLAYER_NAME] [HOST_ADDRESS] [PORT_NUMBER] [PASSWORD]\".\n")
		terminalManager.print("Valid names for players and types are those that are alphabetic and at most 15 characters. All names will be uppercased.\n", TerminalManager.Color.BLUE)
		terminalManager.print("Port numbers must exceed 1024 and be lower than 65535; default port is 6669.\n", TerminalManager.Color.BLUE)
		terminalManager.print("Default amount for maximum number of players is 8.\n", TerminalManager.Color.BLUE)
		terminalManager.print("If no password is specified, an empty password is used. Passwords can be at most 15 characters.\n", TerminalManager.Color.BLUE)

		//An input loop that runs until the user enters valid interpretable input
		var input: String
		var isInputValid = false
		inputLoop@ while (!isInputValid) {
			input = ""
			while (input.isEmpty()) {
				terminalManager.run()
				input = terminalManager.input
			}
			if (!input.startsWith("/")) {
				terminalManager.print("Input must be a command while not in a lobby.\n", TerminalManager.Color.RED)
				continue@inputLoop
			}
			if (input == "/exit") {
				break@mainLoop
			}
			if (!input.startsWith("/host ") && !input.startsWith("/join ")) {
				terminalManager.print("Unable to interpret inputted command.\n", TerminalManager.Color.RED)
				continue@inputLoop
			}
			//TODO: validation on input
			isInputValid = true
		}

		//TOOD: create a lobby or a client and run until they are finished

	}
	terminalManager.end()
}
