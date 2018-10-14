require("dotenv").load();
const express = require("express");
const identityManager = require("./identityManager");

//Sets the port of the app
const app = express();
app.listen(process.env.PORT);

//Serves all public content
app.use(express.static('client'));

//Serves new IDs when requested; if id is not generated, nothing is returned, otherwise, generated id is returned
app.post('/api/id', (req, res) => {
    let createdId;
    if (req.query["desiredId"] != undefined) {
        createdId = identityManager.makeId(req.query["desiredId"]);
    } else {
        createdId = identityManager.makeId();
    }
    res.send(createdId);
});
