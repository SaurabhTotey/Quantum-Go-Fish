package com.saurabhtotey.quantumgofish.network

/**
 * A definition for what a User is
 * Implementations should handle sending data to the user
 */
abstract class User(val name: String) {

	//Should return the first unhandled message received from receiveData: empty string if none
	abstract val input: String

	/**
	 * Sends the inputted data to this user (either over network or locally depending on implementation)
	 */
	abstract fun sendData(data: String)

	/**
	 * Is repeatedly called and should receieve data from this user
	 * Should be non-blocking
	 */
	abstract fun receiveData()

}
