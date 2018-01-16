package de.gerrygames.viarewind;

import de.gerrygames.viarewind.api.ViaRewindPlatform;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class BukkitPlugin extends JavaPlugin implements ViaRewindPlatform {
	@Override
	public void onEnable() {
		this.init();
	}

	@Override
	public void disable() {
		getPluginLoader().disablePlugin(this);
	}
}
