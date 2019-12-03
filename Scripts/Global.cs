using Godot;
using Steamworks;

/**
 * A global singleton that stores persistent data
 */
public class Global : Node {

	//Whether the user is a host (true) or a client (false); null means the user is neither a host nor a client
	public bool? IsHost = null;

	/**
	 * 
	 */
	public override void _Ready() {
		SteamAPI.Init();
		GD.Print("Entering game as " + SteamFriends.GetPersonaName() + ".");
	}

}
