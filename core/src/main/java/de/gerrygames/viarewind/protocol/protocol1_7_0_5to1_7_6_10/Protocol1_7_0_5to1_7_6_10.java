package de.gerrygames.viarewind.protocol.protocol1_7_0_5to1_7_6_10;

import de.gerrygames.viarewind.protocol.protocol1_7_6_10to1_8.types.Types1_7_6_10;
import us.myles.ViaVersion.api.PacketWrapper;
import us.myles.ViaVersion.api.data.UserConnection;
import us.myles.ViaVersion.api.protocol.Protocol;
import us.myles.ViaVersion.api.remapper.PacketHandler;
import us.myles.ViaVersion.api.remapper.PacketRemapper;
import us.myles.ViaVersion.api.remapper.ValueTransformer;
import us.myles.ViaVersion.api.type.Type;
import us.myles.ViaVersion.packets.State;

import java.util.ArrayList;
import java.util.List;

public class Protocol1_7_0_5to1_7_6_10 extends Protocol {
	public static final ValueTransformer<String,String> REMOVE_DASHES = new ValueTransformer<String,String>(Type.STRING) {
		@Override
		public String transform(PacketWrapper packetWrapper, String s) {
			return s.replace("-","");
		}
	};
	@Override
	protected void registerPackets() {
		//Login Success
		this.registerOutgoing(State.LOGIN, 0x02, 0x02, new PacketRemapper() {
			@Override
			public void registerMap() {
				map(Type.STRING, REMOVE_DASHES);
				map(Type.STRING);
			}
		});

		//Spawn Player
		this.registerOutgoing(State.PLAY, 0x0C, 0x0C, new PacketRemapper() {
			@Override
			public void registerMap() {
				map(Type.VAR_INT);
				map(Type.STRING, REMOVE_DASHES);
				map(Type.STRING);
				handler(new PacketHandler() {
					@Override
					public void handle(PacketWrapper packetWrapper) throws Exception {
						int size = packetWrapper.read(Type.VAR_INT);
						for (int i = 0; i < size * 3; i++) packetWrapper.read(Type.STRING);
					}
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
		this.registerOutgoing(State.PLAY, 0x3E, 0x3E, new PacketRemapper() {
			@Override
			public void registerMap() {
				map(Type.STRING);
				map(Type.BYTE);
				handler(new PacketHandler() {
					@Override
					public void handle(PacketWrapper packetWrapper) throws Exception {
						byte mode = packetWrapper.get(Type.BYTE, 0);
						if (mode==0 || mode==2) {
							packetWrapper.passthrough(Type.STRING);
							packetWrapper.passthrough(Type.STRING);
							packetWrapper.passthrough(Type.STRING);
							packetWrapper.passthrough(Type.BYTE);
						}
						if (mode==0 || mode==3 || mode==4) {
							int size = packetWrapper.read(Type.SHORT);
							List<String> entryList = new ArrayList<>();

							for (int i = 0; i<size; i++) {
								String entry = packetWrapper.read(Type.STRING);
								if (entry==null) continue;
								if (entry.length()>16) {
									entry = entry.substring(0, 16);
								}
								if (entryList.contains(entry)) continue;
								entryList.add(entry);
							}

							packetWrapper.write(Type.SHORT, (short)entryList.size());
							for (String entry : entryList) {
								packetWrapper.write(Type.STRING, entry);
							}
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
