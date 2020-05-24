package com.saurabhtotey.quantumgofish.network

/**
 * The user that is hosting the given lobby
 */
class HostUser constructor(val lobby: Lobby, name: String): User(name) {

	/**
	 * TODO: handle data
	 */
	override fun sendData(data: String) {
		NetworkUtil.interpretIncoming(data)
	}

}
