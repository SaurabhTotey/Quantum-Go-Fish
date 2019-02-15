
//Constants for drawing canvas buttons
let imageSize = 100;
let padding = 5;

//Starts loading the images for the canvas buttons
let drawModeButton = new Image();
drawModeButton.src = "../assets/pencil.png";
let eraseModeButton = new Image();
eraseModeButton.src = "../assets/eraser.png";

//Gets the canvas element from the DOM and gets a renderer for it
let canvas = document.getElementById("screen");
canvas.setAttribute("width", `${canvas.offsetWidth}`);
canvas.setAttribute("height", `${canvas.offsetHeight}`);
let renderer = canvas.getContext("2d");

/**
 * Draws canvas buttons on the canvas and prevents drawing over the buttons
 */
window.setInterval(() => {
    renderer.clearRect(0, canvas.height, 3 * padding + 2 * imageSize, 2 * padding + imageSize);
    renderer.strokeRect(padding, canvas.height - padding - imageSize, imageSize, imageSize);
    renderer.strokeRect(2 * padding + imageSize, canvas.height - padding - imageSize, imageSize, imageSize);
    renderer.drawImage(drawModeButton, padding, canvas.height - padding - imageSize, imageSize, imageSize);
    renderer.drawImage(eraseModeButton, 2 * padding + imageSize, canvas.height - padding - imageSize, imageSize, imageSize);
}, 17);
