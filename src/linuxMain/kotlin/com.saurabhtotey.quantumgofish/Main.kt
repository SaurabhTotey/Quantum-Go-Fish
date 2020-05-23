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
	terminalManager.print("Welcome to Quantum Go Fish!\n", TerminalManager.Color.MAGENTA)
	terminalManager.print("To host a lobby, enter \"/host [PLAYER_NAME] [MAX_PLAYERS] [PORT_NUMBER] [PASSWORD]\".\n")
	terminalManager.print("To instead join a lobby, enter \"/join [PLAYER_NAME] [HOST_ADDRESS] [PORT_NUMBER] [PASSWORD]\".\n")
	terminalManager.print("Valid names for players and types are those that are alphabetic and at most 15 characters. All names will be uppercased.\n", TerminalManager.Color.BLUE)
	terminalManager.print("Port numbers must exceed 1024 and be lower than 65535; default port is 6669.\n", TerminalManager.Color.BLUE)
	terminalManager.print("Default amount for maximum number of players is 8.\n", TerminalManager.Color.BLUE)
	terminalManager.print("If no password is specified, an empty password is used. Passwords can be at most 15 characters.\n", TerminalManager.Color.BLUE)
	//TODO: put back in old functionality for program flow
	var flag = false
	while (terminalManager.inputQueue.size < 2) {
		terminalManager.run()
		if (!flag && terminalManager.inputQueue.size > 0) {
			terminalManager.print(terminalManager.inputQueue.first())
			flag = true
		}
	}
	terminalManager.end()
}
