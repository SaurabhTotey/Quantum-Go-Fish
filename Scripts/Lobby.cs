using Godot;

/**
 * Multiplayer Lobby
 */
public class Lobby : Node {

	public override void _Ready() {
		GD.Print("Entering Lobby as a " + (Global.getFrom(this).IsHost.Value ? "Host" : "Client") + ".");
		this.GetNode<Button>("ExitButton").Connect("pressed", this, nameof(this.GoToMainMenu));
	}

	/**
	 * Cancels doing anything that might have been happening on this screen and goes back to the main menu
	 */
	private void GoToMainMenu() {
		GD.Print("Leaving Lobby");
		this.GetTree().ChangeScene("res://Scenes/MainMenu.tscn");
	}

}
