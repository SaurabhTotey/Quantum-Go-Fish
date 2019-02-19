const allLetters = "`1234567890-=~!@#$%^&*()_+qwertyuiop[]\\WERTYUIOP{}|asdfghjkl;'ASDFGHJKL:\"zxcvbnm,./ZXCVBNM<>?";

//A hashmap of id integer to Identity
let ids = {};

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
        this.currentGame = null;
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
    makeId: makeId
};
