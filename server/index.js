require("dotenv").load();
const express = require("express");
const app = express();

//Sets the port of the app
app.listen(process.env.PORT);

//Serves all public content
app.use(express.static('client'));