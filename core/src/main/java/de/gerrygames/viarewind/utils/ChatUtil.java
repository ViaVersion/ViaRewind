package de.gerrygames.viarewind.utils;

import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.chat.ComponentSerializer;

import java.util.regex.Pattern;

public class ChatUtil {
	private static Pattern unusedColorPattern = Pattern.compile("(?>(?>§[0-fk-or])*(§r|\\Z))|(?>(?>§[0-f])*(§[0-f]))");

	public static String jsonToLegacy(String json) {
		if (json==null || json.equals("null") || json.equals("")) return "";
		String legacy = TextComponent.toLegacyText(ComponentSerializer.parse(json));
		while (legacy.startsWith("§f")) legacy = legacy.substring(2, legacy.length());
		return legacy;
	}

	public static String legacyToJson(String legacy) {
		if (legacy==null) return "";
		return ComponentSerializer.toString(TextComponent.fromLegacyText(legacy));
	}

	public static String removeUnusedColor(String legacy, char last) {
		if (legacy==null) return null;
		legacy = unusedColorPattern.matcher(legacy).replaceAll("$1$2");
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i<legacy.length(); i++) {
			char current = legacy.charAt(i);
			if (current!='§' || i==legacy.length()-1) {
				builder.append(current);
				continue;
			}
			current = legacy.charAt(++i);
			if (current==last) continue;
			builder.append('§').append(current);
			last = current;
		}
		return builder.toString();
	}
}
