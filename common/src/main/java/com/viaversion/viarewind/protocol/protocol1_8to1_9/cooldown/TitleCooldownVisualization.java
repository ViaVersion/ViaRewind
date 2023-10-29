package com.viaversion.viarewind.protocol.protocol1_8to1_9.cooldown;

import com.viaversion.viarewind.protocol.protocol1_8to1_9.Protocol1_8To1_9;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.libs.gson.JsonPrimitive;
import com.viaversion.viaversion.protocols.protocol1_8.ClientboundPackets1_8;
import java.util.function.Consumer;

public class TitleCooldownVisualization implements CooldownVisualization {
	private final UserConnection user;

	public TitleCooldownVisualization(UserConnection user) {
		this.user = user;
	}

	@Override
	public void show(double progress) throws Exception {
		String text = CooldownVisualization.buildProgressText("˙", progress);
		sendTitle("", text, 0, 2, 5);
	}

	@Override
	public void hide() throws Exception {
		sendTitlePacket(ACTION_HIDE, wrapper -> {});
	}

	private static final int ACTION_SET_TITLE = 0;
	private static final int ACTION_SET_SUBTITLE = 1;
	private static final int ACTION_SET_TIMES_AND_DISPLAY = 2;
	private static final int ACTION_HIDE = 3;

	private void sendTitle(String titleText, String subTitleText, int fadeIn, int stay, int fadeOut) throws Exception {
		sendTitle(titleText);
		sendTitlePacket(
			ACTION_SET_TITLE,
			packet -> packet.write(Type.COMPONENT, new JsonPrimitive(titleText))
		);
		sendTitlePacket(
			ACTION_SET_SUBTITLE,
			packet -> packet.write(Type.COMPONENT, new JsonPrimitive(subTitleText))
		);
		sendTitlePacket(
			ACTION_SET_TIMES_AND_DISPLAY,
			packet -> {
				packet.write(Type.INT, fadeIn);
				packet.write(Type.INT, stay);
				packet.write(Type.INT, fadeOut);
			}
		);
	}

	private void sendTitle(String titleText) throws Exception {
		PacketWrapper titlePacket = PacketWrapper.create(ClientboundPackets1_8.TITLE, user);
		titlePacket.write(Type.VAR_INT, 0); // Action - set title
		titlePacket.write(Type.COMPONENT, new JsonPrimitive(titleText));
		titlePacket.scheduleSend(Protocol1_8To1_9.class);
	}

	private void sendTitlePacket(int action, Consumer<PacketWrapper> writer) throws Exception {
		PacketWrapper titlePacket = PacketWrapper.create(ClientboundPackets1_8.TITLE, user);
		titlePacket.write(Type.VAR_INT, action);
		writer.accept(titlePacket);
		titlePacket.scheduleSend(Protocol1_8To1_9.class);
	}
}
