const IdentityManager = require("./IdentityManager");

/**
 * A class that represents a lobby
 * A lobby is a list of players waiting for/in a game
 */
class Lobby {

    constructor(lobbyId) {
        this.lobbyId = lobbyId;
        this.playerIds = [];
        this.isJoinable = true;
    }

    addPlayer(id) {
        this.playerIds.push(id);
        IdentityManager.ids[id].currentLobbyId = this.lobbyId;
        this.isJoinable = this.playerIds.length < 4;
        //TODO: maybe do something with player's log?
    }

    removePlayer(id) {
        IdentityManager.ids[id].currentLobbyId = null;
        this.playerIds.splice(this.playerIds.indexOf(id), 1);
        this.isJoinable = true;
    }

    startGame() {
        this.isJoinable = false;
        for (let i = 0 ; i < this.playerIds.length; i++) {
            let playerId = this.playerIds[i];
            //TODO: maybe do something with player's log?
        }
        //TODO: start game
    }

}

//A list of lobbies; TODO: should always have at least one joinable lobby
let lobbies = [new Lobby(0)];

/**
 * Creates a lobby with the user with firstId
 */
function createLobby(firstId) {
    //TODO:
}

/**
 * Joins the given id to the best lobby that it can join
 * Will create a lobby if necessary
 */
function matchMake(id) {
    //TODO:
}

/**
 * Joins the joiningId to the lobby that alreadyLobbiedId exists in
 */
function join(joiningId, alreadyLobbiedId) {
    //TODO:
}

module.exports = {
    lobbies: lobbies
};
