require("dotenv").load();
const express = require("express");
let app = express();
let http = require("http").Server(app);
let io = require("socket.io")(http);

//Serves all public content
app.use(express.static("client"));

/**
 * How each user is handled
 */
io.on("connection", socket => {
    console.log("a user connected");
});

http.listen(process.env.PORT);
