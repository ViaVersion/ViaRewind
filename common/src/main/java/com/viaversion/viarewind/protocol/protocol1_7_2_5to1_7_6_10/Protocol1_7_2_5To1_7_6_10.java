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

package com.viaversion.viarewind.protocol.protocol1_7_2_5to1_7_6_10;

import com.viaversion.viarewind.protocol.protocol1_7_6_10to1_8.types.Types1_7_6_10;
import com.viaversion.viaversion.api.protocol.AbstractProtocol;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.protocol.packet.State;
import com.viaversion.viaversion.api.protocol.remapper.PacketHandlers;
import com.viaversion.viaversion.api.protocol.remapper.ValueTransformer;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.protocols.base.ClientboundLoginPackets;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Protocol1_7_2_5To1_7_6_10 extends AbstractProtocol<ClientboundPackets1_7_2_5, ClientboundPackets1_7_2_5, ServerboundPackets1_7_2_5, ServerboundPackets1_7_2_5> {

	public static final ValueTransformer<String, String> REMOVE_DASHES = new ValueTransformer<String, String>(Type.STRING) {
		@Override
		public String transform(PacketWrapper wrapper, String s) {
			return s.replace("-", "");
		}
	};

	public Protocol1_7_2_5To1_7_6_10() {
		super(ClientboundPackets1_7_2_5.class, ClientboundPackets1_7_2_5.class, ServerboundPackets1_7_2_5.class, ServerboundPackets1_7_2_5.class);
	}

	@Override
	protected void registerPackets() {
		this.registerClientbound(State.LOGIN, ClientboundLoginPackets.GAME_PROFILE.getId(), ClientboundLoginPackets.GAME_PROFILE.getId(), new PacketHandlers() {
			@Override
			public void register() {
				map(Type.STRING, REMOVE_DASHES); // uuid
				map(Type.STRING); // name
			}
		});

		this.registerClientbound(ClientboundPackets1_7_2_5.SPAWN_PLAYER, new PacketHandlers() {
			@Override
			public void register() {
				map(Type.VAR_INT); // entity id
				map(Type.STRING, REMOVE_DASHES); // uuid
				map(Type.STRING); // name
				handler(wrapper -> { // delete data introduced in 1.7.6
					final int size = wrapper.read(Type.VAR_INT); // data count
					for (int i = 0; i < size; i++) {
						wrapper.read(Type.STRING); // data name
						wrapper.read(Type.STRING); // data value
						wrapper.read(Type.STRING); // data signature
					}
				});
				map(Type.INT); // x
				map(Type.INT); // y
				map(Type.INT); // z
				map(Type.BYTE); // yaw
				map(Type.BYTE); // pitch
				map(Type.SHORT); // item in hand
				map(Types1_7_6_10.METADATA_LIST); // metadata
			}
		});

		this.registerClientbound(ClientboundPackets1_7_2_5.TEAMS, new PacketHandlers() {
			@Override
			public void register() {
				map(Type.STRING); // team name
				map(Type.BYTE); // mode
				handler(wrapper -> {
					final byte mode = wrapper.get(Type.BYTE, 0);
					if (mode == 0 || mode == 2) { // team is created or information is updated
						wrapper.passthrough(Type.STRING); // team display name
						wrapper.passthrough(Type.STRING); // team prefix
						wrapper.passthrough(Type.STRING); // team suffix
						wrapper.passthrough(Type.BYTE); // friendly fire
					}
					if (mode == 0 || mode == 3 || mode == 4) { // team is created, player is added or player is removed
						List<String> entryList = new ArrayList<>();
						final int size = wrapper.read(Type.SHORT);
						for (int i = 0; i < size; i++) {
							entryList.add(wrapper.read(Type.STRING));
						}

						entryList = entryList.stream()
							.map(it -> it.length() > 16 ? it.substring(0, 16) : it) // trim to 16 characters
							.distinct() // remove duplicates
							.collect(Collectors.toList());

						wrapper.write(Type.SHORT, (short) entryList.size());
						for (String entry : entryList) {
							wrapper.write(Type.STRING, entry);
						}
					}
				});
			}
		});
	}
}
