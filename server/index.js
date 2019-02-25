require("dotenv").load();
const express = require("express");
const IdentityManager = require("./IdentityManager");

let app = express();
let http = require("http").Server(app);
let io = require("socket.io")(http);

//Serves all public content
app.use(express.static("client"));

/**
 * How each user is handled
 */
io.on("connection", socket => {

    /*
     * Makes an identity for the user that just connected
     */
    let id = IdentityManager.makeId();
    let password = IdentityManager.ids[id].password;

    /*
     * Sends the user their identity and deletes the identity on user disconnect
     */
    socket.emit("identity", `{"id":${id},"password":${JSON.stringify(password)}}`);
    socket.on("disconnect", () => delete IdentityManager.ids[id]);

});

http.listen(process.env.PORT);
