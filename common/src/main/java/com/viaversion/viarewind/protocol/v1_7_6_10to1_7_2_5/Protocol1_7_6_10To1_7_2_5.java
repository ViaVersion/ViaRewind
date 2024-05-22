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
package com.viaversion.viarewind.protocol.v1_7_6_10to1_7_2_5;

import com.viaversion.viarewind.api.type.version.Types1_7_6_10;
import com.viaversion.viarewind.protocol.v1_7_6_10to1_7_2_5.packet.ClientboundPackets1_7_2_5;
import com.viaversion.viarewind.protocol.v1_7_6_10to1_7_2_5.packet.ServerboundPackets1_7_2_5;
import com.viaversion.viaversion.api.protocol.AbstractProtocol;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.protocol.packet.State;
import com.viaversion.viaversion.api.protocol.remapper.PacketHandlers;
import com.viaversion.viaversion.api.protocol.remapper.ValueTransformer;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.protocols.base.ClientboundLoginPackets;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Protocol1_7_6_10To1_7_2_5 extends AbstractProtocol<ClientboundPackets1_7_2_5, ClientboundPackets1_7_2_5, ServerboundPackets1_7_2_5, ServerboundPackets1_7_2_5> {

	public static final ValueTransformer<String, String> REMOVE_DASHES = new ValueTransformer<>(Types.STRING) {
		@Override
		public String transform(PacketWrapper wrapper, String s) {
			return s.replace("-", "");
		}
	};

	public Protocol1_7_6_10To1_7_2_5() {
		super(ClientboundPackets1_7_2_5.class, ClientboundPackets1_7_2_5.class, ServerboundPackets1_7_2_5.class, ServerboundPackets1_7_2_5.class);
	}

	@Override
	protected void registerPackets() {
		this.registerClientbound(State.LOGIN, ClientboundLoginPackets.GAME_PROFILE.getId(), ClientboundLoginPackets.GAME_PROFILE.getId(), new PacketHandlers() {
			@Override
			public void register() {
				map(Types.STRING, REMOVE_DASHES); // Uuid
				map(Types.STRING); // Name
			}
		});

		this.registerClientbound(ClientboundPackets1_7_2_5.ADD_PLAYER, new PacketHandlers() {
			@Override
			public void register() {
				map(Types.VAR_INT); // Entity id
				map(Types.STRING, REMOVE_DASHES); // Uuid
				map(Types.STRING); // Name
				handler(wrapper -> {
					final int size = wrapper.read(Types.VAR_INT); // Data count
					for (int i = 0; i < size; i++) {
						wrapper.read(Types.STRING); // Data name
						wrapper.read(Types.STRING); // Data value
						wrapper.read(Types.STRING); // Data signature
					}
				});
				map(Types.INT); // X
				map(Types.INT); // Y
				map(Types.INT); // Z
				map(Types.BYTE); // Yaw
				map(Types.BYTE); // Pitch
				map(Types.SHORT); // Item in hand
				map(Types1_7_6_10.ENTITY_DATA_LIST); // Entity data
			}
		});

		this.registerClientbound(ClientboundPackets1_7_2_5.SET_PLAYER_TEAM, new PacketHandlers() {
			@Override
			public void register() {
				map(Types.STRING); // Team name
				map(Types.BYTE); // Mode
				handler(wrapper -> {
					final byte mode = wrapper.get(Types.BYTE, 0);
					if (mode == 0 || mode == 2) { // Team is created or information is updated
						wrapper.passthrough(Types.STRING); // Team display name
						wrapper.passthrough(Types.STRING); // Team prefix
						wrapper.passthrough(Types.STRING); // Team suffix
						wrapper.passthrough(Types.BYTE); // Friendly fire
					}
					if (mode == 0 || mode == 3 || mode == 4) { // Team is created, player is added or player is removed
						List<String> entryList = new ArrayList<>();
						final int size = wrapper.read(Types.SHORT);
						for (int i = 0; i < size; i++) {
							entryList.add(wrapper.read(Types.STRING));
						}

						entryList = entryList.stream()
							.map(it -> it.length() > 16 ? it.substring(0, 16) : it) // trim to 16 characters
							.distinct() // remove duplicates
							.collect(Collectors.toList());

						wrapper.write(Types.SHORT, (short) entryList.size());
						for (String entry : entryList) {
							wrapper.write(Types.STRING, entry);
						}
					}
				});
			}
		});
	}
}
