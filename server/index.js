require("dotenv").load();
const express = require("express");
const IdentityManager = require("./IdentityManager");
const ConsoleManager = require("./ConsoleManager");

//Sets the port of the app
const app = express();
app.listen(process.env.PORT);

//Serves all public content
app.use(express.static('client'));

/**
 * Makes and replies with a new id
 */
app.post("/api/id", (req, res) => {
    let newId = IdentityManager.makeId();
    res.send(`{"id":${newId},"password":${JSON.stringify(IdentityManager.ids[newId].password)}}`);
});

/**
 * Deletes a given id if the password is correct
 * Returns whether an id was deleted or not
 */
app.delete("/api/id", (req, res) => {
    try {
        let id = parseInt(req.query["id"]);
        if (req.query["password"] === IdentityManager.ids[id].password) {
            delete IdentityManager.ids[id];
            res.send(true);
        } else {
            res.send(false);
        }
    } catch (ignored) { res.send(false); }
});

/**
 * Gets the console for the game of the given player id
 */
app.get("/api", (req, res) => {
    try {
        let id = parseInt(req.query["id"]);
        if (req.query["password"] === IdentityManager.ids[id].password) {
            if (IdentityManager.ids[id].currentGame == null) {
                res.send([ConsoleManager.makeInfoMessage("TODO: general game instructions will go here.")]);
            } else {
                res.send(IdentityManager.ids[id].log);
            }
        } else {
            res.send([ConsoleManager.makeErrorMessage()]);
        }
    } catch (ignored) { res.send([ConsoleManager.makeErrorMessage()]); }
});

/**
 * Tries to interpret user commands to do whatever chat command was sent
 * Allows the user to post to the chat when in game
 */
app.post("/api", (req, res) => {
    try {
        let id = parseInt(req.query["id"]);
        if (req.query["password"] === IdentityManager.ids[id].password) {
            //TODO: try and interpret commands
            ConsoleManager.pushChatMessage(req.query["message"], id, false);
        } else {
            res.send([ConsoleManager.makeErrorMessage()]);
        }
    } catch (ignored) { res.send([ConsoleManager.makeErrorMessage()]); }
});
