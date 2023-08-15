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

package de.gerrygames.viarewind.protocol.protocol1_8to1_9.packets;

import com.viaversion.viaversion.api.protocol.Protocol;
import com.viaversion.viaversion.api.protocol.remapper.PacketHandlers;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.protocols.protocol1_8.ClientboundPackets1_8;
import com.viaversion.viaversion.protocols.protocol1_8.ServerboundPackets1_8;
import com.viaversion.viaversion.protocols.protocol1_9to1_8.ClientboundPackets1_9;
import com.viaversion.viaversion.protocols.protocol1_9to1_8.ServerboundPackets1_9;

public class ScoreboardPackets {

	public static void register(Protocol<ClientboundPackets1_9, ClientboundPackets1_8,
			ServerboundPackets1_9, ServerboundPackets1_8> protocol) {
		/*  OUTGOING  */

		//Display Scoreboard
		//Scoreboard Objective

		//Scoreboard Team
		protocol.registerClientbound(ClientboundPackets1_9.TEAMS, new PacketHandlers() {
			@Override
			public void register() {
				map(Type.STRING);
				map(Type.BYTE);
				handler(packetWrapper -> {
					byte mode = packetWrapper.get(Type.BYTE, 0);
					if (mode == 0 || mode == 2) {
						packetWrapper.passthrough(Type.STRING);  //Display Name
						packetWrapper.passthrough(Type.STRING);  //Prefix
						packetWrapper.passthrough(Type.STRING);  //Suffix
						packetWrapper.passthrough(Type.BYTE);  //Friendly Flags
						packetWrapper.passthrough(Type.STRING);  //Name Tag Visibility
						packetWrapper.read(Type.STRING);  //Skip Collision Rule
						packetWrapper.passthrough(Type.BYTE);  //Friendly Flags
					}

					if (mode == 0 || mode == 3 || mode == 4) {
						packetWrapper.passthrough(Type.STRING_ARRAY);
					}
				});
			}
		});

		//Update Score
	}
}
