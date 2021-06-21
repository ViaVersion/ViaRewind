package de.gerrygames.viarewind.protocol.protocol1_8to1_9.packets;

import com.viaversion.viaversion.api.protocol.Protocol;
import com.viaversion.viaversion.api.protocol.remapper.PacketRemapper;
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
		protocol.registerClientbound(ClientboundPackets1_9.TEAMS, new PacketRemapper() {
			@Override
			public void registerMap() {
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
