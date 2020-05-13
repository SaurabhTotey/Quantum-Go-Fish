package com.saurabhtotey.quantumgofish.network

import kotlinx.cinterop.*
import platform.linux.getifaddrs
import platform.linux.ifaddrs
import platform.linux.inet_ntop
import platform.posix.AF_INET
import platform.posix.INET_ADDRSTRLEN
import platform.posix.sockaddr_in

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
			if (ifa[0].ifa_next == null || ifa[0].ifa_next.rawValue == nativeNullPtr) {
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
