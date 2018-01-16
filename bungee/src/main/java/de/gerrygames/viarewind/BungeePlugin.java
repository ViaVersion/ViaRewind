package de.gerrygames.viarewind;

import de.gerrygames.viarewind.api.ViaRewindPlatform;
import net.md_5.bungee.api.plugin.Plugin;
import us.myles.ViaVersion.api.data.UserConnection;

public class BungeePlugin extends Plugin implements ViaRewindPlatform {
	@Override
	public void onEnable() {
		this.init();
	}

	@Override
	public void disable() {

	}
}
