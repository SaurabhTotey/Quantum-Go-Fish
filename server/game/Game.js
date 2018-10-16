//A reference to the hashmap of IDs in IdentityManager
const userIds = require("../IdentityManager").ids;

//A hashmap of game IDs to games
const games = {};

/**
 * A class that represents an in-progress game
 */
class Game {
    
    /**
     * Makes a game and registers it in the list of games
     */
    constructor() {
        let i = 0;
        do {
            i++;
        } while (i in games);
        games[i] = this;
        this.id = i;
        this.players = []; //No players until they join
        this.hasStarted = false; //Games don't start until all players are ready
    }

}