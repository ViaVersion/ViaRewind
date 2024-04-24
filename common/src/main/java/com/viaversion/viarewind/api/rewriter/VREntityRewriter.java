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
package com.viaversion.viarewind.api.rewriter;

import com.viaversion.viabackwards.api.BackwardsProtocol;
import com.viaversion.viabackwards.api.rewriters.LegacyEntityRewriter;
import com.viaversion.viarewind.ViaRewind;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.minecraft.ClientWorld;
import com.viaversion.viaversion.api.minecraft.metadata.MetaType;
import com.viaversion.viaversion.api.minecraft.metadata.types.MetaType1_8;
import com.viaversion.viaversion.api.protocol.packet.ClientboundPacketType;
import com.viaversion.viaversion.api.protocol.remapper.PacketHandlers;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.data.entity.EntityTrackerBase;

public abstract class VREntityRewriter<C extends ClientboundPacketType, T extends BackwardsProtocol<C, ?, ?, ?>> extends LegacyEntityRewriter<C, T> {

	public VREntityRewriter(T protocol) {
		super(protocol, MetaType1_8.String, MetaType1_8.Byte);
	}

	public VREntityRewriter(T protocol, MetaType displayType, MetaType displayVisibilityType) {
		super(protocol, displayType, displayVisibilityType);
	}

	protected void registerJoinGame1_8(final C packetType) {
		protocol.registerClientbound(packetType, new PacketHandlers() {
			@Override
			protected void register() {
				map(Type.INT); // Entity id
				map(Type.UNSIGNED_BYTE); // Game mode
				map(Type.BYTE); // Dimension
				handler(playerTrackerHandler());
				handler(wrapper -> wrapper.user().get(ClientWorld.class).setEnvironment(wrapper.get(Type.BYTE, 0)));
			}
		});
	}

	protected void untrackEntities(final UserConnection connection, final int[] entities) {
		final EntityTrackerBase tracker = tracker(connection);
		for (int entityId : entities) {
			tracker.removeEntity(entityId);
		}
	}

	@Override
	protected Object getDisplayVisibilityMetaValue() {
		return (byte) 1;
	}

	@Override
	protected boolean alwaysShowOriginalMobName() {
		return ViaRewind.getConfig().alwaysShowOriginalMobName();
	}
}
