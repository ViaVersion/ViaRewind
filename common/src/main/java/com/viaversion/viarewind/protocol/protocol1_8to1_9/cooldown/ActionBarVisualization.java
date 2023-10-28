package com.viaversion.viarewind.protocol.protocol1_8to1_9.cooldown;

import com.viaversion.viarewind.protocol.protocol1_8to1_9.Protocol1_8To1_9;
import com.viaversion.viarewind.utils.PacketUtil;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.libs.gson.JsonPrimitive;
import com.viaversion.viaversion.protocols.protocol1_8.ClientboundPackets1_8;

public class ActionBarVisualization implements CooldownVisualization {
	private final UserConnection user;

	public ActionBarVisualization(UserConnection user) {
		this.user = user;
	}

	@Override
	public void show(double progress) {
		sendActionBar(CooldownVisualization.buildProgressText("■", progress));
	}

	@Override
	public void hide() {
		sendActionBar("§r");
	}

	private void sendActionBar(String bar) {
		PacketWrapper actionBarPacket = PacketWrapper.create(ClientboundPackets1_8.CHAT_MESSAGE, user);
		actionBarPacket.write(Type.COMPONENT, new JsonPrimitive(bar));
		actionBarPacket.write(Type.BYTE, (byte) 2);

		PacketUtil.sendPacket(actionBarPacket, Protocol1_8To1_9.class);
	}
}
