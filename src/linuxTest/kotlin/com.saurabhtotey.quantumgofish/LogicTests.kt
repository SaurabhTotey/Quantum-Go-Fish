package com.saurabhtotey.quantumgofish

import com.saurabhtotey.quantumgofish.logic.GameObject
import com.saurabhtotey.quantumgofish.logic.GameObjectType
import com.saurabhtotey.quantumgofish.logic.Player
import com.saurabhtotey.quantumgofish.logic.TypeManager
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
 * A test that ensures that if types are filled (their count is 4), they are eliminated from the possibilities for unknown objects by the TypeManager
 */
@Test fun convertUnknownByElimination() {
	val players = List(4) { Player(DummyUser(it)) }
	val typeManager = TypeManager(players)
	//Gives all players an object of type 1, 2, and 3
	players.forEach { player ->
		typeManager.gameObjectTypes.subList(0, 3).forEach { type ->
			player.convertUnknownInto(type)
		}
	}
	//Because types 0, 1, and 2 are filled, the remaining objects must be of type 3
	typeManager.determineKnowableTypes()
	assert(players.all { player -> player.gameObjects.count { it.determinedType == typeManager.gameObjectTypes[3] } == 1 })
}

/**
 * A test that ensure that if the remaining amount of a type means that a certain player must have at some that type, they are given it by the TypeManager
 */
@Test fun convertUnknownByPlayer() {
	val players = List(3) { Player(DummyUser(it)) }
	val typeManager = TypeManager(players)
	//Gives Player 0 2 objects of type 1 and 2 objects of type 2. Gives Player 1 an object of type 1.
	players[0].gameObjects[0].determineType(typeManager.gameObjectTypes[1])
	players[0].gameObjects[1].determineType(typeManager.gameObjectTypes[1])
	players[0].gameObjects[2].determineType(typeManager.gameObjectTypes[2])
	players[0].gameObjects[3].determineType(typeManager.gameObjectTypes[2])
	players[1].gameObjects[0].determineType(typeManager.gameObjectTypes[1])
	//Because Player 1 can only have at most 3 objects of type 0 and Player 0 does not have objects of type 0, Player 2 is guaranteed to have an object of type 0
	typeManager.determineKnowableTypes()
	assertEquals(1, players[2].gameObjects.count { it.determinedType == typeManager.gameObjectTypes[0] })
	//Gives Player 1 an additional object of type 2
	players[1].gameObjects[1].determineType(typeManager.gameObjectTypes[2])
	//Because Player 1 can now only have at most 2 objects of type 0 and Player 0 does noth ave objects of type 0, Player 2 is guaranteed to have 2 objects of type 0
	typeManager.determineKnowableTypes()
	assertEquals(2, players[2].gameObjects.count { it.determinedType == typeManager.gameObjectTypes[0] })
}
