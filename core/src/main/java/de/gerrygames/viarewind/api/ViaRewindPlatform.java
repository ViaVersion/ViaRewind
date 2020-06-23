package de.gerrygames.viarewind.api;

import de.gerrygames.viarewind.ViaRewind;
import de.gerrygames.viarewind.protocol.protocol1_7_0_5to1_7_6_10.Protocol1_7_0_5to1_7_6_10;
import de.gerrygames.viarewind.protocol.protocol1_7_6_10to1_8.Protocol1_7_6_10TO1_8;
import de.gerrygames.viarewind.protocol.protocol1_8to1_9.Protocol1_8TO1_9;
import us.myles.ViaVersion.api.Via;
import us.myles.ViaVersion.api.protocol.ProtocolRegistry;
import us.myles.ViaVersion.api.protocol.ProtocolVersion;

import java.util.Collections;
import java.util.logging.Logger;

public interface ViaRewindPlatform {
	default void init(ViaRewindConfig config) {
		ViaRewind.init(this, config);

		String version = ViaRewind.class.getPackage().getImplementationVersion();
		Via.getManager().getSubPlatforms().add(version != null ? version : "UNKNOWN");

		ProtocolRegistry.registerProtocol(new Protocol1_8TO1_9(), Collections.singletonList(ProtocolVersion.v1_8.getId()), ProtocolVersion.v1_9.getId());
		ProtocolRegistry.registerProtocol(new Protocol1_7_6_10TO1_8(), Collections.singletonList(ProtocolVersion.v1_7_6.getId()), ProtocolVersion.v1_8.getId());
		ProtocolRegistry.registerProtocol(new Protocol1_7_0_5to1_7_6_10(), Collections.singletonList(ProtocolVersion.v1_7_1.getId()), ProtocolVersion.v1_7_6.getId());
	}

	Logger getLogger();
}
