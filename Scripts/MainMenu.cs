using Godot;
using Steamworks;

/**
 * Main menu
 */
public class MainMenu : Node {

	/**
	 * When the main menu loads up, connect up the create and join buttons to the OnMenuButtonPressed method
	 * Also ensure that the user is set to be neither a host nor a client
	 */
	public override void _Ready() {
		GD.Print("Entering Main Menu.");
		Global.getFrom(this).IsHost = null;
		this.GetNode("LobbyCreateButton").Connect("pressed", this, nameof(this.GoToLobby),
			new Godot.Collections.Array {true});
		this.GetNode("LobbyJoinButton").Connect("pressed", this, nameof(this.OpenSteamOverlay));
		
		//TODO: handle invites section
	}

	/**
	 * Opens the steam overlay so that the user can join a lobby
	 */
	public void OpenSteamOverlay() {
		GD.Print("Opening the Steam Overlay to the friends section");
		SteamFriends.ActivateGameOverlay("friends");
	}

	/**
	 * Moves the user to the lobby screen as either a host or a client
	 */
	public void GoToLobby(bool isHost) {
		GD.Print("Setting user to be a " + (isHost ? "Host" : "Client") + ".");
		Global.getFrom(this).IsHost = isHost;
		this.GetTree().ChangeScene("res://Scenes/Lobby.tscn");
	}

}
