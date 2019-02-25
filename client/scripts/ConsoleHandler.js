
let log = document.getElementById("log");
let input = document.getElementById("user-text-input");

//TODO: update log when receiving something from socket

/**
 * When the submit button is clicked or when enter is typed, the user's input is submitted and the input is cleared
 */
let submitMethod = () => {
    if (input.value === "") {
        return;
    }
    //TODO: submit input.value
    input.value = "";
};
document.getElementById("user-submit-button").onclick = submitMethod;
input.onkeyup = keyEvent => {
    if (keyEvent.key === "Enter") {
        submitMethod();
    }
};
