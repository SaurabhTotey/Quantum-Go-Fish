const IdentityManager = require("./IdentityManager");
const Message = require("./Message");

//A function that sends a message to an entire lobby: initialized from ConsoleManager in Lobby constructor
let lobbyMessageFunction;

/**
 * A class that represents a lobby
 * A lobby is a list of players waiting for/in a game
 */
class Lobby {

    /**
     * Makes a lobby with the given id
     * Lobby IDs are separate from player IDs
     * Lobby IDs are the position of the lobby in the list of all lobbies
     */
    constructor(lobbyId) {

        this.lobbyId = lobbyId;
        this.playerIds = [];
        this.isJoinable = true;

        /*
         * Takes the lobby message function from the Console Manager
         * ConsoleManager is not a permanent dependency because it would then cause a cyclic dependency
         * Therefore, the lobby message function needs to be initialized in a time afterward, so lazy initialization is used
         * Message function is only loaded after the first lobby is made
         */
        if (lobbyMessageFunction === undefined) {
            lobbyMessageFunction = require("./ConsoleManager").sendMessageToLobby;
        }

    }

    /**
     * Adds the player with the given ID to this lobby
     */
    addPlayer(id) {
        this.playerIds.push(id);
        IdentityManager.ids[id].currentLobbyId = this.lobbyId;
        this.isJoinable = this.playerIds.length < 4;
        this.message(`Please welcome player ${id} to lobby ${this.lobbyId}! The players currently in this lobby are ${this.playerIds}.`);
    }

    /**
     * Removes the player with the given ID from this lobby
     */
    removePlayer(id) {
        IdentityManager.ids[id].currentLobbyId = null;
        this.playerIds.splice(this.playerIds.indexOf(id), 1);
        this.isJoinable = true;
        this.message(`Player ${id} has just left this lobby. The players currently in this lobby are ${this.playerIds}.`);
    }

    /**
     * Sends the given text as a message to all players in this lobby
     */
    message(text) {
        lobbyMessageFunction(this.lobbyId, new Message.Message(Message.defaultSender, "INFO", text));
    }

    /**
     * Makes the lobby enter a game of QGF
     */
    startGame() {
        this.isJoinable = false;
        for (let i = 0 ; i < this.playerIds.length; i++) {
            let playerId = this.playerIds[i];
            //TODO: maybe do something with player's log?
        }
        //TODO: start game
    }

}

//A list of lobbies
let lobbies = [];

/**
 * Creates a lobby with the user with firstId
 * Will try and use an existing empty lobby if possible
 */
function createLobby(firstId) {
    let emptyLobby = lobbies.find(lobby => lobby.playerIds.length === 0);
    if (emptyLobby === undefined) {
        emptyLobby = new Lobby(lobbies.length);
        lobbies.push(emptyLobby);
    }
    emptyLobby.addPlayer(firstId);
}

/**
 * Joins the given id to the best lobby that it can join
 * Will create a lobby if necessary
 */
function matchMake(id) {
    let joinableLobbies = lobbies.filter(lobby => lobby.isJoinable).sort((a, b) => a.playerIds.length - b.playerIds.length);
    if (joinableLobbies.length === 0) {
        createLobby(id);
    } else {
        joinableLobbies[0].addPlayer(id);
    }
}

/**
 * Joins the joiningId to the lobby that alreadyLobbiedId exists in
 * Returns whether the join was successful
 */
function join(joiningId, alreadyLobbiedId) {
    if (!(alreadyLobbiedId in IdentityManager.ids) || IdentityManager.ids[alreadyLobbiedId].currentLobbyId == null || IdentityManager.ids[joiningId].currentLobbyId != null) {
        return false;
    }
    let lobbyToJoin = lobbies[IdentityManager.ids[alreadyLobbiedId].currentLobbyId];
    if (!lobbyToJoin.isJoinable) {
        return false;
    }
    lobbyToJoin.addPlayer(joiningId);
    return true;
}

module.exports = {
    lobbies: lobbies,
    createLobby: createLobby,
    matchMake: matchMake,
    join: join
};
