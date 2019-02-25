
let log = document.getElementById("log");
let input = document.getElementById("user-text-input");

/**
 * Handles receiving messages and appending them to the log
 */
socket.on("message", () => {

});

/**
 * When the submit button is clicked or when enter is typed, the user's input is submitted and the input is cleared
 */
let submitMethod = () => {
    if (input.value === "") {
        return;
    }
    socket.emit("message", input.value);
    input.value = "";
};
document.getElementById("user-submit-button").onclick = submitMethod;
input.onkeyup = keyEvent => {
    if (keyEvent.key === "Enter") {
        submitMethod();
    }
};
