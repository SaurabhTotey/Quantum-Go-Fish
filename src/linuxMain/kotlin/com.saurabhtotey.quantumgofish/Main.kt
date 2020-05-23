package com.saurabhtotey.quantumgofish

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
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
@ExperimentalTime fun main() = runBlocking {
	val terminalManager = TerminalManager()
	terminalManager.clear()
	terminalManager.print("Welcome to Quantum Go Fish!\nTo host a lobby, enter \"/host [PLAYER_NAME] [MAX_PLAYERS] [PORT_NUMBER] [PASSWORD]\".\nTo instead join a lobby, enter \"/join [PLAYER_NAME] [HOST_ADDRESS] [PORT_NUMBER] [PASSWORD]\".\nPlayer names must be alphabetic and will be uppercased. Player names will be truncated at 15 characters.\nPort numbers must exceed 1024 and be lower than 65535; default port is 6669.\nDefault amount for maximum number of players is 8.\nIf no password is specified, an empty password is used. Passwords must be alphanumeric and 15 characters at most.\nIncorrectly entered data may error or be coerced into a correct format.\n")
	//TODO: put back in old functionality for program flow
	while (terminalManager.inputQueue.size < 2) {
		terminalManager.manageInput()
	}
	terminalManager.end()
}
