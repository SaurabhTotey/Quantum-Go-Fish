
let log = document.getElementById("log");
let input = document.getElementById("user-text-input");

/**
 * A function that defines how to get server data and set the log messages
 */
let updateLog = () => {
    //TODO: 
};

/**
 * When the submit button is clicked, the user's input is submitted and the input is cleared
 */
document.getElementById("user-submit-button").onclick = () => {
    //TODO: submit text from input and clear input
};

/**
 * Sets the log to get updated every 50ms
 */
window.setInterval(() => {
    try {
        updateLog();
    } catch (ignored) {}
}, 50);
