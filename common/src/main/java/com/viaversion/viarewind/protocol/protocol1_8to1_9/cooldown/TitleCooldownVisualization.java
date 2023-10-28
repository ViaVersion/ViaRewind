package com.viaversion.viarewind.protocol.protocol1_8to1_9.cooldown;

import com.viaversion.viarewind.protocol.protocol1_8to1_9.Protocol1_8To1_9;
import com.viaversion.viarewind.utils.PacketUtil;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.libs.gson.JsonPrimitive;
import com.viaversion.viaversion.protocols.protocol1_8.ClientboundPackets1_8;

public class TitleCooldownVisualization implements CooldownVisualization {
	private final UserConnection user;

	public TitleCooldownVisualization(UserConnection user) {
		this.user = user;
	}

	@Override
	public void show(double progress) {
		String text = CooldownVisualization.buildProgressText("˙", progress);
		sendTitle("", text, 0, 2, 5);
	}

	@Override
	public void hide() {
		PacketWrapper hide = PacketWrapper.create(ClientboundPackets1_8.TITLE, user);
		hide.write(Type.VAR_INT, 3);
		PacketUtil.sendPacket(hide, Protocol1_8To1_9.class);
	}

	private void sendTitle(String title, String subTitle, int fadeIn, int stay, int fadeOut) {
		PacketWrapper timePacket = PacketWrapper.create(ClientboundPackets1_8.TITLE, user);
		timePacket.write(Type.VAR_INT, 2);
		timePacket.write(Type.INT, fadeIn);
		timePacket.write(Type.INT, stay);
		timePacket.write(Type.INT, fadeOut);
		PacketWrapper titlePacket = PacketWrapper.create(ClientboundPackets1_8.TITLE, user);
		titlePacket.write(Type.VAR_INT, 0);
		titlePacket.write(Type.COMPONENT, new JsonPrimitive(title));
		PacketWrapper subtitlePacket = PacketWrapper.create(ClientboundPackets1_8.TITLE, user);
		subtitlePacket.write(Type.VAR_INT, 1);
		subtitlePacket.write(Type.COMPONENT, new JsonPrimitive(subTitle));

		PacketUtil.sendPacket(titlePacket, Protocol1_8To1_9.class);
		PacketUtil.sendPacket(subtitlePacket, Protocol1_8To1_9.class);
		PacketUtil.sendPacket(timePacket, Protocol1_8To1_9.class);
	}
}
