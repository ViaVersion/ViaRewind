package de.gerrygames.viarewind;

import de.gerrygames.viarewind.api.ViaRewindConfigImpl;
import de.gerrygames.viarewind.api.ViaRewindPlatform;
import net.md_5.bungee.api.plugin.Plugin;

import java.io.File;

public class BungeePlugin extends Plugin implements ViaRewindPlatform {
	@Override
	public void onEnable() {
		ViaRewindConfigImpl conf = new ViaRewindConfigImpl(new File(getDataFolder(), "config.yml"));
		conf.reloadConfig();
		this.init(conf);
	}
}
