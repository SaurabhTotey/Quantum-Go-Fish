package com.saurabhtotey.quantumgofish.network

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

	fun interpretIncoming(incoming: String) {
		if (incoming.startsWith("MESSAGE")) {
			println("${incoming.substring(7, 22)}: ${incoming.substring(22)}")
		}
	}

	//TODO: send method

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
		return socketDescription
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
