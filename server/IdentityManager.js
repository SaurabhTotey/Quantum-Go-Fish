
const allLetters = "`1234567890-=~!@#$%^&*()_+qwertyuiop[]\\WERTYUIOP{}|asdfghjkl;'ASDFGHJKL:\"zxcvbnm,./ZXCVBNM<>?";

//A hashmap of id integer to Identity
let ids = {};
//A list of lobbies; TODO: should always have at least one joinable lobby
let lobbies = [new Lobby(0)];

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
        ids[id].currentLobbyId = this.lobbyId;
        this.isJoinable = this.playerIds.length < 4;
        //TODO: maybe do something with player's log?
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

/**
 * A function that makes a random password with 60 random characters
 */
function generatePassword() {
    let password = "";
    for (let i = 0; i < 60; i++) {
        password += allLetters.charAt(Math.floor(Math.random() * allLetters.length));
    }
    return password;
}

/**
 * A class that represents the data that is associated with an id
 */
class Identity {
    constructor(id) {
        this.id = id;
        this.password = generatePassword();
        this.currentLobbyId = null;
        this.log = [];
    }
}

/**
 * Makes a unique integer id
 */
function makeId() {
    let i = 0;
    do {
        i++;
    } while (i in ids);
    ids[i] = new Identity(i);
    return i;
}

module.exports = {
    ids: ids,
    lobbies: lobbies,
    makeId: makeId
};
