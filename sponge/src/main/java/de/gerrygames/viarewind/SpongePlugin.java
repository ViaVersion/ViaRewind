package de.gerrygames.viarewind;

import com.google.inject.Inject;
import de.gerrygames.viarewind.api.ViaRewindConfigImpl;
import de.gerrygames.viarewind.api.ViaRewindPlatform;
import de.gerrygames.viarewind.sponge.VersionInfo;
import lombok.Getter;
import org.spongepowered.api.config.DefaultConfig;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.game.state.GameAboutToStartServerEvent;
import org.spongepowered.api.plugin.Dependency;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.plugin.PluginContainer;
import us.myles.ViaVersion.sponge.util.LoggerWrapper;

import java.io.File;
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
	@Getter
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

		File configFile = new File(defaultConfig.getParentFile(), "config.yml");

		ViaRewindConfigImpl conf = new ViaRewindConfigImpl(configFile,
				getClass().getClassLoader().getResource("assets/viarewind/config.yml"));
		conf.reloadConfig();

		this.init(conf);
	}

	@Override
	public void disable() {

	}

}
