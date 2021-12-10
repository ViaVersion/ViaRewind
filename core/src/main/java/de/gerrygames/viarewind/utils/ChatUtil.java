package de.gerrygames.viarewind.utils;

import com.viaversion.viaversion.libs.gson.JsonElement;
import com.viaversion.viaversion.libs.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import com.viaversion.viaversion.libs.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import com.viaversion.viaversion.protocols.protocol1_13to1_12_2.ChatRewriter;
import de.gerrygames.viarewind.ViaRewind;

import java.util.logging.Level;
import java.util.regex.Pattern;

public class ChatUtil {
	private static final Pattern UNUSED_COLOR_PATTERN = Pattern.compile("(?>(?>§[0-fk-or])*(§r|\\Z))|(?>(?>§[0-f])*(§[0-f]))");

	public static String jsonToLegacy(String json) {
		if (json == null || json.equals("null") || json.isEmpty()) return "";
		try {
			String legacy = LegacyComponentSerializer.legacySection().serialize(ChatRewriter.HOVER_GSON_SERIALIZER.deserialize(json));
			while (legacy.startsWith("§f")) legacy = legacy.substring(2);
			return legacy;
		} catch (Exception ex) {
			ViaRewind.getPlatform().getLogger().log(Level.WARNING, "Could not convert component to legacy text: " + json, ex);
		}
		return "";
	}

	public static String jsonToLegacy(JsonElement component) {
		if (component.isJsonNull() || component.isJsonArray() && component.getAsJsonArray().isEmpty() || component.isJsonObject() && component.getAsJsonObject().size() == 0) {
			return "";
		} else if (component.isJsonPrimitive()) {
			return component.getAsString();
		} else {
			return jsonToLegacy(component.toString());
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
