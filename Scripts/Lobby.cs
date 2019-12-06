using Godot;
using Steamworks;

/**
 * Multiplayer Lobby
 */
public class Lobby : Node {

	/**
	 * Initializes relevant display elements and starts up/joins the lobby
	 */
	public override void _Ready() {
		GD.Print("Entering Lobby as a " + (Global.getFrom(this).IsHost.Value ? "Host" : "Client") + ".");
		this.GetNode<Button>("ExitButton").Connect("pressed", this, nameof(this.GoToMainMenu));
		
		//TODO: add some sort of functionality to display all players currently in the lobby
		
		//TODO: add a button to open the invitation dialog to easily allow players to invite other players (should only be visible to host and enabled once lobby is created)
		
		if (Global.getFrom(this).IsHost.Value) {
			new Callback<LobbyCreated_t>(creationEvent => {
				if (creationEvent.m_eResult != EResult.k_EResultOK) {
					GD.Print("Error creating lobby.");
					this.GoToMainMenu();
				}
				Global.getFrom(this).CurrentLobbyId = creationEvent.m_ulSteamIDLobby;
				this.UpdateLobbyLabel();
				GD.Print("Lobby " + creationEvent.m_ulSteamIDLobby + " created.");
			});
			SteamMatchmaking.CreateLobby(ELobbyType.k_ELobbyTypeFriendsOnly, 8);
			
			//TODO: connect functionality to the start game button
		}
		else {
			this.GetNode<CanvasItem>("StartGameButton").Visible = false;
			this.UpdateLobbyLabel();
		}
	}

	/**
	 * Updates the lobby label to display the lobby id
	 */
	private void UpdateLobbyLabel() {
		this.GetNode<RichTextLabel>("LobbyLabel").BbcodeText = $"[b][center]Lobby: {Global.getFrom(this).CurrentLobbyId.Value}[/center][/b]";
	}

	/**
	 * Cancels doing anything that might have been happening on this screen and goes back to the main menu
	 */
	private void GoToMainMenu() {
		GD.Print("Leaving Lobby");
		if (Global.getFrom(this).CurrentLobbyId.HasValue) {
			SteamMatchmaking.LeaveLobby((CSteamID) Global.getFrom(this).CurrentLobbyId.Value);
		}
		this.GetTree().ChangeScene("res://Scenes/MainMenu.tscn");
	}

}
