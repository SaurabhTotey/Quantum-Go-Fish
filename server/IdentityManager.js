
//A hashmap of id integer to Identity
let ids = {};

/**
 * A class that represents the data that is associated with an id
 */
class Identity {
    constructor(id, socket) {
        this.id = id;
        this.currentLobbyId = null;
        this.socket = socket;
    }
}

/**
 * Makes a unique integer id
 * Takes in the socket of the user
 */
function makeId(socket) {
    let i = 0;
    do {
        i++;
    } while (i in ids);
    ids[i] = new Identity(i, socket);
    return i;
}

module.exports = {
    ids: ids,
    makeId: makeId
};
