const IdentityManager = require("./IdentityManager");
const Message = require("./Message");
const QGFGame = require("./QGFGame");

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
        this.game = null;
        this.nonReadyPlayers = [];

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
        this.nonReadyPlayers.push(id);
        IdentityManager.ids[id].currentLobbyId = this.lobbyId;
        IdentityManager.ids[id].log = [];
        this.message(`Please welcome player ${id} to lobby ${this.lobbyId}! The players currently in this lobby are ${this.playerIds}.`);
    }

    /**
     * Removes the player with the given ID from this lobby
     */
    removePlayer(id) {
        IdentityManager.ids[id].currentLobbyId = null;
        IdentityManager.ids[id].log = [];
        this.playerIds.splice(this.playerIds.indexOf(id), 1);
        this.nonReadyPlayers = this.playerIds;
        this.isJoinable = true;
        this.game = null;
        this.message(`Player ${id} has just left this lobby. The players currently in this lobby are ${this.playerIds}. Any ongoing games of Quantum Go Fish in this lobby have been ended and all players have been marked unready.`);
    }

    /**
     * Marks the player of the given id as ready to play a game
     */
    readyPlayer(id) {
        if (this.playerIds.length === 1) {
            throw `Please wait for at least one other player to join the lobby before readying up.`;
        }
        if (!this.nonReadyPlayers.includes(id)) {
            throw `Player ${id} is already ready!`;
        }
        this.nonReadyPlayers.splice(this.nonReadyPlayers.indexOf(id), 1);
        if (this.nonReadyPlayers.length > 0) {
            this.message(`Player ${id} is now ready to play QGF! Before the game starts, ${this.nonReadyPlayers} need to ready up.`);
        } else {
            this.startGame();
        }
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
        for (let i = 0; i < this.playerIds.length; i++) {
            IdentityManager.ids[this.playerIds[i]].log = [];
        }
        this.message(`Everyone in the lobby has readied up!`);
        this.game = new QGFGame.QFGGame(this);
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
