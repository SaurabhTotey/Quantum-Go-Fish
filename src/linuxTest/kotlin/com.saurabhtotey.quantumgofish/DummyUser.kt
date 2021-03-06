package com.saurabhtotey.quantumgofish

import com.saurabhtotey.quantumgofish.network.User

/**
 * A class that is only intended for testing purposes
 * Is a User sub-class that doesn't actually do anything
 * index parameter just for naming/debugging
 * None of the DummyUser methods should be called other than the id field
 */
class DummyUser(index: Int) : User("Dummy User $index") {

	override val input: String
		get() = throw Error("input should not be called because $this is a DummyUser.")

	override fun sendData(data: String) {
		throw Error("sendData should not be called because $this is a DummyUser.")
	}

	override fun receiveData() {
		throw Error("receiveData should not be called because $this is a DummyUser.")
	}

}
