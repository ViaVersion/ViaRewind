package de.gerrygames.viarewind.api;

import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import de.gerrygames.viarewind.ViaRewind;
import de.gerrygames.viarewind.protocol.protocol1_7_0_5to1_7_6_10.Protocol1_7_0_5to1_7_6_10;
import de.gerrygames.viarewind.protocol.protocol1_7_6_10to1_8.Protocol1_7_6_10TO1_8;
import de.gerrygames.viarewind.protocol.protocol1_7_6to1_7_2.Protocol1_7_6to1_7_2;
import de.gerrygames.viarewind.protocol.protocol1_8to1_9.Protocol1_8TO1_9;

import java.util.logging.Logger;

public interface ViaRewindPlatform {
	default void init(ViaRewindConfig config) {
		ViaRewind.init(this, config);

		String version = ViaRewind.class.getPackage().getImplementationVersion();
		Via.getManager().getSubPlatforms().add(version != null ? version : "UNKNOWN");

		Via.getManager().getProtocolManager().registerProtocol(new Protocol1_8TO1_9(), ProtocolVersion.v1_8, ProtocolVersion.v1_9);
		Via.getManager().getProtocolManager().registerProtocol(new Protocol1_7_6_10TO1_8(), ProtocolVersion.v1_7_6, ProtocolVersion.v1_8);
		Via.getManager().getProtocolManager().registerProtocol(new Protocol1_7_0_5to1_7_6_10(), ProtocolVersion.v1_7_1, ProtocolVersion.v1_7_6);

		Via.getManager().getProtocolManager().registerProtocol(new Protocol1_7_6to1_7_2(), ProtocolVersion.v1_7_6, ProtocolVersion.v1_7_1);
	}

	Logger getLogger();
}
