package de.gerrygames.viarewind.utils;

import us.myles.ViaVersion.api.data.UserConnection;
import us.myles.ViaVersion.protocols.base.ProtocolInfo;
import us.myles.viaversion.libs.opennbt.tag.builtin.CompoundTag;
import us.myles.viaversion.libs.opennbt.tag.builtin.Tag;

import java.util.UUID;
import java.util.function.Consumer;

public class Utils {

	public static UUID getUUID(UserConnection user) {
		return user.get(ProtocolInfo.class).getUuid();
	}
}
