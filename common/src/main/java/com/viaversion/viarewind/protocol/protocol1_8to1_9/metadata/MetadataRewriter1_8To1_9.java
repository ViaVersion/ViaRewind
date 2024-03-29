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

import com.viaversion.viarewind.api.rewriter.VREntityRewriter;
import com.viaversion.viarewind.protocol.protocol1_8to1_9.Protocol1_8To1_9;
import com.viaversion.viarewind.protocol.protocol1_8to1_9.storage.EntityTracker1_9;
import com.viaversion.viaversion.api.minecraft.EulerAngle;
import com.viaversion.viaversion.api.minecraft.Vector;
import com.viaversion.viaversion.api.minecraft.entities.EntityTypes1_10;
import com.viaversion.viaversion.api.minecraft.entities.EntityTypes1_10.EntityType;
import com.viaversion.viaversion.api.minecraft.item.Item;
import com.viaversion.viaversion.api.minecraft.metadata.Metadata;
import com.viaversion.viaversion.api.minecraft.metadata.types.MetaType1_8;
import com.viaversion.viaversion.protocols.protocol1_9to1_8.ClientboundPackets1_9;
import com.viaversion.viaversion.protocols.protocol1_9to1_8.metadata.MetaIndex;
import com.viaversion.viaversion.rewriter.meta.MetaHandlerEvent;

import java.util.UUID;

public class MetadataRewriter1_8To1_9 extends VREntityRewriter<ClientboundPackets1_9, Protocol1_8To1_9> {

	private static final byte HAND_ACTIVE_BIT = 0;
	private static final byte STATUS_USE_BIT = 4;

	public MetadataRewriter1_8To1_9(Protocol1_8To1_9 protocol) {
		super(protocol);
	}

	@Override
	protected void registerRewrites() {
		mapEntityTypeWithData(EntityType.SHULKER, EntityType.)

		filter().handler(this::handleMetadata);
	}

	private void handleMetadata(MetaHandlerEvent event, Metadata metadata) {
		final EntityTracker1_9 tracker = tracker(event.user());
		if (metadata.id() == MetaIndex.ENTITY_STATUS.getIndex()) {
			tracker.getStatus().put(event.entityId(), (Byte) metadata.value());
		}
		final MetaIndex metaIndex = MetaIndex1_8to1_9.searchIndex(event.entityType(), metadata.id());
		if (metaIndex == null) {
			// Almost certainly bad data, remove it
			event.cancel();
			return;
		}
		if (metaIndex.getOldType() == MetaType1_8.NonExistent || metaIndex.getNewType() == null) {
			if (metaIndex == MetaIndex.PLAYER_HAND) { // Player eating/aiming/drinking
				int status = tracker.getStatus().getOrDefault(event.entityId(), 0);
				if ((((byte) metadata.value()) & 1 << HAND_ACTIVE_BIT) != 0) {
					status = (byte) (status | 1 << STATUS_USE_BIT);
				} else {
					status = (byte) (status & ~(1 << STATUS_USE_BIT));
				}
				event.createExtraMeta(new Metadata(MetaIndex.ENTITY_STATUS.getIndex(), MetaType1_8.Byte, status));
				return;
			}
			event.cancel();
			return;
		}

		metadata.setId(metaIndex.getNewIndex());
		metadata.setMetaTypeUnsafe(metaIndex.getNewType());

		final Object value = metadata.getValue();
		switch (metaIndex.getNewType()) {
			case Byte:
				if (metaIndex.getOldType() == MetaType1_8.Byte) {
					metadata.setValue(value);
				}
				if (metaIndex.getOldType() == MetaType1_8.Int) {
					metadata.setValue(((Byte) value).intValue());
				}
				break;
			case OptUUID:
				if (metaIndex.getOldType() != MetaType1_8.String) {
					event.cancel();
					break;
				}
				final UUID owner = (UUID) value;
				metadata.setValue(owner != null ? owner.toString() : "");
				break;
			case BlockID:
				event.cancel();
				event.createExtraMeta(new Metadata(metaIndex.getIndex(), MetaType1_8.Short, ((Integer) value).shortValue()));
				break;
			case VarInt:
				if (metaIndex.getOldType() == MetaType1_8.Byte) {
					metadata.setValue(((Integer) value).byteValue());
				}
				if (metaIndex.getOldType() == MetaType1_8.Short) {
					metadata.setValue(((Integer) value).shortValue());
				}
				if (metaIndex.getOldType() == MetaType1_8.Int) {
					metadata.setValue(value);
				}
				break;
			case Float:
            case String:
            case Chat:
                metadata.setValue(value);
				break;
            case Boolean:
				final boolean bool = (Boolean) value;
				if (metaIndex == MetaIndex.AGEABLE_AGE) {
					metadata.setValue((byte) (bool ? -1 : 0));
				} else {
					metadata.setValue((byte) (bool ? 1 : 0));
				}
				break;
			case Slot:
				metadata.setValue(protocol.getItemRewriter().handleItemToClient((Item) value));
				break;
			case Position:
				final Vector vector = (Vector) value;
				metadata.setValue(vector);
				break;
			case Vector3F:
				final EulerAngle angle = (EulerAngle) value;
				metadata.setValue(angle);
				break;
            default:
				throw new RuntimeException("Unhandled MetaDataType: " + metaIndex.getNewType());
		}
	}

	@Override
	public EntityType typeFromId(int type) {
		return EntityTypes1_10.getTypeFromId(type, false);
	}

	@Override
	public EntityType objectTypeFromId(int type) {
		return EntityTypes1_10.getTypeFromId(type, true);
	}
}
