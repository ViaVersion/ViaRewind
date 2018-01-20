package de.gerrygames.viarewind.protocol.protocol1_7_0_5to1_7_6_10;

import de.gerrygames.viarewind.protocol.protocol1_7_0_5to1_7_6_10.types.Types1_7_1_5;
import de.gerrygames.viarewind.protocol.protocol1_7_6_10to1_8.types.Types1_7_6_10;
import us.myles.ViaVersion.api.PacketWrapper;
import us.myles.ViaVersion.api.data.UserConnection;
import us.myles.ViaVersion.api.protocol.Protocol;
import us.myles.ViaVersion.api.remapper.PacketHandler;
import us.myles.ViaVersion.api.remapper.PacketRemapper;
import us.myles.ViaVersion.api.remapper.ValueTransformer;
import us.myles.ViaVersion.api.type.Type;
import us.myles.ViaVersion.packets.State;

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
						packetWrapper.read(Type.VAR_INT);
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
	}

	@Override
	public void init(UserConnection userConnection) {

	}
}
