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

package com.viaversion.viarewind.protocol.protocol1_8to1_9.cooldown;

import com.viaversion.viarewind.protocol.protocol1_8to1_9.Protocol1_8To1_9;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.libs.gson.JsonPrimitive;
import com.viaversion.viaversion.protocols.protocol1_9to1_8.ClientboundPackets1_9;
import java.util.UUID;

public class BossBarVisualization implements CooldownVisualization {
	private final UserConnection user;
	private UUID bossUUID;

	public BossBarVisualization(UserConnection user) {
		this.user = user;
	}

	@Override
	public void show(double progress) throws Exception {
		PacketWrapper wrapper = PacketWrapper.create(ClientboundPackets1_9.BOSSBAR, user);
		if (bossUUID == null) {
			bossUUID = UUID.randomUUID();
			wrapper.write(Type.UUID, bossUUID);
			wrapper.write(Type.VAR_INT, 0); // Action - add
			wrapper.write(Type.COMPONENT, new JsonPrimitive(" ")); // Title
			wrapper.write(Type.FLOAT, (float) progress); // Health
			wrapper.write(Type.VAR_INT, 0); // Color
			wrapper.write(Type.VAR_INT, 0); // Division
			wrapper.write(Type.UNSIGNED_BYTE, (short) 0); // Flags
		} else {
			wrapper.write(Type.UUID, bossUUID);
			wrapper.write(Type.VAR_INT, 2); // Action - update health
			wrapper.write(Type.FLOAT, (float) progress); // Health
		}
		wrapper.scheduleSend(Protocol1_8To1_9.class, false);
	}

	@Override
	public void hide() throws Exception {
		if (bossUUID == null) {
			return;
		}
		PacketWrapper wrapper = PacketWrapper.create(ClientboundPackets1_9.BOSSBAR, null, user);
		wrapper.write(Type.UUID, bossUUID);
		wrapper.write(Type.VAR_INT, 1); // Action - remove
		wrapper.scheduleSend(Protocol1_8To1_9.class, false);
		bossUUID = null;
	}
}
