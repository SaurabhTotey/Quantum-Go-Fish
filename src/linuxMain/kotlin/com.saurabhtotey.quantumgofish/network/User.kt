package com.saurabhtotey.quantumgofish.network

/**
 * A definition for what a User is
 * Implementations should handle sending data to and from the user
 * TODO: figure out how to handle monitoring whether the user is still connected/active
 *  maybe a lobby class could repeatedly ping the user with sendData and wait some fixed amount of time for getResponse
 */
abstract class User {

	//A unique identifier for this user: TODO: figure out what to use (worst comes to worst, ask user for a name or something)
	abstract val id: String

	/**
	 * TODO: make this method take an input
	 * Sends the inputted data to this user (either over network or locally depending on implementation)
	 */
	abstract fun sendData()

	/**
	 * TODO: make this method return some sort of response data
	 * A blocking method that awaits a response from this user and then returns it
	 */
	abstract fun getResponse()

}
