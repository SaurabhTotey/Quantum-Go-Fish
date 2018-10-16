//A hashmap of id to current game
let ids = {};

/**
 * Makes a unique integer id
 */
function makeId() {
    let i = 0;
    do {
        i++;
    } while (i in ids);
    ids[i] = null;
    return i;
}

module.exports = {
    ids: ids,
    makeId: makeId
};