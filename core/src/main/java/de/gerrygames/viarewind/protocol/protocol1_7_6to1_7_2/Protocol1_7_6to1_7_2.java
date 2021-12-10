package de.gerrygames.viarewind.protocol.protocol1_7_6to1_7_2;

import com.viaversion.viaversion.api.protocol.AbstractProtocol;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.protocol.packet.State;
import com.viaversion.viaversion.api.protocol.remapper.PacketRemapper;
import com.viaversion.viaversion.api.protocol.remapper.ValueTransformer;
import com.viaversion.viaversion.api.type.Type;
import de.gerrygames.viarewind.protocol.protocol1_7_6_10to1_8.ClientboundPackets1_7;
import de.gerrygames.viarewind.protocol.protocol1_7_6_10to1_8.ServerboundPackets1_7;

public class Protocol1_7_6to1_7_2 extends AbstractProtocol<ClientboundPackets1_7, ClientboundPackets1_7, ServerboundPackets1_7, ServerboundPackets1_7> {
	public static ValueTransformer<String, String> INSERT_DASHES = new ValueTransformer<String, String>(Type.STRING) {
		@Override
		public String transform(PacketWrapper wrapper, String inputValue) throws Exception {
			StringBuilder builder = new StringBuilder(inputValue);
			builder.insert(20, "-");
			builder.insert(16, "-");
			builder.insert(12, "-");
			builder.insert(8, "-");
			return builder.toString();
		}
	};

	public Protocol1_7_6to1_7_2() {
		super(ClientboundPackets1_7.class, ClientboundPackets1_7.class, ServerboundPackets1_7.class, ServerboundPackets1_7.class);
	}

	@Override
	protected void registerPackets() {
		//Login Success
		this.registerClientbound(State.LOGIN, 0x02, 0x02, new PacketRemapper() {
			@Override
			public void registerMap() {
				map(Type.STRING, INSERT_DASHES);
			}
		});

		//Spawn Player
		this.registerClientbound(ClientboundPackets1_7.SPAWN_PLAYER, new PacketRemapper() {
			@Override
			public void registerMap() {
				map(Type.VAR_INT);
				map(Type.STRING, INSERT_DASHES);
				map(Type.STRING);
				create(Type.VAR_INT, 0);
			}
		});
	}
}
