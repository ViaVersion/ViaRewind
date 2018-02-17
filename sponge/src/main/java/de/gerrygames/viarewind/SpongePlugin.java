package de.gerrygames.viarewind;

import com.google.inject.Inject;
import de.gerrygames.viarewind.api.ViaRewindConfig;
import de.gerrygames.viarewind.api.ViaRewindPlatform;
import de.gerrygames.viarewind.sponge.VersionInfo;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import ninja.leaping.configurate.yaml.YAMLConfigurationLoader;
import org.spongepowered.api.config.DefaultConfig;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.game.state.GameAboutToStartServerEvent;
import org.spongepowered.api.plugin.Dependency;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.plugin.PluginContainer;
import us.myles.ViaVersion.sponge.util.LoggerWrapper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
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

	private ConfigurationLoader<ConfigurationNode> loader;
	private ConfigurationNode rootNode;

	@Listener(order = Order.LATE)
	public void onServerStart(GameAboutToStartServerEvent e) {
		// Setup Logger
		this.logger = new LoggerWrapper(container.getLogger());
		// Init!

		File configFile = new File(defaultConfig.getParentFile(), "config.yml");
		if (!configFile.exists()) {
			saveDefaultConfig(configFile);
		}

		try {
			loader = YAMLConfigurationLoader.builder().setFile(configFile).build();
			rootNode = loader.load();
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		this.init(new ViaRewindConfig() {
			@Override
			public CooldownIndicator getCooldownIndicator() {
				return CooldownIndicator.valueOf(rootNode.getNode("cooldown-indicator").getString("TITLE"));
			}

			@Override
			public boolean isReplaceAdventureMode() {
				return rootNode.getNode("replace-adventure").getBoolean(false);
			}

			@Override
			public boolean isReplaceParticles() {
				return rootNode.getNode("replace-particles").getBoolean(false);
			}
		});
	}

	private void saveDefaultConfig(File file) {
		try {
			URL url = getClass().getClassLoader().getResource("config.yml");
			URLConnection connection = url.openConnection();
			connection.setUseCaches(false);
			InputStream in = connection.getInputStream();
			OutputStream out = new FileOutputStream(file);
			byte[] buf = new byte[1024];

			int len;
			while((len = in.read(buf)) > 0) {
				out.write(buf, 0, len);
			}

			out.close();
			in.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	@Override
	public Logger getLogger() {
		return logger;
	}

	@Override
	public void disable() {

	}

}
