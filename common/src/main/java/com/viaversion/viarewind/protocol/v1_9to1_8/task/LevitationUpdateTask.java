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
package com.viaversion.viarewind.protocol.v1_9to1_8.task;

import com.viaversion.viarewind.ViaRewind;
import com.viaversion.viarewind.protocol.v1_9to1_8.Protocol1_9To1_8;
import com.viaversion.viarewind.protocol.v1_9to1_8.storage.LevitationStorage;
import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.protocols.v1_8to1_9.packet.ClientboundPackets1_8;

import java.util.logging.Level;

public class LevitationUpdateTask implements Runnable {

	@Override
	public void run() {
		if (!ViaRewind.getConfig().emulateLevitationEffect()) {
			return;
		}
		for (UserConnection connection : Via.getManager().getConnectionManager().getConnections()) {
			final LevitationStorage levitation = connection.get(LevitationStorage.class);
			if (!levitation.isActive()) {
				continue;
			}

			final PacketWrapper velocityPacket = PacketWrapper.create(ClientboundPackets1_8.SET_ENTITY_MOTION, connection);
			velocityPacket.write(Types.VAR_INT, connection.getEntityTracker(Protocol1_9To1_8.class).clientEntityId());
			velocityPacket.write(Types.SHORT, (short) 0);
			velocityPacket.write(Types.SHORT, (short) ((levitation.getAmplifier() + 1) * 360));
			velocityPacket.write(Types.SHORT, (short) 0);
			try {
				velocityPacket.scheduleSend(Protocol1_9To1_8.class);
			} catch (Exception e) {
				ViaRewind.getPlatform().getLogger().log(Level.SEVERE, "Failed to send levitation packet", e);
			}
		}
	}
}
