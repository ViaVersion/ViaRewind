package de.gerrygames.viarewind.protocol.protocol1_8to1_9.packets;

import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.protocol.Protocol;
import com.viaversion.viaversion.api.protocol.remapper.PacketHandler;
import com.viaversion.viaversion.api.protocol.remapper.PacketRemapper;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.api.protocol.packet.State;

public class ScoreboardPackets {

	public static void register(Protocol protocol) {
		/*  OUTGOING  */

		//Display Scoreboard
		protocol.registerClientbound(State.PLAY, 0x38, 0x3D);

		//Scoreboard Objective
		protocol.registerClientbound(State.PLAY, 0x3F, 0x3B);

		//Scoreboard Team
		protocol.registerClientbound(State.PLAY, 0x41, 0x3E, new PacketRemapper() {
			@Override
			public void registerMap() {
				map(Type.STRING);
				map(Type.BYTE);
				handler(new PacketHandler() {
					@Override
					public void handle(PacketWrapper packetWrapper) throws Exception {
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
					}
				});
			}
		});

		//Update Score
		protocol.registerClientbound(State.PLAY, 0x42, 0x3C);
	}
}
