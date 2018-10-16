//The current ID and password of the user
let id = undefined;
let password = undefined;

//Defines and calls an async function to get the id from the api
(async () => {
    let response = JSON.parse(await (await fetch(`${window.location.href}api/id`, { method: "POST" })).text());
    id = parseInt(response.id);
    password = response.password;
})();