package de.gerrygames.viarewind.utils;

import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.chat.ComponentSerializer;

public class ChatUtil {

	public static String jsonToLegacy(String json) {
		if (json==null || json.equals("null")) return "";
		String legacy = TextComponent.toLegacyText(ComponentSerializer.parse(json));
		while (legacy.startsWith("§f")) legacy = legacy.substring(2, legacy.length());
		return legacy;
	}

	public static String legacyToJson(String legacy) {
		if (legacy==null) return "";
		return ComponentSerializer.toString(TextComponent.fromLegacyText(legacy));
	}

	public static String removeUnusedColor(String legacy) {
		if (legacy==null) return null;
		legacy = legacy.replaceAll("§[0-f](§[0-f|r])", "$1");
		legacy = legacy.replaceAll("(§.)?\\1", "$1");
		legacy = legacy.replaceAll("(§.)*\\Z", "");
		return legacy;
	}
}
