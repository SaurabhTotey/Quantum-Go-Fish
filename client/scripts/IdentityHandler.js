//The current ID and password of the user
let id = undefined;
let password = undefined;

//Defines and calls an async function to get the id from the api
(async () => {
    let response = JSON.parse(await (await fetch(`${window.location.href}api/id`, { method: "POST" })).text());
    id = parseInt(response.id);
    password = response.password;
})();

/**
 * When the window is left, the user's id is deleted
 */
window.onbeforeunload = async () => {
    fetch(`${window.location.href}api/id?id=${id}&password=${encodeURIComponent(password)}`, { method: "DELETE" });
}
