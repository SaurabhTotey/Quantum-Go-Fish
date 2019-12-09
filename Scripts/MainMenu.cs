using System.Collections.Generic;
using Godot;
using Steamworks;

/**
 * Main menu
 */
public class MainMenu : Node {

	//All the game IDs for steam invites; is used to allow users to join a game they are invited to
	public List<ulong> InvitedLobbyIDs = new List<ulong>();
	
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
		
		new Callback<LobbyInvite_t>(inviteEvent => {
			GD.Print($"Got an invite from {SteamFriends.GetFriendPersonaName((CSteamID) inviteEvent.m_ulSteamIDUser)}.");
			this.GetNode<ItemList>("InvitesPanel/InvitesScrollContainer/InvitesItemList").AddItem(SteamFriends.GetFriendPersonaName((CSteamID) inviteEvent.m_ulSteamIDUser));
			this.InvitedLobbyIDs.Add(inviteEvent.m_ulSteamIDLobby);
		});
		this.GetNode<ItemList>("InvitesPanel/InvitesScrollContainer/InvitesItemList").Connect("item_activated", this, nameof(this.AcceptInvite));
	}

	/**
	 * Gets called when an invite is activated from the InvitesItemList
	 */
	private void AcceptInvite(int inviteIndex) {
		GD.Print($"Accepting invite from {this.GetNode<ItemList>("InvitesPanel/InvitesScrollContainer/InvitesItemList").Items[inviteIndex]}.");
		SteamMatchmaking.JoinLobby((CSteamID) this.InvitedLobbyIDs[inviteIndex]);
		this.GoToLobby(false);
	}

	/**
	 * Opens the steam overlay so that the user can join a lobby
	 */
	private void OpenSteamOverlay() {
		GD.Print("Opening the Steam Overlay to the friends section");
		SteamFriends.ActivateGameOverlay("friends");
	}

	/**
	 * Moves the user to the lobby screen as either a host or a client
	 */
	private void GoToLobby(bool isHost) {
		GD.Print($"Setting user to be a {(isHost ? "Host" : "Client")}.");
		Global.getFrom(this).IsHost = isHost;
		this.GetTree().ChangeScene("res://Scenes/Lobby.tscn");
	}

}
