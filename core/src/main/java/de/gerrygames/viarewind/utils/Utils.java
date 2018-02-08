package de.gerrygames.viarewind.utils;

import us.myles.ViaVersion.api.data.UserConnection;
import us.myles.ViaVersion.protocols.base.ProtocolInfo;
import us.myles.viaversion.libs.opennbt.tag.builtin.CompoundTag;
import us.myles.viaversion.libs.opennbt.tag.builtin.Tag;

import java.util.UUID;
import java.util.function.Consumer;

public class Utils {

	public static void iterateCompountTagRecursive(CompoundTag tag, final Consumer<Tag> action) {
		tag.values().forEach(child -> {
			if (child instanceof CompoundTag) iterateCompountTagRecursive((CompoundTag) child, action);
			else action.accept(child);
		});
	}

	public static UUID getUUID(UserConnection user) {
		return user.get(ProtocolInfo.class).getUuid();
	}
}
