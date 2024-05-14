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
package com.viaversion.viarewind.protocol.v1_8to1_7_6_10.task;

import com.viaversion.viarewind.ViaRewind;
import com.viaversion.viarewind.protocol.v1_8to1_7_6_10.Protocol1_8To1_7_6_10;
import com.viaversion.viarewind.protocol.v1_8to1_7_6_10.storage.PlayerSessionStorage;
import com.viaversion.viarewind.protocol.v1_8to1_7_6_10.storage.WorldBorderEmulator;
import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.protocols.v1_8to1_9.packet.ClientboundPackets1_8;

import java.util.logging.Level;

public class WorldBorderUpdateTask implements Runnable {
	public static final int VIEW_DISTANCE = 16;

	@Override
	public void run() {
		for (UserConnection connection : Via.getManager().getConnectionManager().getConnections()) {
			final WorldBorderEmulator worldBorderEmulatorTracker = connection.get(WorldBorderEmulator.class);
			if (!worldBorderEmulatorTracker.isInit()) continue;

			final PlayerSessionStorage playerSession = connection.get(PlayerSessionStorage.class);

			double radius = worldBorderEmulatorTracker.getSize() / 2.0;

			for (WorldBorderEmulator.Side side : WorldBorderEmulator.Side.values()) {
				double d;
				double pos;
				double center;
				if (side.modX != 0) {
					pos = playerSession.getPosZ();
					center = worldBorderEmulatorTracker.getZ();
					d = Math.abs(worldBorderEmulatorTracker.getX() + radius * side.modX - playerSession.getPosX());
				} else {
					center = worldBorderEmulatorTracker.getX();
					pos = playerSession.getPosX();
					d = Math.abs(worldBorderEmulatorTracker.getZ() + radius * side.modZ - playerSession.getPosZ());
				}
				if (d >= VIEW_DISTANCE) continue;

				double r = Math.sqrt(VIEW_DISTANCE * VIEW_DISTANCE - d * d);

				double minH = Math.ceil(pos - r);
				double maxH = Math.floor(pos + r);
				double minV = Math.ceil(playerSession.getPosY() - r);
				double maxV = Math.floor(playerSession.getPosY() + r);

				if (minH < center - radius) minH = Math.ceil(center - radius);
				if (maxH > center + radius) maxH = Math.floor(center + radius);
				if (minV < 0.0) minV = 0.0;

				double centerH = (minH + maxH) / 2.0;
				double centerV = (minV + maxV) / 2.0;

				double particleOffset = 2.5;

				final PacketWrapper spawnParticle = PacketWrapper.create(ClientboundPackets1_8.LEVEL_PARTICLES, connection);
				spawnParticle.write(Types.STRING, ViaRewind.getConfig().getWorldBorderParticle()); // particle name
				spawnParticle.write(Types.FLOAT, (float) (side.modX != 0 ? worldBorderEmulatorTracker.getX() + (radius * side.modX) : centerH)); // x
				spawnParticle.write(Types.FLOAT, (float) centerV); // y
				spawnParticle.write(Types.FLOAT, (float) (side.modX == 0 ? worldBorderEmulatorTracker.getZ() + (radius * side.modZ) : centerH)); // z
				spawnParticle.write(Types.FLOAT, (float) (side.modX != 0 ? 0f : (maxH - minH) / particleOffset)); // offset x
				spawnParticle.write(Types.FLOAT, (float) ((maxV - minV) / particleOffset)); // offset y
				spawnParticle.write(Types.FLOAT, (float) (side.modX == 0 ? 0f : (maxH - minH) / particleOffset)); // offset z
				spawnParticle.write(Types.FLOAT, 0F); // particle data
				spawnParticle.write(Types.INT, (int) Math.floor((maxH - minH) * (maxV - minV) * 0.5));

				try {
					spawnParticle.send(Protocol1_8To1_7_6_10.class);
				} catch (Exception e) {
					ViaRewind.getPlatform().getLogger().log(Level.SEVERE, "Failed to send world border particle", e);
				}
			}
		}
	}
}
