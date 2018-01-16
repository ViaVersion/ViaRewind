package de.gerrygames.viarewind.api;

import de.gerrygames.viarewind.ViaRewind;
import de.gerrygames.viarewind.protocol.protocol1_7_0_5to1_7_6_10.Protocol1_7_0_5to1_7_6_10;
import de.gerrygames.viarewind.protocol.protocol1_7_6_10to1_8.Protocol1_7_6_10TO1_8;
import de.gerrygames.viarewind.protocol.protocol1_8to1_9.Protocol1_8TO1_9;
import us.myles.ViaVersion.api.protocol.ProtocolRegistry;
import us.myles.ViaVersion.api.protocol.ProtocolVersion;

import java.util.Collections;
import java.util.logging.Logger;

public interface ViaRewindPlatform {

	default void init() {
		ViaRewind.init(this);

		ProtocolRegistry.registerProtocol(new Protocol1_8TO1_9(), Collections.singletonList(ProtocolVersion.v1_8.getId()), ProtocolVersion.v1_9.getId());
		ProtocolRegistry.registerProtocol(new Protocol1_7_6_10TO1_8(), Collections.singletonList(ProtocolVersion.v1_7_6.getId()), ProtocolVersion.v1_8.getId());
		ProtocolRegistry.registerProtocol(new Protocol1_7_0_5to1_7_6_10(), Collections.singletonList(ProtocolVersion.v1_7_1.getId()), ProtocolVersion.v1_7_6.getId());
	}

	/*default void checkViaBackwards() {
		String serverVersion = getServerVersion();
		if (!serverVersion.startsWith("1.8") && !serverVersion.startsWith("1.9")) {
			//ViaBackwards needed
			try {
				Class.forName("nl.matsv.viabackwards.ViaBackwards");
			} catch (ClassNotFoundException ex) {
				getLogger().severe("======================================");
				getLogger().severe("YOU DO NOT HAVE VIABACKWARDS INSTALLED");
				getLogger().severe("VIAREWIND WILL ONLY WORK WITH IT ON 1.10+");
				getLogger().severe("PLEASE DOWNLOAD AND INSTALL IT FROM HERE:");
				getLogger().severe("https://www.spigotmc.org/resources/27448/");
				getLogger().severe("======================================");
			}
		}
	}

	String getServerVersion();*/

	Logger getLogger();

	void disable();
}
