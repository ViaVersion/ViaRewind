package de.gerrygames.viarewind;

import com.google.inject.Inject;
import com.viaversion.viaversion.sponge.util.LoggerWrapper;
import de.gerrygames.viarewind.api.ViaRewindConfigImpl;
import de.gerrygames.viarewind.api.ViaRewindPlatform;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.lifecycle.ConstructPluginEvent;
import org.spongepowered.api.event.lifecycle.RefreshGameEvent;
import org.spongepowered.plugin.builtin.jvm.Plugin;

import java.nio.file.Path;
import java.util.logging.Logger;

@Plugin("viarewind")
public class SpongePlugin implements ViaRewindPlatform {
    private Logger logger;
    @SuppressWarnings("SpongeInjection")
    @Inject
    private org.apache.logging.log4j.Logger loggerSlf4j;

    @Inject
    @ConfigDir(sharedRoot = false)
    private Path configDir;
    private ViaRewindConfigImpl conf;

    @Listener(order = Order.LATE)
    public void loadPlugin(ConstructPluginEvent e) {
        // Setup Logger
        this.logger = new LoggerWrapper(loggerSlf4j);
        // Init!
        conf = new ViaRewindConfigImpl(configDir.resolve("config.yml").toFile());
        conf.reloadConfig();
        this.init(conf);
    }

    @Listener
    public void reload(RefreshGameEvent e) {
        conf.reloadConfig();
    }

    public Logger getLogger() {
        return this.logger;
    }
}
