/*
 * This file is part of ViaRewind - https://github.com/ViaVersion/ViaRewind
 * Copyright (C) 2018-2025 ViaVersion and contributors
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

import com.viaversion.viarewind.api.data.RewindMappingDataLoader;
import com.viaversion.viaversion.libs.fastutil.objects.Object2IntMap;
import com.viaversion.viaversion.libs.fastutil.objects.Object2IntOpenHashMap;
import com.viaversion.viaversion.libs.gson.JsonElement;
import com.viaversion.nbt.tag.CompoundTag;
import com.viaversion.viaversion.libs.gson.JsonObject;
import java.util.Map;

public final class RewindMappingData1_8 extends com.viaversion.viarewind.api.data.RewindMappingData {

	private final Object2IntMap<String> identifiers1_8 = new Object2IntOpenHashMap<>();

	public RewindMappingData1_8() {
		super("1.8", "1.7.10");
	}

	@Override
	protected void loadExtras(CompoundTag data) {
		super.loadExtras(data);

		JsonObject obj = RewindMappingDataLoader.INSTANCE.loadData("identifiers-1.8.json");

        for (Map.Entry<String, JsonElement> entry : obj.entrySet()) {
            identifiers1_8.put(entry.getKey(), entry.getValue().getAsInt());
        }
	}

    public int getByNameOrId(final String identifier) {
        int id = identifiers1_8.getOrDefault(identifier.replace("minecraft:", ""), -1);

        if (id == -1) {
            try {
                return Integer.parseInt(identifier);
            } catch (NumberFormatException var3) {
                return -1;
            }
        }

        return id;
    }
}
