using System;
using Godot;
using Steamworks;

/**
 * A global singleton that stores persistent data
 */
public class Global : Node {

	//Whether the user is a host (true) or a client (false); null means the user is neither a host nor a client
	public bool? IsHost = null;

	/**
	 * Initializes Steamworks
	 */
	public override void _Ready() {
		var packSize = Packsize.Test();
		var dllCheck = DllCheck.Test();
		GD.Print("Packsize: " + packSize + "\tDLLCheck: " + dllCheck + ".");
		if (!packSize || !dllCheck) {
			this.GetTree().Quit();
		}

		try {
			if (SteamAPI.RestartAppIfNecessary((AppId_t) 480)) {
				GD.Print("Restarting through Steam.");
				this.GetTree().Quit();
			}
		}
		catch (DllNotFoundException) {
			GD.Print("DLL not found exception.");
			this.GetTree().Quit();
		}

		if (SteamAPI.Init()) {
			GD.Print("Entering game as " + SteamFriends.GetPersonaName() + ".");
		}
		else {
			GD.Print("Couldn't initialize Steam.");
			this.GetTree().Quit();
		}
	}

}
