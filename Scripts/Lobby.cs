using Godot;
using Steamworks;
//ReSharper disable PossibleInvalidOperationException

/**
 * Multiplayer Lobby
 */
public class Lobby : Node {

	/**
	 * Initializes relevant display elements and starts up/joins the lobby
	 */
	public override void _Ready() {
		GD.Print($"Entering Lobby as a {(Global.getFrom(this).IsHost.Value ? "Host" : "Client")}.");
		this.GetNode<Button>("ExitButton").Connect("pressed", this, nameof(this.GoToMainMenu));
		
		//TODO: add some sort of functionality to display all players currently in the lobby

		this.GetNode<Button>("InviteFriendsButton").Connect("pressed", this, nameof(this.OpenInvitesDialog));
		
		if (Global.getFrom(this).IsHost.Value) {
			new Callback<LobbyCreated_t>(creationEvent => {
				if (creationEvent.m_eResult != EResult.k_EResultOK) {
					GD.Print("Error creating lobby.");
					this.GoToMainMenu();
				}
				Global.getFrom(this).CurrentLobbyId = creationEvent.m_ulSteamIDLobby;
				this.UpdateLobbyLabel();
				GD.Print($"Lobby {creationEvent.m_ulSteamIDLobby} created.");
				this.GetNode<Button>("InviteFriendsButton").Disabled = false;
			});
			SteamMatchmaking.CreateLobby(ELobbyType.k_ELobbyTypeFriendsOnly, 8);
			
			//TODO: connect functionality to the start game button: it should be enabled once another player joins
		}
		else {
			this.GetNode<CanvasItem>("StartGameButton").Visible = false;
			this.GetNode<CanvasItem>("InviteFriendsButton").Visible = false;
			this.UpdateLobbyLabel();
			//TODO: have player get kicked back to main menu if the host leaves the lobby
		}
	}

	/**
	 * Allows the current user (presumably the host) to invite other players
	 */
	private void OpenInvitesDialog() {
		SteamFriends.ActivateGameOverlayInviteDialog((CSteamID) Global.getFrom(this).CurrentLobbyId.Value);
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
		if (Global.getFrom(this).IsHost.Value) {
			//TODO: kick out all other players
		}
		this.GetTree().ChangeScene("res://Scenes/MainMenu.tscn");
	}

}
