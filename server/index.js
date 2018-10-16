require("dotenv").load();
const express = require("express");
const identityManager = require("./identityManager");

//Sets the port of the app
const app = express();
app.listen(process.env.PORT);

//Serves all public content
app.use(express.static('client'));

/**
 * Makes and replies with a new id
 */
app.post('/api/id', (req, res) => {
    res.send(`${identityManager.makeId()}`);
});
