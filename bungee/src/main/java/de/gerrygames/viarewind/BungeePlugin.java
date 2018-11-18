package de.gerrygames.viarewind;

import de.gerrygames.viarewind.api.ViaRewindConfigImpl;
import de.gerrygames.viarewind.api.ViaRewindPlatform;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;

import java.io.File;

public class BungeePlugin extends Plugin implements ViaRewindPlatform {
	private Configuration config;

	@Override
	public void onEnable() {
		if (!getDataFolder().exists()) getDataFolder().mkdir();

		File file = new File(getDataFolder(), "config.yml");
		ViaRewindConfigImpl conf = new ViaRewindConfigImpl(file, getClass().getResource("assets/viarewind/config.yml"));
		conf.reloadConfig();
		this.init(conf);
	}

	@Override
	public void disable() {

	}
}
