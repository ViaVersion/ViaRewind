package de.gerrygames.viarewind;

import de.gerrygames.viarewind.api.ViaRewindConfig;
import de.gerrygames.viarewind.api.ViaRewindPlatform;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

public class BungeePlugin extends Plugin implements ViaRewindPlatform {
	private Configuration config;

	@Override
	public void onEnable() {
		if (!getDataFolder().exists()) getDataFolder().mkdir();

		File file = new File(getDataFolder(), "config.yml");
		if (!file.exists()) {
			try (InputStream in = getResourceAsStream("config.yml")) {
				Files.copy(in, file.toPath());
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
		
		try {
			config = ConfigurationProvider.getProvider(YamlConfiguration.class).load(new File(getDataFolder(), "config.yml"));
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		this.init(new ViaRewindConfig() {
			@Override
			public CooldownIndicator getCooldownIndicator() {
				return CooldownIndicator.valueOf(config.getString("cooldown-indicator").toUpperCase());
			}
		});
	}

	@Override
	public void disable() {

	}
}
