package com.saurabhtotey.quantumgofish.logic

import platform.posix.isalpha

/**
 * TODO:
 */
class TypeManager(numberTypes: Int) {
	val typeNames = MutableList(numberTypes) { "?" }
	val objects = List(numberTypes * 4) { Object() }

	/**
	 * Registers the given string as a name for one of the types for objects in the owning game
	 * name must be an alphabetic string (only characters from the alphabet) and must be upper-cased
	 */
	fun registerTypeName(name: String) {
		if (name.any { isalpha(it.toInt()) == 0 }) {
			throw Error("Cannot register \"$name\" because it contains non-alphabetic characters.")
		}
		if (name != name.toUpperCase()) {
			throw Error("Cannot register \"$name\" because it is not entirely upper-cased.")
		}
		if (name in typeNames) {
			throw Error("Cannot register \"$name\" because it is already registered.")
		}
		val updatedIndex = typeNames.indexOf("?")
		typeNames[updatedIndex] = name
	}
}
