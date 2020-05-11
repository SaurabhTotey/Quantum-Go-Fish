package com.saurabhtotey.quantumgofish.network

/**
 * A definition for what a User is
 * Implementations should handle sending data to the user
 */
abstract class User(val name: String) {

	/**
	 * Sends the inputted data to this user (either over network or locally depending on implementation)
	 */
	abstract fun sendData(data: String)

}
