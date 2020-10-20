package de.gerrygames.viarewind.protocol.protocol1_8to1_9.packets;

import us.myles.ViaVersion.api.protocol.Protocol;
import us.myles.ViaVersion.api.remapper.PacketRemapper;
import us.myles.ViaVersion.api.type.Type;
import us.myles.ViaVersion.packets.State;

public class ScoreboardPackets {

	public static void register(Protocol protocol) {
		/*  OUTGOING  */

		//Display Scoreboard
		protocol.registerOutgoing(State.PLAY, 0x38, 0x3D);

		//Scoreboard Objective
		protocol.registerOutgoing(State.PLAY, 0x3F, 0x3B);

		//Scoreboard Team
		protocol.registerOutgoing(State.PLAY, 0x41, 0x3E, new PacketRemapper() {
			@Override
			public void registerMap() {
				map(Type.STRING);
				map(Type.BYTE);

				handler(wrapper -> {
					byte mode = wrapper.get(Type.BYTE, 0);
					if (mode == 0 || mode == 2) {
						wrapper.passthrough(Type.STRING);  //Display Name
						wrapper.passthrough(Type.STRING);  //Prefix
						wrapper.passthrough(Type.STRING);  //Suffix
						wrapper.passthrough(Type.BYTE);  //Friendly Flags
						wrapper.passthrough(Type.STRING);  //Name Tag Visibility
						wrapper.read(Type.STRING);  //Skip Collision Rule
						wrapper.passthrough(Type.BYTE);  //Friendly Flags
					}

					if (mode == 0 || mode == 3 || mode == 4) {
						wrapper.passthrough(Type.STRING_ARRAY);
					}
				});
			}
		});

		//Update Score
		protocol.registerOutgoing(State.PLAY, 0x42, 0x3C);
	}
}
