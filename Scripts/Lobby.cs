using Godot;
using Godot.Collections;
using Steamworks;
//ReSharper disable PossibleInvalidOperationException

/**
 * Multiplayer Lobby
 */
public class Lobby : Node {

	//The host of the lobby as of the time of the initialization of this Lobby; is used to determine if the host has left and is only relevant if user is a client
	private CSteamID CurrentLobbyOwner = CSteamID.Nil;

	/**
	 * Initializes relevant display elements and starts up/joins the lobby
	 */
	public override void _Ready() {
		GD.Print($"Entering Lobby as a {(Global.getFrom(this).IsHost.Value ? "Host" : "Client")}.");
		this.GetNode<Button>("ExitButton").Connect("pressed", this, nameof(this.GoToMainMenu));
		this.GetNode<Button>("InviteFriendsButton").Connect("pressed", this, nameof(this.OpenInvitesDialog));
		
		new Callback<LobbyChatUpdate_t>(_ => this.UpdateLobbyMembersList());
		
		if (Global.getFrom(this).IsHost.Value) {
			new Callback<LobbyCreated_t>(creationEvent => {
				if (creationEvent.m_eResult != EResult.k_EResultOK) {
					GD.Print("Error creating lobby.");
					this.GoToMainMenu();
				}
				Global.getFrom(this).CurrentLobbyId = (CSteamID) creationEvent.m_ulSteamIDLobby;
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
			this.CurrentLobbyOwner = SteamMatchmaking.GetLobbyOwner(Global.getFrom(this).CurrentLobbyId);
			new Callback<LobbyChatUpdate_t>(updateEvent => {
				var newestLobbyOwner = SteamMatchmaking.GetLobbyOwner(Global.getFrom(this).CurrentLobbyId);
				if (this.CurrentLobbyOwner != newestLobbyOwner) {
					GD.Print($"Lobby owner has changed from {SteamFriends.GetFriendPersonaName(this.CurrentLobbyOwner)} to {SteamFriends.GetFriendPersonaName(newestLobbyOwner)}. Leaving lobby.");
					this.GoToMainMenu();
				}
			});
		}
	}

	/**
	 * Allows the current user (presumably the host) to invite other players
	 */
	private void OpenInvitesDialog() {
		SteamFriends.ActivateGameOverlayInviteDialog(Global.getFrom(this).CurrentLobbyId);
	}

	/**
	 * Updates the lobby label to display the lobby id
	 */
	private void UpdateLobbyLabel() {
		this.GetNode<RichTextLabel>("LobbyLabel").BbcodeText = $"[b][center]Lobby: {Global.getFrom(this).CurrentLobbyId}[/center][/b]";
	}

	/**
	 * Updates the lobby members list to list whomever is currently connected to the current lobby
	 */
	private void UpdateLobbyMembersList() {
		GD.Print("Updating the list of members of the current lobby.");
		var lobbyId = Global.getFrom(this).CurrentLobbyId;
		var listNode = this.GetNode<ItemList>("MembersListBackgroundPanel/JoinedMembersList");
		listNode.Items = new Array();
		for (var i = 0; i < SteamMatchmaking.GetNumLobbyMembers(lobbyId); i++) {
			listNode.AddItem($"{SteamFriends.GetFriendPersonaName(SteamMatchmaking.GetLobbyMemberByIndex(lobbyId, i))}");
		}
	}

	/**
	 * Cancels doing anything that might have been happening on this screen and goes back to the main menu
	 */
	private void GoToMainMenu() {
		GD.Print("Leaving Lobby.");
		if (Global.getFrom(this).CurrentLobbyId != CSteamID.Nil) {
			SteamMatchmaking.LeaveLobby(Global.getFrom(this).CurrentLobbyId);
		}
		this.GetTree().ChangeScene("res://Scenes/MainMenu.tscn");
	}

}
