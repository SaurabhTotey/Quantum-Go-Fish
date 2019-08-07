
//Constants for drawing canvas buttons
let imageSize = 100;
let padding = 5;

/**
 * A class that represents a button and defines its bounding box and how it is drawn and how it behaves
 */
class Button {

	constructor(imageSrc, x, y, w, h) {
		this.image = new Image();
		this.image.src = imageSrc;
		this.x = x;
		this.y = y;
		this.w = w;
		this.h = h;
		this.isHovered = false;
		this.isSelected = false;
	}

	draw(renderer) {
		renderer.fillStyle = "white";
		if (this.isSelected) {
			renderer.fillStyle = "#0074D9";
		} else if (this.isHovered) {
			renderer.fillStyle = "#7FDBFF";
		}
		renderer.fillRect(this.x, this.y, this.w, this.h);
		renderer.strokeRect(this.x, this.y, this.w, this.h);
		renderer.drawImage(this.image, this.x, this.y, this.w, this.h);
	}

	contains(x, y) {
		return x > this.x && x < this.x + this.w && y > this.y && y < this.y + this.h;
	}

}

//Sets up the canvas element
let canvas = document.getElementById("screen");
canvas.setAttribute("width", `${canvas.offsetWidth}`);
canvas.setAttribute("height", `${canvas.offsetHeight}`);
let renderer = canvas.getContext("2d");
canvas.strokeStyle = "black";

//Starts loading the images for the canvas buttons and sets their positions
let drawModeButton = new Button("../assets/pencil.png", padding, canvas.height - padding - imageSize, imageSize, imageSize);
let eraseModeButton = new Button("../assets/eraser.png", 2 * padding + imageSize, canvas.height - padding - imageSize, imageSize, imageSize);
drawModeButton.isSelected = true;

/**
 * Sets a procedure for how the canvas elements should get resized
 */
window.onresize = () => {
	// TODO: canvas resizing is janky
	// canvas.setAttribute("width", `${canvas.offsetWidth}`);
	// canvas.setAttribute("height", `${canvas.offsetHeight}`);
	drawModeButton.x = padding;
	drawModeButton.y = canvas.height - padding - imageSize;
	drawModeButton.w = imageSize;
	drawModeButton.h = imageSize;
	eraseModeButton.x = 2 * padding + imageSize;
	eraseModeButton.y = canvas.height - padding - imageSize;
	eraseModeButton.w = imageSize;
	eraseModeButton.h = imageSize;
};

//Initializes variables used to keep track of the user's drawing on the canvas
let previousPoint = null;
canvas.onmousedown = mouseEvent => previousPoint = { x: mouseEvent.x, y: mouseEvent.y };
window.onmouseup = () => previousPoint = null;

/**
 * Checks where the mouse is and whether it is hovering over any button
 * Draws if the mouse is clicked
 * TODO: mouseEvent.x and mouseEvent.y are janky on window resizes
 */
canvas.onmousemove = mouseEvent => {
	drawModeButton.isHovered = drawModeButton.contains(mouseEvent.x, mouseEvent.y);
	eraseModeButton.isHovered = eraseModeButton.contains(mouseEvent.x, mouseEvent.y);
	if (drawModeButton.isHovered || eraseModeButton.isHovered) {
		canvas.style.cursor = "pointer";
	} else {
		canvas.style.cursor = "default";
	}
	if (previousPoint != null) {
		if (drawModeButton.isSelected) {
			renderer.beginPath();
			renderer.moveTo(previousPoint.x, previousPoint.y);
			renderer.lineTo(mouseEvent.x, mouseEvent.y);
			renderer.closePath();
			renderer.stroke();
			previousPoint = { x: mouseEvent.x, y: mouseEvent.y };
		} else if (eraseModeButton.isSelected) {
			renderer.clearRect(previousPoint.x, previousPoint.y, mouseEvent.x - previousPoint.x, mouseEvent.y - previousPoint.y);
		}
	}
};

/**
 * Checks whether the user clicked on any button
 */
canvas.onclick = mouseEvent => {
	if (drawModeButton.contains(mouseEvent.x, mouseEvent.y)) {
		drawModeButton.isSelected = true;
		eraseModeButton.isSelected = false;
	} else if (eraseModeButton.contains(mouseEvent.x, mouseEvent.y)) {
		drawModeButton.isSelected = false;
		eraseModeButton.isSelected = true;
	}
};

//Allows touching to have the same functionality of the mouse TODO: this doesn't work
function convertToPoint(touchEvent) {
	return new { x: touchEvent.clientX, y: touchEvent.clientY };
}
canvas.ontouchstart = touchEvent => {
	touchEvent.preventDefault();
	canvas.onmousedown(convertToPoint(touchEvent));
	canvas.onclick(convertToPoint(touchEvent));
};
canvas.ontouchend = touchEvent => {
	touchEvent.preventDefault();
	canvas.onmouseup(convertToPoint(touchEvent));
};
canvas.ontouchcancel = touchEvent => {
	touchEvent.preventDefault();
	canvas.onmouseup(convertToPoint(touchEvent));
};
canvas.ontouchmove = touchEvent => {
	touchEvent.preventDefault();
	canvas.onmousemove(convertToPoint(touchEvent));
};

/**
 * Draws canvas buttons on the canvas and prevents drawing over the buttons
 */
window.setInterval(() => {
	drawModeButton.draw(renderer);
	eraseModeButton.draw(renderer);
}, 17);
