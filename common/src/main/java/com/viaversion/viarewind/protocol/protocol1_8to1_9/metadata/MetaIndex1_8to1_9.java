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

package com.viaversion.viarewind.protocol.protocol1_8to1_9.metadata;

import com.viaversion.viaversion.api.minecraft.entities.Entity1_10Types;
import com.viaversion.viaversion.protocols.protocol1_9to1_8.metadata.MetaIndex;
import com.viaversion.viaversion.util.Pair;

import java.util.HashMap;
import java.util.Optional;

public class MetaIndex1_8to1_9 {

	private static final HashMap<Pair<Entity1_10Types.EntityType, Integer>, MetaIndex> metadataRewrites = new HashMap<>();

	static {
		for (MetaIndex index : MetaIndex.values())
			metadataRewrites.put(new Pair<>(index.getClazz(), index.getNewIndex()), index);
	}

	private static Optional<MetaIndex> getIndex(Entity1_10Types.EntityType type, int index) {
		Pair<Entity1_10Types.EntityType, Integer> pair = new Pair<>(type, index);
		if (metadataRewrites.containsKey(pair)) {
			return Optional.of(metadataRewrites.get(pair));
		}

		return Optional.empty();
	}

	public static MetaIndex searchIndex(Entity1_10Types.EntityType type, int index) {
		Entity1_10Types.EntityType currentType = type;
		do {
			Optional<MetaIndex> optMeta = getIndex(currentType, index);

			if (optMeta.isPresent()) {
				return optMeta.get();
			}

			currentType = currentType.getParent();
		} while (currentType != null);

		return null;
	}
}
