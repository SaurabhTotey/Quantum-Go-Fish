const IdentityManager = require("../IdentityManager");

//A reference to the hashmap of IDs in IdentityManager
const userIds = IdentityManager.ids;

//A hashmap of game IDs to games
const games = {};

/**
 * A class that represents an in-progress game
 */
class Game {
    
    /**
     * Makes a game and registers it in the list of games
     * Takes in a host+password and a password for the game; only those with the game's password may join
     */
    constructor(hostId, hostPassword, gamePassword) {
        let i = 0;
        do {
            i++;
        } while (i in games);
        this.password = gamePassword? gamePassword : IdentityManager.generatePassword();
        games[i] = this;
        this.id = i;
        this.players = [];
        this.hasStarted = false;
        this.acceptPlayerJoin(hostId, hostPassword, this.password);
    }

    /**
     * Gets the Id of the game host
     */
    get hostId() { return this.players[0]; }

    /**
     * A function that tries to add a player to this game
     * Returns whether the player successfully joined
     */
    acceptPlayerJoin(id, playerPassword, gamePassword) {
        if (this.hasStarted || userIds[id].password !== playerPassword || this.password !== gamePassword) {
            return false;
        }
        this.players.push(id);
        userIds[id].currentGame = this;
        return true;
    }

    /**
     * A function that handles a user leaving a game
     */
    handlePlayerDisconnect(id, playerPassword) {
        if (userIds[id].password !== playerPassword) {
            return;
        }
        this.players.splice(this.players.indexOf(id), 1);
        userIds[id].currentGame = null;
    }

}