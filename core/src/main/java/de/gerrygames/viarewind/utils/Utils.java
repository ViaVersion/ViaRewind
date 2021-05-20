package de.gerrygames.viarewind.utils;

import com.viaversion.viaversion.api.connection.UserConnection;

import java.util.UUID;

public class Utils {

	public static UUID getUUID(UserConnection user) {
		return user.getProtocolInfo().getUuid();
	}
}
