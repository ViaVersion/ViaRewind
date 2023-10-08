/*
 * This file is part of ViaRewind - https://github.com/ViaVersion/ViaRewind
 * Copyright (C) 2016-2023 ViaVersion and contributors
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

package com.viaversion.viarewind.protocol.protocol1_7_6_10to1_8.emulator;

import com.viaversion.viarewind.protocol.protocol1_7_2_5to1_7_6_10.ClientboundPackets1_7_2_5;
import com.viaversion.viarewind.protocol.protocol1_7_6_10to1_8.Protocol1_7_6_10To1_8;
import com.viaversion.viarewind.protocol.protocol1_7_6_10to1_8.types.Types1_7_6_10;
import com.viaversion.viarewind.api.minecraft.EntityModel;
import com.viaversion.viarewind.utils.PacketUtil;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.type.Type;

import java.util.ArrayList;

public abstract class EntityModel1_7_6_10 extends EntityModel<Protocol1_7_6_10To1_8> {

	public EntityModel1_7_6_10(UserConnection user, Protocol1_7_6_10To1_8 protocol) {
		super(user, protocol);
	}

	protected void teleportAndUpdate(final int entityId, final double x, final double y, final double z, final float yaw, final float pitch, final float headYaw) {
		teleportEntity(entityId, x, y, z, yaw, pitch);
		updateHeadYaw(entityId, headYaw);
	}

	protected void teleportEntity(final int entityId, final double x, final double y, final double z, final float yaw, final float pitch) {
		final PacketWrapper entityTeleport = PacketWrapper.create(ClientboundPackets1_7_2_5.ENTITY_TELEPORT, user);

		entityTeleport.write(Type.INT, entityId); // entity id
		entityTeleport.write(Type.INT, (int) (x * 32.0)); // x
		entityTeleport.write(Type.INT, (int) (y * 32.0)); // y
		entityTeleport.write(Type.INT, (int) (z * 32.0)); // z
		entityTeleport.write(Type.BYTE, (byte) ((yaw / 360f) * 256)); // yaw
		entityTeleport.write(Type.BYTE, (byte) ((pitch / 360f) * 256)); // pitch

		PacketUtil.sendPacket(entityTeleport, Protocol1_7_6_10To1_8.class, true, true);
	}

	protected void updateHeadYaw(final int entityId, final float headYaw) {
		final PacketWrapper entityHeadLook = PacketWrapper.create(ClientboundPackets1_7_2_5.ENTITY_HEAD_LOOK, user);

		entityHeadLook.write(Type.INT, entityId);
		entityHeadLook.write(Type.BYTE, (byte) ((headYaw / 360f) * 256));

		PacketUtil.sendPacket(entityHeadLook, Protocol1_7_6_10To1_8.class, true, true);
	}

	protected void spawnEntity(final int entityId, final int type, final double locX, final double locY, final double locZ) {
		final PacketWrapper spawnMob = PacketWrapper.create(ClientboundPackets1_7_2_5.SPAWN_MOB, null, user);

		spawnMob.write(Type.VAR_INT, entityId); // entity id
		spawnMob.write(Type.UNSIGNED_BYTE, (short) type); // type
		spawnMob.write(Type.INT, (int) (locX * 32.0)); // x
		spawnMob.write(Type.INT, (int) (locY * 32.0)); // y
		spawnMob.write(Type.INT, (int) (locZ * 32.0)); // z
		spawnMob.write(Type.BYTE, (byte) 0); // yaw
		spawnMob.write(Type.BYTE, (byte) 0); // pitch
		spawnMob.write(Type.BYTE, (byte) 0); // head pitch
		spawnMob.write(Type.SHORT, (short) 0); // velocity x
		spawnMob.write(Type.SHORT, (short) 0); // velocity y
		spawnMob.write(Type.SHORT, (short) 0); // velocity z
		spawnMob.write(Types1_7_6_10.METADATA_LIST, new ArrayList<>()); // metadata

		PacketUtil.sendPacket(spawnMob, Protocol1_7_6_10To1_8.class, true, true);
	}
}
