package de.gerrygames.viarewind.protocol.protocol1_7_0_5to1_7_6_10;

import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.protocol.AbstractProtocol;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.protocol.packet.State;
import com.viaversion.viaversion.api.protocol.remapper.PacketRemapper;
import com.viaversion.viaversion.api.protocol.remapper.ValueTransformer;
import com.viaversion.viaversion.api.type.Type;
import de.gerrygames.viarewind.protocol.protocol1_7_6_10to1_8.ClientboundPackets1_7;
import de.gerrygames.viarewind.protocol.protocol1_7_6_10to1_8.ServerboundPackets1_7;
import de.gerrygames.viarewind.protocol.protocol1_7_6_10to1_8.types.Types1_7_6_10;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Protocol1_7_0_5to1_7_6_10 extends AbstractProtocol<ClientboundPackets1_7, ClientboundPackets1_7, ServerboundPackets1_7, ServerboundPackets1_7> {
	public static final ValueTransformer<String, String> REMOVE_DASHES = new ValueTransformer<String, String>(Type.STRING) {
		@Override
		public String transform(PacketWrapper packetWrapper, String s) {
			return s.replace("-", "");
		}
	};

	public Protocol1_7_0_5to1_7_6_10() {
		super(ClientboundPackets1_7.class, ClientboundPackets1_7.class, ServerboundPackets1_7.class, ServerboundPackets1_7.class);
	}

	@Override
	protected void registerPackets() {
		//Login Success
		this.registerClientbound(State.LOGIN, 0x02, 0x02, new PacketRemapper() {
			@Override
			public void registerMap() {
				map(Type.STRING, REMOVE_DASHES);
				map(Type.STRING);
			}
		});

		//Spawn Player
		this.registerClientbound(ClientboundPackets1_7.SPAWN_PLAYER, new PacketRemapper() {
			@Override
			public void registerMap() {
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
		this.registerClientbound(ClientboundPackets1_7.TEAMS, new PacketRemapper() {
			@Override
			public void registerMap() {
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
