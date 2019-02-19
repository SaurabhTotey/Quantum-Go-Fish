
let log = document.getElementById("log");
let input = document.getElementById("user-text-input");

/**
 * A function that defines how to get server data and set the log messages
 */
let updateLog = async () => {
    let response = JSON.parse(await (await fetch(`${window.location.href}api?id=${id}&password=${encodeURIComponent(password)}`, { method: "GET" })).text());
    log.innerHTML = "";
    for (let i = 0; i < response.length; i++) {
        log.appendChild(document.createTextNode(response[i]))
    }
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
