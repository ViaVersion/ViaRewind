package de.gerrygames.viarewind;

import de.gerrygames.viarewind.api.ViaRewindConfigImpl;
import de.gerrygames.viarewind.api.ViaRewindPlatform;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public class BukkitPlugin extends JavaPlugin implements ViaRewindPlatform {
	@Override
	public void onEnable() {
		final File config = getDataFolder().toPath().resolve("config.yml").toFile();
		ViaRewindConfigImpl conf = new ViaRewindConfigImpl(config, getClass().getResource("assets/viarewind/config.yml"));
		conf.reloadConfig();
		this.init(conf);
	}

	@Override
	public void disable() {
		getPluginLoader().disablePlugin(this);
	}
}
