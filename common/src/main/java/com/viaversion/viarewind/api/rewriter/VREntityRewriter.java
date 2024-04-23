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
import com.viaversion.viaversion.api.minecraft.ClientWorld;
import com.viaversion.viaversion.api.minecraft.entities.EntityType;
import com.viaversion.viaversion.api.minecraft.metadata.types.MetaType1_8;
import com.viaversion.viaversion.api.protocol.packet.ClientboundPacketType;
import com.viaversion.viaversion.api.type.Type;

public abstract class VREntityRewriter<C extends ClientboundPacketType, T extends BackwardsProtocol<C, ?, ?, ?>> extends LegacyEntityRewriter<C, T> {

	public VREntityRewriter(T protocol) {
		super(protocol, MetaType1_8.String, MetaType1_8.Byte);
	}

	protected void registerJoinGame1_8(final C packetType, final EntityType playerType) {
		protocol.registerClientbound(packetType, wrapper -> {
			final int entityId = wrapper.passthrough(Type.INT);
			wrapper.passthrough(Type.UNSIGNED_BYTE); // Game mode
			final byte dimension = wrapper.passthrough(Type.BYTE);

			addTrackedEntity(wrapper, entityId, playerType);
			wrapper.user().get(ClientWorld.class).setEnvironment(dimension);
		});
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
