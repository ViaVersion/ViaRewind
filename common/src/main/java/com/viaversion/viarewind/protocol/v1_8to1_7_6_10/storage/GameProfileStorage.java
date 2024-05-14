/*
 * This file is part of ViaRewind - https://github.com/ViaVersion/ViaRewind
 * Copyright (C) 2018-2024 ViaVersion and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.viaversion.viarewind.protocol.v1_8to1_7_6_10.storage;

import com.viaversion.viarewind.utils.ChatUtil;
import com.viaversion.viaversion.api.connection.StoredObject;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.minecraft.item.DataItem;
import com.viaversion.viaversion.api.minecraft.item.Item;
import com.viaversion.nbt.tag.CompoundTag;
import com.viaversion.nbt.tag.ListTag;
import com.viaversion.nbt.tag.StringTag;
import com.viaversion.viaversion.util.ChatColorUtil;

import java.util.*;

public class GameProfileStorage extends StoredObject {
	private final Map<UUID, GameProfile> properties = new HashMap<>();

	public GameProfileStorage(UserConnection user) {
		super(user);
	}

	public GameProfile put(UUID uuid, String name) {
		GameProfile gameProfile = new GameProfile(uuid, name);
		properties.put(uuid, gameProfile);
		return gameProfile;
	}

	public GameProfile get(UUID uuid) {
		return properties.get(uuid);
	}

	public GameProfile get(String name, boolean ignoreCase) {
		if (ignoreCase) name = name.toLowerCase();

		for (GameProfile profile : properties.values()) {
			if (profile.name == null) continue;

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
			if (profile.name == null) continue;

			String n = ignoreCase ? profile.name.toLowerCase() : profile.name;

			if (n.startsWith(prefix)) profiles.add(profile);
		}

		return profiles;
	}

	public GameProfile remove(UUID uuid) {
		return properties.remove(uuid);
	}


	public static class GameProfile {
		public final String name;
		public final UUID uuid;
		public String displayName;
		public int ping;
		public List<Property> properties = new ArrayList<>();
		public int gamemode = 0;

		public GameProfile(UUID uuid, String name) {
			this.name = name;
			this.uuid = uuid;
		}

		public Item getSkull() {
			CompoundTag tag = new CompoundTag();
			CompoundTag ownerTag = new CompoundTag();
			tag.put("SkullOwner", ownerTag);
			ownerTag.put("Id", new StringTag(uuid.toString()));
			CompoundTag properties = new CompoundTag();
			ownerTag.put("Properties", properties);
			ListTag textures = new ListTag(CompoundTag.class);
			properties.put("textures", textures);
			for (Property property : this.properties) {
				if (property.name.equals("textures")) {
					CompoundTag textureTag = new CompoundTag();
					textureTag.put("Value", new StringTag(property.value));
					if (property.signature != null) {
						textureTag.put("Signature", new StringTag(property.signature));
					}
					textures.add(textureTag);
				}
			}

			return new DataItem(397, (byte) 1, (short) 3, tag);
		}

		public String getDisplayName() {
			String displayName = this.displayName == null ? name : this.displayName;
			if (displayName.length() > 16) displayName = ChatUtil.removeUnusedColor(displayName, 'f');
			if (displayName.length() > 16) displayName = ChatColorUtil.stripColor(displayName);
			if (displayName.length() > 16) displayName = displayName.substring(0, 16);
			return displayName;
		}

		public void setDisplayName(String displayName) {
			this.displayName = displayName;
		}
	}

	public static class Property {
		public final String name;
		public final String value;
		public final String signature;

		public Property(String name, String value, String signature) {
			this.name = name;
			this.value = value;
			this.signature = signature;
		}
	}
}
