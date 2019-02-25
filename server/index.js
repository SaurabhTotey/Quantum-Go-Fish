require("dotenv").load();
const express = require("express");
const ConsoleManager = require("./ConsoleManager");
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
    IdentityManager.ids[id].log = [new Message.Message(Message.defaultSender, "INFO", "TODO: THESE WILL BE GAME INSTRUCTIONS")];

    /*
     * Sends the user their identity and deletes the identity on user disconnect
     */
    socket.emit("identity", id);
    socket.on("disconnect", () => delete IdentityManager.ids[id]);

    /*
     * Handles receiving messages from the user and routing it to appropriate places
     */
    IdentityManager.ids[id].sendUpdates();
    socket.on("message", message => ConsoleManager.sendUserMessage(id, message));

});

http.listen(process.env.PORT);
