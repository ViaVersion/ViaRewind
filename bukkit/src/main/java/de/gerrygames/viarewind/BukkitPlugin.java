package de.gerrygames.viarewind;

import de.gerrygames.viarewind.api.ViaRewindConfig;
import de.gerrygames.viarewind.api.ViaRewindPlatform;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class BukkitPlugin extends JavaPlugin implements ViaRewindPlatform {
	@Override
	public void onEnable() {
		saveDefaultConfig();
		final FileConfiguration config = getConfig();
		this.init(new ViaRewindConfig() {
			@Override
			public CooldownIndicator getCooldownIndicator() {
				return CooldownIndicator.valueOf(config.getString("cooldown-indicator").toUpperCase());
			}
		});
	}

	@Override
	public void disable() {
		getPluginLoader().disablePlugin(this);
	}
}
