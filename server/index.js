require("dotenv").load();
const express = require("express");
const IdentityManager = require("./IdentityManager");
const Message = require("./Message");

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
    let id = IdentityManager.makeId(socket);

    /*
     * Sends the user their identity and deletes the identity on user disconnect
     */
    socket.emit("identity", id);
    socket.on("disconnect", () => delete IdentityManager.ids[id]);

    /*
     * Handles receiving messages from the user and routing it to appropriate places
     */
    socket.on("message", message => {
        console.log(`Received "${message}"`);
    });

    socket.emit("message", new Message.Message(Message.defaultSender, "INFO", "TODO: THESE WILL BE GAME INSTRUCTIONS"));

});

http.listen(process.env.PORT);
