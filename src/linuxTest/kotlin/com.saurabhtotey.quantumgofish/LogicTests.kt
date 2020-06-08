package com.saurabhtotey.quantumgofish

import com.saurabhtotey.quantumgofish.logic.*
import kotlin.test.Test
import kotlin.test.assertEquals

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

/**
 * A test that runs through the sample game on https://stacky.net/wiki/index.php?title=Quantum_Go_Fish and ensures that the output matches
 * This test manually facilitates the game and is more for testing the TypeManager
 */
@Test fun sampleGameManual() {
	val players = List(3) { Player(DummyUser(it)) }
	val typeManager = TypeManager(players)
	//Player A asks Player B if they have any of type 0, and B says yes
	players[0].convertUnknownInto(typeManager.gameObjectTypes[0])
	players[1].convertUnknownInto(typeManager.gameObjectTypes[0])
	players[0].gameObjects.add(players[1].gameObjects.removeAt(players[1].gameObjects.indexOfFirst { it.determinedType == typeManager.gameObjectTypes[0] }))
	typeManager.determineKnowableTypes()
	//Player B asks Player C if they have any of type 0, and C says yes
	players[1].convertUnknownInto(typeManager.gameObjectTypes[0])
	players[2].convertUnknownInto(typeManager.gameObjectTypes[0])
	players[1].gameObjects.add(players[2].gameObjects.removeAt(players[2].gameObjects.indexOfFirst { it.determinedType == typeManager.gameObjectTypes[0] }))
	typeManager.determineKnowableTypes()
	//Player C asks Player A if they have any of type 1, and A says no
	players[2].convertUnknownInto(typeManager.gameObjectTypes[1])
	players[0].gameObjects.filter { it.determinedType == null }.forEach { it.removeTypePossibility(typeManager.gameObjectTypes[1]) }
	typeManager.determineKnowableTypes()
	//Player A asks Player C if they have any of type 2, and C says no
	players[2].gameObjects.filter { it.determinedType == null }.forEach { it.removeTypePossibility(typeManager.gameObjectTypes[2]) }
	typeManager.determineKnowableTypes()
	//Game is finished with the below state
	assert(typeManager.gameObjects.all { it.determinedType != null })
	assertEquals(2, players[0].countOf(typeManager.gameObjectTypes[0]))
	assertEquals(3, players[0].countOf(typeManager.gameObjectTypes[2]))
	assertEquals(2, players[1].countOf(typeManager.gameObjectTypes[0]))
	assertEquals(1, players[1].countOf(typeManager.gameObjectTypes[1]))
	assertEquals(1, players[1].countOf(typeManager.gameObjectTypes[2]))
	assertEquals(3, players[2].countOf(typeManager.gameObjectTypes[1]))
}

/**
 * A test that runs through the sample game on https://stacky.net/wiki/index.php?title=Quantum_Go_Fish and ensures that the output matches
 * This test facilitates the game using the actual Game class as is done during the actual application
 */
@Test fun sampleGameReal() {
	val users = List(3) { DummyUser(it) }
	val game = Game(users)
	//Player A asks Player B if they have any of type 0, and B says yes
	game.ask(users[1], game.typeManager.gameObjectTypes[0])
	game.answer(true)
	assertEquals(null, game.winner)
	//Player B asks Player C if they have any of type 0, and C says yes
	game.ask(users[2], game.typeManager.gameObjectTypes[0])
	game.answer(true)
	assertEquals(null, game.winner)
	//Player C asks Player A if they have any of type 1, and A says no
	game.ask(users[0], game.typeManager.gameObjectTypes[1])
	game.answer(false)
	assertEquals(null, game.winner)
	//Player A asks Player C if they have any of type 2, and C says no
	game.ask(users[2], game.typeManager.gameObjectTypes[2])
	game.answer(false)
	assertEquals(users[0], game.winner)
	//Game is finished with the below state
	assertEquals(2, game.players[0].countOf(game.typeManager.gameObjectTypes[0]))
	assertEquals(3, game.players[0].countOf(game.typeManager.gameObjectTypes[2]))
	assertEquals(2, game.players[1].countOf(game.typeManager.gameObjectTypes[0]))
	assertEquals(1, game.players[1].countOf(game.typeManager.gameObjectTypes[1]))
	assertEquals(1, game.players[1].countOf(game.typeManager.gameObjectTypes[2]))
	assertEquals(3, game.players[2].countOf(game.typeManager.gameObjectTypes[1]))
}
