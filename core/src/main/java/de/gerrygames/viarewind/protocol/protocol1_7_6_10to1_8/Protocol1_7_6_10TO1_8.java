package de.gerrygames.viarewind.protocol.protocol1_7_6_10to1_8;

import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.platform.providers.ViaProviders;
import com.viaversion.viaversion.api.protocol.AbstractProtocol;
import com.viaversion.viaversion.api.protocol.packet.Direction;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.protocol.packet.State;
import com.viaversion.viaversion.api.protocol.remapper.PacketRemapper;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.protocols.protocol1_8.ClientboundPackets1_8;
import com.viaversion.viaversion.protocols.protocol1_8.ServerboundPackets1_8;
import com.viaversion.viaversion.protocols.protocol1_9_3to1_9_1_2.storage.ClientWorld;
import de.gerrygames.viarewind.protocol.protocol1_7_6_10to1_8.packets.*;
import de.gerrygames.viarewind.protocol.protocol1_7_6_10to1_8.provider.CompressionHandlerProvider;
import de.gerrygames.viarewind.protocol.protocol1_7_6_10to1_8.storage.*;
import de.gerrygames.viarewind.utils.Ticker;

public class Protocol1_7_6_10TO1_8 extends AbstractProtocol<ClientboundPackets1_8, ClientboundPackets1_7,
		ServerboundPackets1_8, ServerboundPackets1_7> {

	public Protocol1_7_6_10TO1_8() {
		super(ClientboundPackets1_8.class, ClientboundPackets1_7.class, ServerboundPackets1_8.class, ServerboundPackets1_7.class);
	}

	@Override
	protected void registerPackets() {
		EntityPackets.register(this);
		InventoryPackets.register(this);
		PlayerPackets.register(this);
		ScoreboardPackets.register(this);
		SpawnPackets.register(this);
		WorldPackets.register(this);

		this.registerClientbound(ClientboundPackets1_8.KEEP_ALIVE, new PacketRemapper() {
			@Override
			public void registerMap() {
				map(Type.VAR_INT, Type.INT);
			}
		});

		this.cancelClientbound(ClientboundPackets1_8.SET_COMPRESSION); // unused

		this.registerServerbound(ServerboundPackets1_7.KEEP_ALIVE, new PacketRemapper() {
			@Override
			public void registerMap() {
				map(Type.INT, Type.VAR_INT);
			}
		});

		//Encryption Request
		this.registerClientbound(State.LOGIN, 0x01, 0x01, new PacketRemapper() {
			@Override
			public void registerMap() {
				map(Type.STRING);  //Server ID
				map(Type.BYTE_ARRAY_PRIMITIVE, Type.SHORT_BYTE_ARRAY); // Public key
				map(Type.BYTE_ARRAY_PRIMITIVE, Type.SHORT_BYTE_ARRAY); // Verification token
			}
		});

		//Set Compression
		this.registerClientbound(State.LOGIN, 0x03, 0x03, new PacketRemapper() {
			@Override
			public void registerMap() {
				handler(packetWrapper -> {
					Via.getManager().getProviders().get(CompressionHandlerProvider.class)
							.handleSetCompression(packetWrapper.user(), packetWrapper.read(Type.VAR_INT));
					packetWrapper.cancel();
				});
			}
		});

		//Encryption Response
		this.registerServerbound(State.LOGIN, 0x01, 0x01, new PacketRemapper() {
			@Override
			public void registerMap() {
				map(Type.SHORT_BYTE_ARRAY, Type.BYTE_ARRAY_PRIMITIVE); // Shared secret
				map(Type.SHORT_BYTE_ARRAY, Type.BYTE_ARRAY_PRIMITIVE); // Verification token
			}
		});
	}

	@Override
	public void transform(Direction direction, State state, PacketWrapper packetWrapper) throws Exception {
		Via.getManager().getProviders().get(CompressionHandlerProvider.class)
				.handleTransform(packetWrapper.user());

		super.transform(direction, state, packetWrapper);
	}

	@Override
	public void init(UserConnection userConnection) {
		Ticker.init();

		userConnection.put(new Windows(userConnection));
		userConnection.put(new EntityTracker(userConnection));
		userConnection.put(new PlayerPosition(userConnection));
		userConnection.put(new GameProfileStorage(userConnection));
		userConnection.put(new Scoreboard(userConnection));
		userConnection.put(new CompressionSendStorage(userConnection));
		userConnection.put(new WorldBorder(userConnection));
		userConnection.put(new PlayerAbilities(userConnection));
		userConnection.put(new ClientWorld(userConnection));
	}

	@Override
	public void register(ViaProviders providers) {
		providers.register(CompressionHandlerProvider.class, new CompressionHandlerProvider());
	}
}
