using Godot;
using System;
using Godot.Collections;

public class MainMenu : Node {

	public override void _Ready() {
		Global.IsHost = null;
		this.GetNode("LobbyCreateButton").Connect("pressed", this, nameof(this.OnMenuButtonPressed),
			new Godot.Collections.Array {true});
		this.GetNode("LobbyJoinButton").Connect("pressed", this, nameof(this.OnMenuButtonPressed),
			new Godot.Collections.Array {false});
	}

	public void OnMenuButtonPressed(bool isHost) {
		Global.IsHost = isHost;
		this.GetTree().ChangeScene("res://Lobby.tscn");
	}

}
