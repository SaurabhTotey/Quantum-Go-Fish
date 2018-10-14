//A hashmap of id to current game
let ids = {};

/**
 * Tries to make a new id with the given name
 * Returns id if unique or empty string on failure
 * If requested id is undefined, a new unique guest id is generated
 */
function makeId(id) {
    if (id == undefined) {
        let i = 0;
        let generatedId;
        do {
            generatedId = `Guest#${i}`;
            i++;
        } while (generatedId in ids);
        ids[generatedId] = null;
        return generatedId;
    }
    if (id in ids) {
        return '';
    }
    ids[id] = null;
    return id;
}

module.exports = {
    ids: ids,
    makeId: makeId
};