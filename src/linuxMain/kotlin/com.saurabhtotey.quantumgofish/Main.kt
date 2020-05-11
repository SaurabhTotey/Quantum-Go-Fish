package com.saurabhtotey.quantumgofish

import com.saurabhtotey.quantumgofish.network.Client
import com.saurabhtotey.quantumgofish.network.Lobby
import platform.posix.system

fun main() {
	while (true) {
		//Clears the terminal
		system("setterm -reset")
		//Displays welcome message and starting instructions
		println("Welcome to Quantum Go Fish! TODO: I need to write instructions here!")
		//Reads input to check whether user wants to join a lobby or host a lobby
		var input: String
		do {
			input = readLine() ?: return
			if (input == "/exit") {
				return
			}
		} while(!input.startsWith("/host") && !input.startsWith("/join"))
		//TODO: extract information from input about hosting or joining
		//Clears the terminal again and then let's Lobby or Client control what happens until user leaves lobby
		system("setterm -reset")
		if (input.startsWith("/h")) {
			Lobby("TODO:")
			//TODO: some sort of blocking method
		} else {
			Client()
			//TODO: some sort of blocking method
		}
	}
}
