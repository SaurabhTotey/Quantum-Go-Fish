package com.saurabhtotey.quantumgofish

import kotlin.time.ExperimentalTime

/**
 * Entry point for the program
 * Handles main flow/logic for the user
 */
@ExperimentalTime fun main() {
	TerminalManager.clear()
	TerminalManager.print("Welcome to Quantum Go Fish!\nTo host a lobby, enter \"/host [PLAYER_NAME] [MAX_PLAYERS] [PORT_NUMBER] [PASSWORD]\".\nTo instead join a lobby, enter \"/join [PLAYER_NAME] [HOST_ADDRESS] [PORT_NUMBER] [PASSWORD]\".\nPlayer names must be alphabetic and will be uppercased. Player names will be truncated at 15 characters.\nPort numbers must exceed 1024 and be lower than 65535; default port is 6669.\nDefault amount for maximum number of players is 8.\nIf no password is specified, an empty password is used. Passwords must be alphanumeric and 15 characters at most.\nIncorrectly entered data may error or be coerced into a correct format.\n")
	TerminalManager.print(TerminalManager.getInput())
	TerminalManager.getInput()
	//TODO: put back in old functionality for program flow
	TerminalManager.end()
}
