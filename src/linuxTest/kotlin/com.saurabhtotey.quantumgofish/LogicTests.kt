package com.saurabhtotey.quantumgofish

import com.saurabhtotey.quantumgofish.logic.GameObject
import com.saurabhtotey.quantumgofish.logic.GameObjectType
import kotlin.test.Test
import kotlin.test.assertEquals

//TODO: more tests!!!!

/**
 * A test that ensures that objects determine their type if all but one of their type possibilities is removed
 */
@Test fun typeReductionTest() {
	val testTypes = Array(2) { GameObjectType(it, "Test Type $it") }
	val obj = GameObject(testTypes.toMutableSet())
	obj.removeTypePossibility(testTypes[0])
	assertEquals(testTypes[1], obj.determinedType)
}

/**
 * A test that ensures that if only one player can have a certain type, their unknown objects are converted to that type
 */
@Test fun convertUnknownByPlayer() {

}

/**
 * A test that ensure that if the remaining amount of a type means that a certain player must have at least one of that type, they are given it by the TypeManager
 */
@Test fun convertUnknownByNumber() {

}
