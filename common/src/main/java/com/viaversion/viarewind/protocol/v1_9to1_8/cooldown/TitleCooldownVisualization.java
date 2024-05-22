/*
 * This file is part of ViaRewind - https://github.com/ViaVersion/ViaRewind
 * Copyright (C) 2018-2024 ViaVersion and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.viaversion.viarewind.protocol.v1_9to1_8.cooldown;

import com.viaversion.viarewind.protocol.v1_9to1_8.Protocol1_9To1_8;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.libs.gson.JsonPrimitive;
import com.viaversion.viaversion.protocols.v1_8to1_9.packet.ClientboundPackets1_8;

import java.util.function.Consumer;

public class TitleCooldownVisualization implements CooldownVisualization {

	private static final int ACTION_SET_TITLE = 0;
	private static final int ACTION_SET_SUBTITLE = 1;
	private static final int ACTION_SET_TIMES_AND_DISPLAY = 2;
	private static final int ACTION_HIDE = 3;

	private final UserConnection user;

	public TitleCooldownVisualization(UserConnection user) {
		this.user = user;
	}

	@Override
	public void show(double progress) throws Exception {
		final String text = CooldownVisualization.buildProgressText("˙", progress);

		sendTitlePacket(ACTION_SET_TITLE, wrapper -> wrapper.write(Types.COMPONENT, new JsonPrimitive("")));
		sendTitlePacket(ACTION_SET_SUBTITLE, wrapper -> wrapper.write(Types.COMPONENT, new JsonPrimitive(text)));
		sendTitlePacket(ACTION_SET_TIMES_AND_DISPLAY, wrapper -> {
			wrapper.write(Types.INT, 0);
			wrapper.write(Types.INT, 2);
			wrapper.write(Types.INT, 5);
		});
	}

	@Override
	public void hide() throws Exception {
		sendTitlePacket(ACTION_HIDE, wrapper -> {});
	}

	private void sendTitlePacket(int action, Consumer<PacketWrapper> writer) {
		final PacketWrapper title = PacketWrapper.create(ClientboundPackets1_8.SET_TITLES, user);
		title.write(Types.VAR_INT, action);
		writer.accept(title);
		title.scheduleSend(Protocol1_9To1_8.class);
	}

}
