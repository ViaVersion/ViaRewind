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

package com.viaversion.viarewind.protocol.protocol1_8to1_9.metadata;

import com.viaversion.viarewind.ViaRewind;
import com.viaversion.viarewind.protocol.protocol1_8to1_9.Protocol1_8To1_9;
import com.viaversion.viarewind.protocol.protocol1_8to1_9.storage.EntityTracker;
import com.viaversion.viaversion.api.minecraft.EulerAngle;
import com.viaversion.viaversion.api.minecraft.Vector;
import com.viaversion.viaversion.api.minecraft.entities.EntityTypes1_10.EntityType;
import com.viaversion.viaversion.api.minecraft.item.Item;
import com.viaversion.viaversion.api.minecraft.metadata.Metadata;
import com.viaversion.viaversion.api.minecraft.metadata.types.MetaType1_8;
import com.viaversion.viaversion.protocols.protocol1_9to1_8.metadata.MetaIndex;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MetadataRewriter {
	private final Protocol1_8To1_9 protocol;

	public MetadataRewriter(Protocol1_8To1_9 protocol) {
		this.protocol = protocol;
	}

	public void transform(EntityTracker tracker, int entityId, List<Metadata> list) {
		transform(tracker, entityId, list, tracker.getClientEntityTypes().get(entityId));
	}

	private static final byte HAND_ACTIVE_BIT = 0;
	private static final byte STATUS_USE_BIT = 4;

	public void transform(EntityTracker tracker, int entityId, List<Metadata> list, EntityType entityType) {
		for (Metadata metadata : list) {
			if (metadata.id() == MetaIndex.ENTITY_STATUS.getIndex()) {
				tracker.getStatusInformation().put(entityId, (Byte) metadata.getValue());
			}
		}
		for (Metadata entry : new ArrayList<>(list)) {
			MetaIndex metaIndex = MetaIndex1_8to1_9.searchIndex(entityType, entry.id());
			try {
				if (metaIndex != null) {
					if (metaIndex.getOldType() == MetaType1_8.NonExistent || metaIndex.getNewType() == null) {
						list.remove(entry);
						if (metaIndex == MetaIndex.PLAYER_HAND) {
							byte status = tracker.getStatusInformation().getOrDefault(entityId, (byte) 0);
							if ((((byte) entry.value()) & 1 << HAND_ACTIVE_BIT) != 0) {
								status = (byte) (status | 1 << STATUS_USE_BIT);
							} else {
								status = (byte) (status & ~(1 << STATUS_USE_BIT));
							}
							list.add(new Metadata(MetaIndex.ENTITY_STATUS.getIndex(), MetaType1_8.Byte, status));
						}
						continue;
					}
					Object value = entry.getValue();
					entry.setMetaTypeUnsafe(metaIndex.getOldType());
					entry.setId(metaIndex.getIndex());
					switch (metaIndex.getNewType()) {
						case Byte:
							if (metaIndex.getOldType() == MetaType1_8.Byte) {
								entry.setValue(value);
							}
							if (metaIndex.getOldType() == MetaType1_8.Int) {
								entry.setValue(((Byte) value).intValue());
							}
							break;
						case OptUUID:
							if (metaIndex.getOldType() != MetaType1_8.String) {
								list.remove(entry);
								break;
							}
							UUID owner = (UUID) value;
							if (owner == null) entry.setValue("");
							else entry.setValue(owner.toString());
							break;
						case BlockID:
							list.remove(entry);
							list.add(new Metadata(metaIndex.getIndex(), MetaType1_8.Short, ((Integer) value).shortValue()));
							break;
						case VarInt:
							if (metaIndex.getOldType() == MetaType1_8.Byte) {
								entry.setValue(((Integer) value).byteValue());
							}
							if (metaIndex.getOldType() == MetaType1_8.Short) {
								entry.setValue(((Integer) value).shortValue());
							}
							if (metaIndex.getOldType() == MetaType1_8.Int) {
								entry.setValue(value);
							}
							break;
						case Float:
							entry.setValue(value);
							break;
						case String:
							entry.setValue(value);
							break;
						case Boolean:
							if (metaIndex == MetaIndex.AGEABLE_AGE) entry.setValue((byte) ((Boolean) value ? -1 : 0));
							else entry.setValue((byte) ((Boolean) value ? 1 : 0));
							break;
						case Slot:
							entry.setValue(protocol.getItemRewriter().handleItemToClient((Item) value));
							break;
						case Position:
							Vector vector = (Vector) value;
							entry.setValue(vector);
							break;
						case Vector3F:
							EulerAngle angle = (EulerAngle) value;
							entry.setValue(angle);
							break;
						case Chat:
							entry.setValue(value);
							break;
						default:
							ViaRewind.getPlatform().getLogger().warning("[Out] Unhandled MetaDataType: " + metaIndex.getNewType());
							list.remove(entry);
							break;
					}

					if (!metaIndex.getOldType().type().getOutputClass().isAssignableFrom(entry.getValue().getClass())) {
						list.remove(entry);
					}

				} else {
					throw new Exception("Could not find valid metadata");
				}
			} catch (Exception e) {
				list.remove(entry);
			}
		}
	}

}
