package de.gerrygames.viarewind.protocol.protocol1_7_6_10to1_8.storage;

import us.myles.ViaVersion.api.data.StoredObject;
import us.myles.ViaVersion.api.data.UserConnection;
import us.myles.ViaVersion.api.minecraft.item.Item;
import us.myles.viaversion.libs.opennbt.tag.builtin.CompoundTag;
import us.myles.viaversion.libs.opennbt.tag.builtin.ListTag;
import us.myles.viaversion.libs.opennbt.tag.builtin.StringTag;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class GameProfileStorage extends StoredObject {
	private Map<UUID, GameProfile> properties = new HashMap<>();

	public GameProfileStorage(UserConnection user) {
		super(user);
	}

	public GameProfile put(UUID uuid, String name) {
		GameProfile gameProfile = new GameProfile(uuid, name);
		properties.put(uuid, gameProfile);
		return gameProfile;
	}

	public void putProperty(UUID uuid, Property property) {
		properties.computeIfAbsent(uuid, profile -> new GameProfile(uuid, null)).properties.add(property);
	}

	public void putProperty(UUID uuid, String name, String value, String signature) {
		putProperty(uuid, new Property(name, value, signature));
	}

	public GameProfile get(UUID uuid) {
		return properties.get(uuid);
	}

	public GameProfile get(String name, boolean ignoreCase) {
		if (ignoreCase) name = name.toLowerCase();

		for (GameProfile profile : properties.values()) {
			if (profile.name==null) continue;

			String n = ignoreCase ? profile.name.toLowerCase() : profile.name;

			if (n.equals(name)) {
				return profile;
			}
		}
		return null;
	}

	public List<GameProfile> getAllWithPrefix(String prefix, boolean ignoreCase) {
		if (ignoreCase) prefix = prefix.toLowerCase();

		ArrayList<GameProfile> profiles = new ArrayList<>();

		for (GameProfile profile : properties.values()) {
			if (profile.name==null) continue;

			String n = ignoreCase ? profile.name.toLowerCase() : profile.name;

			if (n.startsWith(prefix)) profiles.add(profile);
		}

		return profiles;
	}

	public GameProfile remove(UUID uuid) {
		return properties.remove(uuid);
	}


	public static class GameProfile {
		public String name;
		public String displayName;
		public int ping;
		public UUID uuid;
		public List<Property> properties = new ArrayList<>();
		public int gamemode = 0;

		public GameProfile(UUID uuid, String name) {
			this.name = name;
			this.uuid = uuid;
		}

		public Item getSkull() {
			CompoundTag tag = new CompoundTag("");
			CompoundTag ownerTag = new CompoundTag("SkullOwner");
			tag.put(ownerTag);
			ownerTag.put(new StringTag("Id", uuid.toString()));
			CompoundTag properties = new CompoundTag("Properties");
			ownerTag.put(properties);
			ListTag textures = new ListTag("textures", CompoundTag.class);
			properties.put(textures);
			for (GameProfileStorage.Property property : this.properties) {
				if (property.name.equals("textures")) {
					CompoundTag textureTag = new CompoundTag("");
					textureTag.put(new StringTag("Value", property.value));
					if (property.signature!=null) {
						textureTag.put(new StringTag("Signature", property.signature));
					}
					textures.add(textureTag);
				}
			}

			return new Item((short) 397, (byte) 1, (short) 3, tag);
		}

		public String getDisplayName() {
			String displayName = this.displayName==null ? name : this.displayName;
			if (displayName.length()>16) displayName = displayName.substring(0, 16);
			return displayName;
		}

		public void setDisplayName(String displayName) {
			this.displayName = displayName;
		}
	}

	public static class Property {
		public String name;
		public String value;
		public String signature;

		public Property(String name, String value, String signature) {
			this.name = name;
			this.value = value;
			this.signature = signature;
		}
	}
}
