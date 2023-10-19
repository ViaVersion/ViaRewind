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

package com.viaversion.viarewind.protocol.protocol1_7_6_10to1_8.rewriter;

import com.viaversion.viarewind.ViaRewind;
import com.viaversion.viarewind.protocol.protocol1_7_6_10to1_8.Protocol1_7_6_10To1_8;
import com.viaversion.viarewind.protocol.protocol1_7_6_10to1_8.model.MetaIndex1_7_6_10To1_8;
import com.viaversion.viarewind.protocol.protocol1_7_6_10to1_8.types.metadata.MetaType1_7_6_10;
import com.viaversion.viaversion.api.minecraft.entities.EntityTypes1_10;
import com.viaversion.viaversion.api.minecraft.item.Item;
import com.viaversion.viaversion.api.minecraft.metadata.Metadata;
import com.viaversion.viaversion.api.minecraft.metadata.types.MetaType1_8;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

@SuppressWarnings({"deprecation"})
public class MetadataRewriter {

	private final Protocol1_7_6_10To1_8 protocol;

	public MetadataRewriter(Protocol1_7_6_10To1_8 protocol) {
		this.protocol = protocol;
	}

	public void transform(EntityTypes1_10.EntityType type, List<Metadata> list) {
		for (Metadata entry : new ArrayList<>(list)) {
			final MetaIndex1_7_6_10To1_8 metaIndex = MetaIndex1_7_6_10To1_8.searchIndex(type, entry.id());
			try {
				if (metaIndex == null) {
					ViaRewind.getPlatform().getLogger().warning("Could not find 1.8 metadata for " + type + " with index " + entry.id());
					list.remove(entry);
					continue;
				}
				if (metaIndex.getOldType() == MetaType1_7_6_10.NonExistent) {
					list.remove(entry);
					continue;
				}
				final Object value = entry.getValue();
				entry.setTypeAndValue(metaIndex.getNewType(), value);
				entry.setMetaTypeUnsafe(metaIndex.getOldType());
				entry.setId(metaIndex.getIndex());

				switch (metaIndex.getOldType()) {
					case Int:
						if (metaIndex.getNewType() == MetaType1_8.Byte) {
							entry.setValue(((Byte) value).intValue());
							if (metaIndex == MetaIndex1_7_6_10To1_8.ENTITY_AGEABLE_AGE) {
								if ((Integer) entry.getValue() < 0) {
									entry.setValue(-25000);
								}
							}
						}
						if (metaIndex.getNewType() == MetaType1_8.Short) {
							entry.setValue(((Short) value).intValue());
						}
						if (metaIndex.getNewType() == MetaType1_8.Int) {
							entry.setValue(value);
						}
						break;
					case Byte:
						if (metaIndex.getNewType() == MetaType1_8.Int) {
							entry.setValue(((Integer) value).byteValue());
						}
						if (metaIndex.getNewType() == MetaType1_8.Byte) {
							if (metaIndex == MetaIndex1_7_6_10To1_8.ITEM_FRAME_ROTATION) {
								entry.setValue(Integer.valueOf((Byte) value % 4).byteValue());
							} else {
								entry.setValue(value);
							}
						}
						if (metaIndex == MetaIndex1_7_6_10To1_8.HUMAN_SKIN_FLAGS) {
							byte flags = (byte) value;
							boolean cape = (flags & 0x01) != 0;
							flags = (byte) (cape ? 0x00 : 0x02);
							entry.setValue(flags);
						}
						break;
					case Slot:
						entry.setValue(protocol.getItemRewriter().handleItemToClient((Item) value));
						break;
					case Float:
					case String:
					case Short:
					case Position:
						break;
					default:
						ViaRewind.getPlatform().getLogger().warning("Could not transform metadata for " + type + " with index " + entry.id() + " and type " + metaIndex.getOldType());
						list.remove(entry);
						break;
				}
			} catch (Exception e) {
				ViaRewind.getPlatform().getLogger().log(Level.WARNING, "Unable to transform metadata", e);
				list.remove(entry);
			}
		}
	}
}
