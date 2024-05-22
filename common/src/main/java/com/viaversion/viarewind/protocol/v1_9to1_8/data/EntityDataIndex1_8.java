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

import com.viaversion.viaversion.api.minecraft.entities.EntityType;
import com.viaversion.viaversion.api.minecraft.entities.EntityTypes1_9;
import com.viaversion.viaversion.protocols.v1_8to1_9.data.EntityDataIndex1_9;
import com.viaversion.viaversion.util.Pair;

import java.util.HashMap;
import java.util.Optional;

public class EntityDataIndex1_8 {

	private static final HashMap<Pair<EntityTypes1_9.EntityType, Integer>, EntityDataIndex1_9> ENTITY_DATA_REWRITES = new HashMap<>();

	static {
		for (EntityDataIndex1_9 index : EntityDataIndex1_9.values()) {
			ENTITY_DATA_REWRITES.put(new Pair<>(index.getClazz(), index.getNewIndex()), index);
		}
	}

	private static Optional<EntityDataIndex1_9> getIndex(final EntityType type, final int index) {
		final Pair<EntityType, Integer> pair = new Pair<>(type, index);
		if (ENTITY_DATA_REWRITES.containsKey(pair)) {
			return Optional.of(ENTITY_DATA_REWRITES.get(pair));
		} else {
			return Optional.empty();
		}
	}

	public static EntityDataIndex1_9 searchIndex(final EntityType type, final int index) {
		EntityType currentType = type;
		do {
			final Optional<EntityDataIndex1_9> optMeta = getIndex(currentType, index);
			if (optMeta.isPresent()) {
				return optMeta.get();
			}
			currentType = currentType.getParent();
		} while (currentType != null);
		return null;
	}

}
