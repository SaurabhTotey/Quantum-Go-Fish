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
