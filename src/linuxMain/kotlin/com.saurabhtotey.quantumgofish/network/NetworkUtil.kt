package com.saurabhtotey.quantumgofish.network

import com.saurabhtotey.quantumgofish.TerminalManager
import kotlinx.cinterop.*
import platform.linux.getifaddrs
import platform.linux.ifaddrs
import platform.linux.inet_addr
import platform.linux.inet_ntop
import platform.posix.*

/**
 * An object that handles the common yucky C methods methods and wraps them in nicer functions
 * Also just handles common networking stuff
 * Is meant for use by Lobby and Client, but doesn't handle all the C methods that might be needed by either of them
 */
object NetworkUtil {

	/**
	 * Repeatedly calls recv and returns the built up message if any
	 * If no message from recv, returns an empty string
	 * Is non-blocking
	 * Assumes that messages are always sent in their entirety: will freeze if a complete message cannot be received
	 */
	fun receiveIncomingFrom(socketHandle: Int): String {
		//TODO:
		return ""
	}

	/**
	 * Runs/interprets the given message that came from the host
	 */
	fun handleMessageFromHost(messageFromHost: String, terminalManager: TerminalManager) {

	}

	/**
	 * Gets the IPV4 address of the current machine
	 * Is basically a Kotlin native implementation of https://stackoverflow.com/questions/212528/get-the-ip-address-of-the-machine
	 */
	fun getSelfAddress(): String {
		memScoped {
			val startOfAddressList = this.alloc<ifaddrs>().ptr
			var ifa = startOfAddressList
			getifaddrs(ifa.reinterpret())
			while (ifa.rawValue != nativeNullPtr) {
				if (ifa[0].ifa_addr == null || ifa[0].ifa_addr.rawValue == nativeNullPtr) {
					ifa = ifa[0].ifa_next ?: break
					continue
				}
				if (ifa[0].ifa_addr?.get(0)?.sa_family?.toInt() == AF_INET) {
					val addressPointer = (ifa[0].ifa_addr!! as CPointer<sockaddr_in>)[0].sin_addr.ptr
					val addressCString = this.allocArray<ByteVar>(INET_ADDRSTRLEN)
					inet_ntop(AF_INET, addressPointer, addressCString, INET_ADDRSTRLEN)
					val addressString = addressCString.toKString()
					if (addressString != "127.0.0.1") {
						return addressString
					}
				}
				ifa = ifa[0].ifa_next ?: break
			}
			return "127.0.0.1"
		}
	}

	/**
	 * Creates a socket and returns the socket description integer
	 */
	fun createSocket(): Int {
		val socketDescription = socket(AF_INET, SOCK_STREAM, 0)
		if (socketDescription == -1) {
			throw Error("Unable to create socket.")
		}
		this.setIsSocketBlocking(socketDescription, false)
		return socketDescription
	}

	/**
	 * Makes the given socket either blocking or non-blocking
	 */
	fun setIsSocketBlocking(socketHandle: Int, isBlocking: Boolean) {
		val currentSocketFlags = fcntl(socketHandle, F_GETFL)
		val newFlags = if (isBlocking) currentSocketFlags and O_NONBLOCK.inv() else currentSocketFlags or O_NONBLOCK
		if (currentSocketFlags == -1 || fcntl(socketHandle, F_SETFL, newFlags) == -1) {
			throw Error("Could not set socket $socketHandle to be ${if (isBlocking) "blocking" else "non-blocking"}.")
		}
	}

	/**
	 * Gets a CValue for an IPV4 socket address
	 */
	fun describeAddress(port: Int, address: String = ""): CValue<sockaddr_in> {
		return cValue {
			this.sin_family = AF_INET.convert()
			this.sin_port = htons(port.toUShort())
			this.sin_addr.s_addr = if (address.isEmpty()) INADDR_ANY else inet_addr(address)
		}
	}

}
