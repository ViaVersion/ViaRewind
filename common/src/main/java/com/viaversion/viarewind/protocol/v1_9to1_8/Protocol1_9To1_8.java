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
package com.viaversion.viarewind.protocol.v1_9to1_8;

import com.viaversion.viabackwards.api.BackwardsProtocol;
import com.viaversion.viarewind.protocol.v1_9to1_8.data.RewindMappingData1_8;
import com.viaversion.viarewind.protocol.v1_9to1_8.rewriter.EntityPacketRewriter1_9;
import com.viaversion.viarewind.protocol.v1_9to1_8.rewriter.*;
import com.viaversion.viarewind.protocol.v1_9to1_8.storage.*;
import com.viaversion.viarewind.protocol.v1_9to1_8.task.CooldownIndicatorTask;
import com.viaversion.viarewind.protocol.v1_9to1_8.task.LevitationUpdateTask;
import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.platform.providers.ViaProviders;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.protocol.remapper.ValueTransformer;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.api.minecraft.ClientWorld;
import com.viaversion.viaversion.protocols.v1_8to1_9.packet.ClientboundPackets1_8;
import com.viaversion.viaversion.protocols.v1_8to1_9.packet.ClientboundPackets1_9;
import com.viaversion.viaversion.protocols.v1_8to1_9.packet.ServerboundPackets1_8;
import com.viaversion.viaversion.protocols.v1_8to1_9.packet.ServerboundPackets1_9;

import java.util.concurrent.TimeUnit;

public class Protocol1_9To1_8 extends BackwardsProtocol<ClientboundPackets1_9, ClientboundPackets1_8, ServerboundPackets1_9, ServerboundPackets1_8> {

	public static final ValueTransformer<Double, Integer> DOUBLE_TO_INT_TIMES_32 = new ValueTransformer<Double, Integer>(Types.INT) {
		@Override
		public Integer transform(PacketWrapper wrapper, Double inputValue) {
			return (int) (inputValue * 32.0D);
		}
	};
	public static final ValueTransformer<Float, Byte> DEGREES_TO_ANGLE = new ValueTransformer<Float, Byte>(Types.BYTE) {
		@Override
		public Byte transform(PacketWrapper packetWrapper, Float degrees) {
			return (byte) ((degrees / 360F) * 256);
		}
	};

	public static final RewindMappingData1_8 MAPPINGS = new RewindMappingData1_8();

	private final BlockItemPacketRewriter1_9 itemRewriter = new BlockItemPacketRewriter1_9(this);
	private final EntityPacketRewriter1_9 entityRewriter = new EntityPacketRewriter1_9(this);

	public Protocol1_9To1_8() {
		super(ClientboundPackets1_9.class, ClientboundPackets1_8.class, ServerboundPackets1_9.class, ServerboundPackets1_8.class);
	}

	@Override
	protected void registerPackets() {
		entityRewriter.register();
		itemRewriter.register();

		PlayerPacketRewriter1_9.register(this);
		WorldPacketRewriter1_9.register(this);
	}

	@Override
	public void init(UserConnection connection) {
		connection.addEntityTracker(this.getClass(), new EntityTracker1_9(connection));

		connection.put(new WindowTracker(connection));
		connection.put(new LevitationStorage());
		connection.put(new PlayerPositionTracker());
		connection.put(new CooldownStorage());
		connection.put(new BlockPlaceDestroyTracker());
		connection.put(new BossBarStorage(connection));

		if (!connection.has(ClientWorld.class)) {
			connection.put(new ClientWorld());
		}
	}

	@Override
	public void register(ViaProviders providers) {
		Via.getManager().getScheduler().scheduleRepeating(new LevitationUpdateTask(), 0L, 50L, TimeUnit.MILLISECONDS);
		Via.getManager().getScheduler().scheduleRepeating(new CooldownIndicatorTask(), 0L, 50L, TimeUnit.MILLISECONDS);
	}

	@Override
	public RewindMappingData1_8 getMappingData() {
		return MAPPINGS;
	}

	@Override
	public BlockItemPacketRewriter1_9 getItemRewriter() {
		return itemRewriter;
	}

	@Override
	public EntityPacketRewriter1_9 getEntityRewriter() {
		return entityRewriter;
	}

	@Override
	public boolean hasMappingDataToLoad() {
		return true;
	}
}
