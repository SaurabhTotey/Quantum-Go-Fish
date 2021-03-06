package com.saurabhtotey.quantumgofish

import platform.linux.inet_addr
import platform.posix.INADDR_NONE

/**
 * A utility object that is used to validate given text
 * All methods return a string that is either empty if the input is valid, or the reason why the input was invalid
 */
object TextUtil {

	/**
	 * Returns whether the input string can be used as a port
	 */
	fun isValidPort(input: String): String {
		val numberInput = input.toIntOrNull() ?: return "Port is not a number."
		if (1024 >= numberInput || numberInput >= 65535) {
			return "Port is not in allowable range."
		}
		return ""
	}

	/**
	 * Returns whether the input string can be used as a name for a player or a type
	 */
	fun isValidName(input: String): String {
		if (input.any{ !it.isLetter() }) {
			return "Name contains non-alphabetic characters."
		}
		if (input.isEmpty()) {
			return "Name does not exist."
		}
		if (input.length > 15) {
			return "Name is longer than max of 15 characters."
		}
		if (input.any { it.isLowerCase() }) {
			return "Name is not upper-cased."
		}
		return ""
	}

	/**
	 * Returns whether the input string can be used as a password for a lobby
	 */
	fun isValidPassword(input: String): String {
		if (input.length > 15) {
			return "Password is longer than max of 15 characters."
		}
		return ""
	}

	/**
	 * Returns whether the input can be used as a maximum number of players
	 */
	fun isValidNumberOfMaxPlayers(input: String): String {
		val numberInput = input.toIntOrNull() ?: return "Max number of players is not a number."
		if (numberInput < 2) {
			return "Max number of players needs to be at least 2."
		}
		if (numberInput > 16) {
			return "Max number of players cannot exceed 16."
		}
		return ""
	}

	/**
	 * Returns whether the input can be be used as an address
	 */
	fun isValidAddress(input: String): String {
		if (inet_addr(input) == INADDR_NONE) { //TODO: convert to inet_aton instead so it doesn't say -1 is invalid because it is a valid address
			return "Address is invalid."
		}
		return ""
	}

	/**
	 * Returns whether the input is a complete message from the host that can be interpretted
	 */
	fun isValidHostMessage(input: String): Boolean {
		if (!input.endsWith("\n")) {
			return false
		}
		if (input.length < 2 || input[1] != '\n') {
			return false
		}
		val separatorCount = input.count { it == '\n' }
		val args = input.split("\n")
		if (input == "V\n") {
			return true
		}
		if  (input.startsWith("I") && separatorCount == 2) {
			return true
		}
		if (input.startsWith("M") && separatorCount == 3 && this.isValidName(args[1]).isBlank()) {
			return true
		}
		if (input.startsWith("C") && separatorCount == 3 && this.isValidName(args[1]).isBlank()) {
			return true
		}
		if (input.startsWith("E") && separatorCount == 2) {
			return true
		}
		if (input.startsWith("G") && separatorCount == 2) {
			return true
		}
		return false
	}

	/**
	 * Tries to interpret the given string as a boolean
	 * null if the string cannot be interpretted
	 */
	fun interpretAsBoolean(input: String): Boolean? {
		if (input.toLowerCase() in setOf("y", "yes", "t", "true")) {
			return true
		} else if (input.toLowerCase() in setOf("n", "no", "f", "false")) {
			return false
		}
		return null
	}

}
