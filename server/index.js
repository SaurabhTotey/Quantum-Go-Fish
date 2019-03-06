require("dotenv").load();
const express = require("express");
const ConsoleManager = require("./ConsoleManager");
const IdentityManager = require("./IdentityManager");
const LobbyManager = require("./LobbyManager");
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
    socket.on("disconnect", () => {
        let identity = IdentityManager.ids[id];
        if (identity.currentLobbyId != null) {
            LobbyManager.lobbies[identity.currentLobbyId].removePlayer(id);
        }
        delete IdentityManager.ids[id];
    });

    /*
     * Handles receiving messages from the user and routing it to appropriate places
     */
    IdentityManager.ids[id].log = [new Message.Message(Message.defaultSender, "INFO", `Welcome to Quantum Go Fish!!! Instructions are described <a href='https://stacky.net/wiki/index.php?title=Quantum_Go_Fish' target="_blank">here</a>. Source code for this website can be found <a href='https://github.com/SaurabhTotey/Quantum-Go-Fish' target="_blank">here</a>. Your ID is ${id}. The canvas is for you to let you keep track of stuff. To join a game lobby, type out '/join any' to join any lobby, '/join [playerId]' to join a specific player, or '/create' to create a new empty lobby.`)];
    IdentityManager.ids[id].sendUpdates();
    socket.on("message", message => ConsoleManager.handleUserMessage(id, message));

});

http.listen(process.env.PORT);
