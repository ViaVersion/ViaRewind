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
package com.viaversion.viarewind.protocol.v1_8to1_7_6_10;

import com.viaversion.viabackwards.api.BackwardsProtocol;
import com.viaversion.viarewind.ViaRewind;
import com.viaversion.viarewind.api.data.RewindMappingData;
import com.viaversion.viarewind.protocol.v1_7_6_10to1_7_2_5.packet.ClientboundPackets1_7_2_5;
import com.viaversion.viarewind.protocol.v1_7_6_10to1_7_2_5.packet.ServerboundPackets1_7_2_5;
import com.viaversion.viarewind.protocol.v1_8to1_7_6_10.rewriter.EntityPacketRewriter1_8;
import com.viaversion.viarewind.protocol.v1_8to1_7_6_10.rewriter.*;
import com.viaversion.viarewind.protocol.v1_8to1_7_6_10.provider.CompressionHandlerProvider;
import com.viaversion.viarewind.protocol.v1_8to1_7_6_10.provider.compression.TrackingCompressionHandlerProvider;
import com.viaversion.viarewind.protocol.v1_8to1_7_6_10.storage.*;
import com.viaversion.viarewind.protocol.v1_8to1_7_6_10.task.WorldBorderUpdateTask;
import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.minecraft.ClientWorld;
import com.viaversion.viaversion.api.platform.providers.ViaProviders;
import com.viaversion.viaversion.api.protocol.packet.Direction;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.protocol.packet.State;
import com.viaversion.viaversion.api.protocol.remapper.PacketHandlers;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.exception.CancelException;
import com.viaversion.viaversion.protocols.base.ClientboundLoginPackets;
import com.viaversion.viaversion.protocols.base.ServerboundLoginPackets;
import com.viaversion.viaversion.protocols.v1_8to1_9.packet.ClientboundPackets1_8;
import com.viaversion.viaversion.protocols.v1_8to1_9.packet.ServerboundPackets1_8;

import java.util.concurrent.TimeUnit;

public class Protocol1_8To1_7_6_10 extends BackwardsProtocol<ClientboundPackets1_8, ClientboundPackets1_7_2_5, ServerboundPackets1_8, ServerboundPackets1_7_2_5> {

	public static final RewindMappingData MAPPINGS = new RewindMappingData("1.8", "1.7.10");

	private final BlockItemPacketRewriter1_8 itemRewriter = new BlockItemPacketRewriter1_8(this);
	private final EntityPacketRewriter1_8 entityRewriter = new EntityPacketRewriter1_8(this);

	public Protocol1_8To1_7_6_10() {
		super(ClientboundPackets1_8.class, ClientboundPackets1_7_2_5.class, ServerboundPackets1_8.class, ServerboundPackets1_7_2_5.class);
	}

	@Override
	protected void registerPackets() {
		itemRewriter.register();
		entityRewriter.register();

		PlayerPacketRewriter1_8.register(this);
		ScoreboardPacketRewriter1_8.register(this);
		WorldPacketRewriter1_8.register(this);

		this.registerClientbound(State.LOGIN, ClientboundLoginPackets.HELLO.getId(), ClientboundLoginPackets.HELLO.getId(), new PacketHandlers() {
			@Override
			public void register() {
				map(Types.STRING); // server hash
				map(Types.BYTE_ARRAY_PRIMITIVE, Types.SHORT_BYTE_ARRAY); // public key
				map(Types.BYTE_ARRAY_PRIMITIVE, Types.SHORT_BYTE_ARRAY); // verification token
			}
		});
		this.registerClientbound(State.LOGIN, ClientboundLoginPackets.LOGIN_COMPRESSION.getId(), ClientboundLoginPackets.LOGIN_COMPRESSION.getId(), new PacketHandlers() {
			@Override
			public void register() {
				handler(wrapper -> {
					final int threshold = wrapper.read(Types.VAR_INT);

					Via.getManager().getProviders().get(CompressionHandlerProvider.class).onHandleLoginCompressionPacket(wrapper.user(), threshold);
					wrapper.cancel();
				});
			}
		});
		this.cancelClientbound(ClientboundPackets1_8.SET_COMPRESSION); // unused
		this.registerClientbound(ClientboundPackets1_8.KEEP_ALIVE, new PacketHandlers() {
			@Override
			public void register() {
				map(Types.VAR_INT, Types.INT); // id
			}
		});

		this.registerServerbound(State.LOGIN, ServerboundLoginPackets.ENCRYPTION_KEY.getId(), ServerboundLoginPackets.ENCRYPTION_KEY.getId(), new PacketHandlers() {
			@Override
			public void register() {
				map(Types.SHORT_BYTE_ARRAY, Types.BYTE_ARRAY_PRIMITIVE); // shared secret
				map(Types.SHORT_BYTE_ARRAY, Types.BYTE_ARRAY_PRIMITIVE); // verification token
			}
		});

		this.registerServerbound(ServerboundPackets1_7_2_5.KEEP_ALIVE, new PacketHandlers() {
			@Override
			public void register() {
				map(Types.INT, Types.VAR_INT); // id
			}
		});
	}

	@Override
	public void transform(Direction direction, State state, PacketWrapper packetWrapper) throws CancelException {
		Via.getManager().getProviders().get(CompressionHandlerProvider.class).onTransformPacket(packetWrapper.user());

		super.transform(direction, state, packetWrapper);
	}

	@Override
	public void init(UserConnection connection) {
		connection.addEntityTracker(this.getClass(), new EntityTracker1_8(connection));

		connection.put(new InventoryTracker(connection));
		connection.put(new PlayerSessionStorage(connection));
		connection.put(new GameProfileStorage(connection));
		connection.put(new ScoreboardTracker(connection));
		connection.put(new CompressionStatusTracker(connection));
		connection.put(new WorldBorderEmulator(connection));

		if (!connection.has(ClientWorld.class)) {
			connection.put(new ClientWorld());
		}
	}

	@Override
	public void register(ViaProviders providers) {
		providers.register(CompressionHandlerProvider.class, new TrackingCompressionHandlerProvider());

		if (ViaRewind.getConfig().isEmulateWorldBorder()) {
			Via.getManager().getScheduler().scheduleRepeating(new WorldBorderUpdateTask(), 0L, 50L, TimeUnit.MILLISECONDS);
		}
	}

	@Override
	public RewindMappingData getMappingData() {
		return MAPPINGS;
	}

	@Override
	public BlockItemPacketRewriter1_8 getItemRewriter() {
		return itemRewriter;
	}

	@Override
	public EntityPacketRewriter1_8 getEntityRewriter() {
		return entityRewriter;
	}

	@Override
	public boolean hasMappingDataToLoad() {
		return true;
	}
}
