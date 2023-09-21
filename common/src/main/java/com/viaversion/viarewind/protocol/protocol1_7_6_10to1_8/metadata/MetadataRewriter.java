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

package com.viaversion.viarewind.protocol.protocol1_7_6_10to1_8.metadata;

import com.viaversion.viarewind.ViaRewind;
import com.viaversion.viarewind.protocol.protocol1_7_6_10to1_8.items.ItemRewriter;
import com.viaversion.viarewind.protocol.protocol1_8to1_7_6_10.metadata.MetaIndex1_8To1_7_6_10;
import com.viaversion.viaversion.api.minecraft.entities.Entity1_10Types;
import com.viaversion.viaversion.api.minecraft.item.Item;
import com.viaversion.viaversion.api.minecraft.metadata.Metadata;
import com.viaversion.viaversion.api.minecraft.metadata.types.MetaType1_8;
import com.viaversion.viarewind.protocol.protocol1_7_6_10to1_8.types.MetaType1_7_6_10;

import java.util.ArrayList;
import java.util.List;

public class MetadataRewriter {

	public static void transform(Entity1_10Types.EntityType type, List<Metadata> list) {
		for (Metadata entry : new ArrayList<>(list)) {
			MetaIndex1_8To1_7_6_10 metaIndex = MetaIndex1_7_6_10to1_8.searchIndex(type, entry.id());
			try {
				if (metaIndex == null) throw new Exception("Could not find valid metadata");
				if (metaIndex.getOldType() == MetaType1_7_6_10.NonExistent) {
					list.remove(entry);
					continue;
				}
				Object value = entry.getValue();
				if (!metaIndex.getNewType().type().getOutputClass().isAssignableFrom(value.getClass())) {
					list.remove(entry);
					continue;
				}
				entry.setMetaTypeUnsafe(metaIndex.getOldType());
				entry.setId(metaIndex.getIndex());
				switch (metaIndex.getOldType()) {
					case Int:
						if (metaIndex.getNewType() == MetaType1_8.Byte) {
							entry.setValue(((Byte) value).intValue());
							if (metaIndex == MetaIndex1_8To1_7_6_10.ENTITY_AGEABLE_AGE) {
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
							if (metaIndex == MetaIndex1_8To1_7_6_10.ITEM_FRAME_ROTATION) {
								value = ((Integer) ((Byte) value / 2)).byteValue();
							}
							entry.setValue(value);
						}
						if (metaIndex == MetaIndex1_8To1_7_6_10.HUMAN_SKIN_FLAGS) {
							byte flags = (byte) value;
							boolean cape = (flags & 0x01) != 0;
							flags = (byte) (cape ? 0x00 : 0x02);
							entry.setValue(flags);
						}
						break;
					case Slot:
						entry.setValue(ItemRewriter.toClient((Item) value));
						break;
					case Float:
						entry.setValue(value);
						break;
					case Short:
						entry.setValue(value);
						break;
					case String:
						entry.setValue(value);
						break;
					case Position:
						entry.setValue(value);
						break;
					default:
						ViaRewind.getPlatform().getLogger().warning("[Out] Unhandled MetaDataType: " + metaIndex.getNewType());
						list.remove(entry);
						break;
				}
			} catch (Exception e) {
				list.remove(entry);
			}
		}
	}
}
