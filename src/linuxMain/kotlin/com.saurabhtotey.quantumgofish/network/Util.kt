package com.saurabhtotey.quantumgofish.network

import kotlinx.cinterop.*
import platform.linux.getifaddrs
import platform.linux.ifaddrs
import platform.posix.AF_INET

fun getSelfAddress(): String {
	memScoped {
		val ifa = this.alloc<ifaddrs>().ptr
		getifaddrs(ifa.reinterpret())
		while (ifa.rawValue != nativeNullPtr) {
			if (ifa[0].ifa_next == null || ifa[0].ifa_next.rawValue == nativeNullPtr) {
				continue
			}
			if (ifa[0].ifa_addr?.get(0)?.sa_family?.toInt() == AF_INET) {

			}
		}
		return "127.0.0.1"
	}
}
