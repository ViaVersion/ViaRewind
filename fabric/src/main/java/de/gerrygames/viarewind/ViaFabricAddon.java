package de.gerrygames.viarewind;

import de.gerrygames.viarewind.api.ViaRewindConfigImpl;
import de.gerrygames.viarewind.api.ViaRewindPlatform;
import de.gerrygames.viarewind.fabric.util.LoggerWrapper;
import de.gerrygames.viarewind.protocol.protocol1_7_6_10to1_7_1_5.Protocol1_7_6_10To1_7_1_5;
import de.gerrygames.viarewind.protocol.protocol1_8to1_7_6_10.Protocol1_8To1_7_6_10;
import lombok.Getter;
import net.fabricmc.loader.api.FabricLoader;
import org.apache.logging.log4j.LogManager;
import us.myles.ViaVersion.api.protocol.ProtocolRegistry;
import us.myles.ViaVersion.api.protocol.ProtocolVersion;

import java.util.Collections;
import java.util.logging.Logger;

public class ViaFabricAddon implements ViaRewindPlatform, Runnable {
    @Getter
    private final Logger logger = new LoggerWrapper(LogManager.getLogger("ViaRewind"));

    @Override
    public void run() {
        ViaRewindConfigImpl conf = new ViaRewindConfigImpl(FabricLoader.getInstance().getConfigDirectory().toPath().resolve("ViaRewind").resolve("config.yml").toFile());
        conf.reloadConfig();
        this.init(conf);
        ProtocolRegistry.registerProtocol(new Protocol1_7_6_10To1_7_1_5(), Collections.singletonList(ProtocolVersion.v1_7_6.getId()), ProtocolVersion.v1_7_1.getId());
        ProtocolRegistry.registerProtocol(new Protocol1_8To1_7_6_10(), Collections.singletonList(ProtocolVersion.v1_8.getId()), ProtocolVersion.v1_7_6.getId());
    }
}
