package com.saurabhtotey.quantumgofish.network

import kotlin.time.ExperimentalTime

/**
 * The user that is hosting the given lobby
 */
@ExperimentalTime class HostUser constructor(val lobby: Lobby, name: String): User(name) {

	/**
	 * TODO: handle data
	 */
	override fun sendData(data: String) {
		NetworkUtil.interpretIncoming(data)
	}

}
