/*
 * This file is part of ViaRewind - https://github.com/ViaVersion/ViaRewind
 * Copyright (C) 2018-2026 ViaVersion and contributors
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
package com.viaversion.viarewind.protocol.v1_8to1_7_6_10.data;

import com.viaversion.viaversion.libs.gson.JsonArray;
import com.viaversion.viaversion.libs.gson.JsonElement;
import com.viaversion.viaversion.libs.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ChatNewlineRewriter {
    public static List<JsonObject> splitChatComponentByNewline(JsonElement element) {
        return splitChatComponentByNewline(element, new JsonObject());
    }

    private static List<JsonObject> splitChatComponentByNewline(JsonElement element, JsonObject inheritedStyle) {
        List<JsonObject> results = new ArrayList<>();
        if (element == null || element.isJsonNull()) {
            return results;
        }

        if (element.isJsonPrimitive()) {
            String text = element.getAsString();
            for (String part : text.split("\n", -1)) {
                JsonObject obj = inheritedStyle.deepCopy();
                obj.addProperty("text", part);
                results.add(obj);
            }
            return results;
        }

        if (element.isJsonArray()) {
            for (JsonElement child : element.getAsJsonArray()) {
                results.addAll(splitChatComponentByNewline(child, inheritedStyle));
            }
            return results;
        }

        if (!element.isJsonObject()) {
            return results;
        }

        JsonObject obj = element.getAsJsonObject();

        // Build the style for this node: inherit first, then override with this object's style
        JsonObject currentStyle = inheritedStyle.deepCopy();
        for (Map.Entry<String, JsonElement> entry : obj.entrySet()) {
            String key = entry.getKey();
            if (!"text".equals(key) && !"extra".equals(key)) {
                currentStyle.add(key, entry.getValue());
            }
        }

        // Gather components in order: base text then extra array
        List<JsonElement> ordered = new ArrayList<>();
        if (obj.has("text")) {
            ordered.add(obj.get("text"));
        }
        if (obj.has("extra") && obj.get("extra").isJsonArray()) {
            for (JsonElement extra : obj.getAsJsonArray("extra")) {
                ordered.add(extra);
            }
        }

        // If there is no text/extra, return a single object with the style
        if (ordered.isEmpty()) {
            results.add(currentStyle);
            return results;
        }

        // Build lines by concatenating split pieces
        List<JsonObject> currentLine = new ArrayList<>();
        for (JsonElement part : ordered) {
            if (part == null || part.isJsonNull()) {
                continue;
            }

            if (part.isJsonPrimitive()) {
                String text = part.getAsString();
                String[] split = text.split("\n", -1);
                for (int i = 0; i < split.length; i++) {
                    JsonObject piece = currentStyle.deepCopy();
                    piece.addProperty("text", split[i]);
                    currentLine.add(piece);

                    if (i < split.length - 1) {
                        results.add(mergeLine(currentLine));
                        currentLine.clear();
                    }
                }
                continue;
            }

            if (part.isJsonObject() || part.isJsonArray()) {
                List<JsonObject> splitParts = splitChatComponentByNewline(part, currentStyle);
                for (int i = 0; i < splitParts.size(); i++) {
                    currentLine.add(splitParts.get(i));
                    if (i < splitParts.size() - 1) {
                        results.add(mergeLine(currentLine));
                        currentLine.clear();
                    }
                }
            }
        }

        if (!currentLine.isEmpty()) {
            results.add(mergeLine(currentLine));
        }

        return results;
    }

    private static JsonObject mergeLine(List<JsonObject> lineParts) {
        if (lineParts.isEmpty()) {
            JsonObject empty = new JsonObject();
            empty.addProperty("text", "");
            return empty;
        }

        JsonObject first = lineParts.get(0);
        if (lineParts.size() == 1) {
            return first;
        }

        JsonArray extra = new JsonArray();
        for (int i = 1; i < lineParts.size(); i++) {
            extra.add(lineParts.get(i));
        }
        first.add("extra", extra);
        return first;
    }
}
