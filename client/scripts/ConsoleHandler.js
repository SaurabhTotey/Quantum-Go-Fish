
let log = document.getElementById("log");
let input = document.getElementById("user-text-input");

/**
 * A function that defines how to get server data and set the log messages
 */
let updateLog = async () => {
    let response = JSON.parse(await (await fetch(`${window.location.href}api?id=${id}&password=${encodeURIComponent(password)}`, { method: "GET" })).text());
    log.innerHTML = "";
    for (let i = 0; i < response.length; i++) {
        //TODO: message formatting
        log.innerHTML += response[i].message;
        log.appendChild(document.createElement("br"));
    }
};

/**
 * When the submit button is clicked or when enter is typed, the user's input is submitted and the input is cleared
 */
let submitMethod = () => {
    if (input.value === "") {
        return;
    }
    fetch(`${window.location.href}api?id=${id}&password=${encodeURIComponent(password)}&message=${encodeURIComponent(input.value)}`, { method: "POST" });
    input.value = "";
};
document.getElementById("user-submit-button").onclick = submitMethod;
input.onkeyup = keyEvent => {
    if (keyEvent.key === "Enter") {
        submitMethod();
    }
};

/**
 * Sets the log to get updated every 50ms
 */
window.setInterval(() => {
    try {
        updateLog();
    } catch (ignored) {}
}, 50);
