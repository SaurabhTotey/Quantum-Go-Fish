let screen = document.getElementById('screen');

//Aspect ratio of the screen
const aspectX = 16;
const aspectY = 9;

//Logical size of the screen
const logicalWidth = aspectX * 100;
const logicalHeight = aspectY * 100;

/**
 * A class that represents a rectangle
 */
class Rectangle {

	constructor(x, y, w, h) {
		this.x = x;
		this.y = y;
		this.w = w;
		this.h = h;
	}

	/**
	 * Returns whether two rectangles intersect
	 * TODO: make
	 */
	intersects() {
		return false;
	}

}

/**
 * Rectangular locations defined in logical coordinates
 */
class ScreenLocation extends Rectangle {

	constructor(logicalX, logicalY, logicalWidth = 1, logicalHeight = 1) {
		super(logicalX, logicalY, logicalWidth, logicalHeight);
	}

	//TODO: getters and setters for coordinates as screen proportions and physical coordinates

}

/**
 * The function that resizes the canvas to handle 
 */
function resizeProcedure() {
	if (window.innerWidth / aspectX < window.innerHeight / aspectY) {
		screen.width = window.innerWidth;
		screen.height = window.innerWidth / aspectX * aspectY;
	} else {
		screen.height = window.innerHeight;
		screen.width = window.innerHeight / aspectY * aspectX;
	}
}

resizeProcedure();
window.onresize = resizeProcedure;