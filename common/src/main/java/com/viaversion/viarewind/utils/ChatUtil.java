/*
 * This file is part of ViaRewind - https://github.com/ViaVersion/ViaRewind
 * Copyright (C) 2016-2023 ViaVersion and contributors
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

package com.viaversion.viarewind.utils;

import com.viaversion.viarewind.ViaRewind;
import com.viaversion.viaversion.api.protocol.packet.ClientboundPacketType;
import com.viaversion.viaversion.libs.gson.JsonElement;
import com.viaversion.viaversion.libs.gson.JsonObject;
import com.viaversion.viaversion.libs.gson.JsonParser;
import com.viaversion.viaversion.libs.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import com.viaversion.viaversion.libs.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import com.viaversion.viaversion.protocols.protocol1_13to1_12_2.ChatRewriter;
import com.viaversion.viaversion.protocols.protocol1_13to1_12_2.Protocol1_13To1_12_2;
import com.viaversion.viaversion.rewriter.ComponentRewriter;

import java.util.logging.Level;
import java.util.regex.Pattern;

public class ChatUtil {
	private final static Pattern UNUSED_COLOR_PATTERN = Pattern.compile("(?>(?>§[0-fk-or])*(§r|\\Z))|(?>(?>§[0-f])*(§[0-f]))");
	private final static ComponentRewriter<ClientboundPacketType> LEGACY_REWRITER = new ComponentRewriter<ClientboundPacketType>(null, ComponentRewriter.ReadType.JSON) {
		@Override
		protected void handleTranslate(JsonObject object, String translate) {
			String text = Protocol1_13To1_12_2.MAPPINGS.getMojangTranslation().get(translate);
			if (text != null) {
				object.addProperty("translate", text);
			}
		}
	};

	public static String jsonToLegacy(String json) {
		if (json == null || json.equals("null") || json.isEmpty()) return "";
		try {
			return jsonToLegacy(JsonParser.parseString(json));
		} catch (Exception e) {
			ViaRewind.getPlatform().getLogger().log(Level.WARNING, "Could not convert component to legacy text: " + json, e);
		}
		return "";
	}

	public static String jsonToLegacy(JsonElement component) {
		if (component.isJsonNull() || component.isJsonArray() && component.getAsJsonArray().isEmpty() || component.isJsonObject() && component.getAsJsonObject().isEmpty()) {
			return "";
		} else if (component.isJsonPrimitive()) {
			return component.getAsString();
		} else {
			try {
				LEGACY_REWRITER.processText(component);
				String legacy = LegacyComponentSerializer.legacySection().serialize(ChatRewriter.HOVER_GSON_SERIALIZER.deserializeFromTree(component));
				while (legacy.startsWith("§f")) legacy = legacy.substring(2);
				return legacy;
			} catch (Exception ex) {
				ViaRewind.getPlatform().getLogger().log(Level.WARNING, "Could not convert component to legacy text: " + component, ex);
			}
			return "";
		}
	}

	public static String legacyToJson(String legacy) {
		if (legacy == null) return "";
		return GsonComponentSerializer.gson().serialize(LegacyComponentSerializer.legacySection().deserialize(legacy));
	}

	public static String removeUnusedColor(String legacy, char last) {
		if (legacy == null) return null;
		legacy = UNUSED_COLOR_PATTERN.matcher(legacy).replaceAll("$1$2");
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < legacy.length(); i++) {
			char current = legacy.charAt(i);
			if (current != '§' || i == legacy.length() - 1) {
				builder.append(current);
				continue;
			}
			current = legacy.charAt(++i);
			if (current == last) continue;
			builder.append('§').append(current);
			last = current;
		}
		return builder.toString();
	}
}
