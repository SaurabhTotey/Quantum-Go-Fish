using Godot;

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
		this.GetNode<Global>("/root/Global").IsHost = null;
		this.GetNode("LobbyCreateButton").Connect("pressed", this, nameof(this.OnMenuButtonPressed),
			new Godot.Collections.Array {true});
		this.GetNode("LobbyJoinButton").Connect("pressed", this, nameof(this.OnMenuButtonPressed),
			new Godot.Collections.Array {false});
	}

	/**
	 * Moves the user to the lobby screen as either a host or a client
	 */
	public void OnMenuButtonPressed(bool isHost) {
		GD.Print("Setting user to be a " + (isHost ? "Host" : "Client") + ".");
		this.GetNode<Global>("/root/Global").IsHost = isHost;
		this.GetTree().ChangeScene("res://Scenes/Lobby.tscn");
	}

}
