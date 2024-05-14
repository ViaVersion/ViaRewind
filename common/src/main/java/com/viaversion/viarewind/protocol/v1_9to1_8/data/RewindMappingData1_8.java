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
package com.viaversion.viarewind.protocol.v1_9to1_8.data;

import com.viaversion.viarewind.api.data.RewindMappingDataLoader;
import com.viaversion.viaversion.libs.fastutil.objects.ObjectArrayList;
import com.viaversion.viaversion.libs.fastutil.objects.ObjectList;
import com.viaversion.viaversion.libs.gson.JsonArray;
import com.viaversion.viaversion.libs.gson.JsonElement;
import com.viaversion.nbt.tag.CompoundTag;

public final class RewindMappingData1_8 extends com.viaversion.viarewind.api.data.RewindMappingData {

	private final ObjectList<String> sounds = new ObjectArrayList<>();

	public RewindMappingData1_8() {
		super("1.9.4", "1.8");
	}

	@Override
	protected void loadExtras(CompoundTag data) {
		super.loadExtras(data);

		final JsonArray sounds = RewindMappingDataLoader.INSTANCE.loadData("sounds-1.9.json").getAsJsonArray("sounds");
		for (final JsonElement sound : sounds) {
			this.sounds.add(sound.getAsString());
		}
	}

	public String soundName(final int soundId) {
		return sounds.get(soundId);
	}
}
