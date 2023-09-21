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

package com.viaversion.viarewind.protocol.protocol1_7_2_5to1_7_6_10;

import com.viaversion.viarewind.protocol.protocol1_7_6_10to1_8.ClientboundPackets1_7;
import com.viaversion.viarewind.protocol.protocol1_7_6_10to1_8.ServerboundPackets1_7;
import com.viaversion.viarewind.protocol.protocol1_7_6_10to1_8.types.Types1_7_6_10;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.protocol.AbstractProtocol;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.protocol.packet.State;
import com.viaversion.viaversion.api.protocol.remapper.PacketHandlers;
import com.viaversion.viaversion.api.protocol.remapper.ValueTransformer;
import com.viaversion.viaversion.api.type.Type;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Protocol1_7_2_5To1_7_6_10 extends AbstractProtocol<ClientboundPackets1_7, ClientboundPackets1_7, ServerboundPackets1_7, ServerboundPackets1_7> {
	public static final ValueTransformer<String, String> REMOVE_DASHES = new ValueTransformer<String, String>(Type.STRING) {
		@Override
		public String transform(PacketWrapper packetWrapper, String s) {
			return s.replace("-", "");
		}
	};

	public Protocol1_7_2_5To1_7_6_10() {
		super(ClientboundPackets1_7.class, ClientboundPackets1_7.class, ServerboundPackets1_7.class, ServerboundPackets1_7.class);
	}

	@Override
	protected void registerPackets() {
		//Login Success
		this.registerClientbound(State.LOGIN, 0x02, 0x02, new PacketHandlers() {
			@Override
			public void register() {
				map(Type.STRING, REMOVE_DASHES);
				map(Type.STRING);
			}
		});

		//Spawn Player
		this.registerClientbound(ClientboundPackets1_7.SPAWN_PLAYER, new PacketHandlers() {
			@Override
			public void register() {
				map(Type.VAR_INT);
				map(Type.STRING, REMOVE_DASHES);
				map(Type.STRING);
				handler(packetWrapper -> {
					int size = packetWrapper.read(Type.VAR_INT);
					for (int i = 0; i < size * 3; i++) packetWrapper.read(Type.STRING);
				});
				map(Type.INT);
				map(Type.INT);
				map(Type.INT);
				map(Type.BYTE);
				map(Type.BYTE);
				map(Type.SHORT);
				map(Types1_7_6_10.METADATA_LIST);
			}
		});

		//Teams
		this.registerClientbound(ClientboundPackets1_7.TEAMS, new PacketHandlers() {
			@Override
			public void register() {
				map(Type.STRING);
				map(Type.BYTE);
				handler(packetWrapper -> {
					byte mode = packetWrapper.get(Type.BYTE, 0);
					if (mode == 0 || mode == 2) {
						packetWrapper.passthrough(Type.STRING);
						packetWrapper.passthrough(Type.STRING);
						packetWrapper.passthrough(Type.STRING);
						packetWrapper.passthrough(Type.BYTE);
					}
					if (mode == 0 || mode == 3 || mode == 4) {
						List<String> entryList = new ArrayList<>();
						int size = packetWrapper.read(Type.SHORT);
						for (int i = 0; i < size; i++) {
							entryList.add(packetWrapper.read(Type.STRING));
						}

						entryList = entryList.stream()
								.map(it -> it.length() > 16 ? it.substring(0, 16) : it)
								.distinct()
								.collect(Collectors.toList());

						packetWrapper.write(Type.SHORT, (short) entryList.size());
						for (String entry : entryList) {
							packetWrapper.write(Type.STRING, entry);
						}
					}
				});
			}
		});
	}

	@Override
	public void init(UserConnection userConnection) {

	}
}
