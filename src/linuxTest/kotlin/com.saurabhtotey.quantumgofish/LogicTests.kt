package com.saurabhtotey.quantumgofish

import com.saurabhtotey.quantumgofish.logic.GameObject
import com.saurabhtotey.quantumgofish.logic.GameObjectType
import kotlin.test.Test
import kotlin.test.assertEquals

//TODO: more tests!!!!

/**
 * A test that ensures that objects determine their type if all but one of their type possibilities is removed
 */
@Test
fun typeReductionTest() {
	val testTypes = Array(4) { GameObjectType(it, "Test Type $it") }
	val obj = GameObject(testTypes.toMutableSet())
	(0 until 3).forEach { obj.removeTypePossibility(testTypes[it]) }
	assertEquals(testTypes[3], obj.determinedType)
}
