//The current ID of the user
let id = undefined;

//Defines and calls an async function to get the id from the api
(async () => {
    id = parseInt(await (await fetch(`${window.location.href}api/id`, { method: "POST" })).text());
})();