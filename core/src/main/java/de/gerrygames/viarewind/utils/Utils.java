package de.gerrygames.viarewind.utils;

import us.myles.ViaVersion.api.data.UserConnection;
import us.myles.ViaVersion.protocols.base.ProtocolInfo;

import java.util.UUID;

public class Utils {

	public static UUID getUUID(UserConnection user) {
		return user.get(ProtocolInfo.class).getUuid();
	}
}
