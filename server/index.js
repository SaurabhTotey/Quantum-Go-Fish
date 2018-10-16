require("dotenv").load();
const express = require("express");
const IdentityManager = require("./IdentityManager");

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
