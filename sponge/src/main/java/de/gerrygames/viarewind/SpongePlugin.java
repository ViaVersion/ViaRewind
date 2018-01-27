package de.gerrygames.viarewind;

import com.google.inject.Inject;
import de.gerrygames.viarewind.api.ViaRewindConfig;
import de.gerrygames.viarewind.api.ViaRewindPlatform;
import de.gerrygames.viarewind.sponge.VersionInfo;
import org.spongepowered.api.config.DefaultConfig;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.game.state.GameAboutToStartServerEvent;
import org.spongepowered.api.plugin.Dependency;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.plugin.PluginContainer;
import us.myles.ViaVersion.sponge.util.LoggerWrapper;
import us.myles.ViaVersion.util.Config;

import java.io.File;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

@Plugin(id = "viarewind",
		name = "ViaRewind",
		version = VersionInfo.VERSION,
		authors = {"Gerrygames"},
		dependencies = {
			@Dependency(id = "viaversion"),
			@Dependency(id = "viabackwards", optional = true)
		}
)
public class SpongePlugin implements ViaRewindPlatform {

	private Logger logger;
	@Inject
	private PluginContainer container;

	@Inject
	@DefaultConfig(sharedRoot = false)
	private File defaultConfig;

	@Listener(order = Order.LATE)
	public void onServerStart(GameAboutToStartServerEvent e) {
		// Setup Logger
		this.logger = new LoggerWrapper(container.getLogger());
		// Init!

		Config config = new Config(new File(defaultConfig, "config.yml")) {
			@Override
			public URL getDefaultConfigURL() {
				return container.getAsset("config.yml").get().getUrl();
			}

			@Override
			protected void handleConfig(Map<String, Object> map) { }

			@Override
			public List<String> getUnsupportedOptions() {
				return Collections.emptyList();
			}
		};

		this.init(new ViaRewindConfig() {
			@Override
			public CooldownIndicator getCooldownIndicator() {
				return CooldownIndicator.valueOf(config.getString("cooldown-indicator", "TITLE").toUpperCase());
			}
		});
	}

	@Override
	public Logger getLogger() {
		return logger;
	}

	@Override
	public void disable() {

	}

}
