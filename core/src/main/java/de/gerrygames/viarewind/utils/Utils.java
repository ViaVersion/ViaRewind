package de.gerrygames.viarewind.utils;

import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.connection.ProtocolInfo;
import com.viaversion.viaversion.libs.opennbt.tag.builtin.CompoundTag;
import com.viaversion.viaversion.libs.opennbt.tag.builtin.Tag;

import java.util.UUID;
import java.util.function.Consumer;

public class Utils {

	public static UUID getUUID(UserConnection user) {
		return user.getProtocolInfo().getUuid();
	}
}
