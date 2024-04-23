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

package com.viaversion.viarewind.protocol.protocol1_8to1_9.packets;

import com.viaversion.viarewind.protocol.protocol1_8to1_9.Protocol1_8To1_9;
import com.viaversion.viarewind.protocol.protocol1_8to1_9.storage.CooldownStorage;
import com.viaversion.viarewind.protocol.protocol1_8to1_9.storage.EntityTracker1_9;
import com.viaversion.viarewind.protocol.protocol1_8to1_9.storage.LevitationStorage;
import com.viaversion.viarewind.protocol.protocol1_8to1_9.storage.PlayerPositionTracker;
import com.viaversion.viarewind.utils.math.RelativeMoveUtil;
import com.viaversion.viaversion.api.minecraft.Vector;
import com.viaversion.viaversion.api.minecraft.entities.EntityType;
import com.viaversion.viaversion.api.minecraft.entities.EntityTypes1_10;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.protocol.remapper.PacketHandlers;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.libs.fastutil.ints.IntArrayList;
import com.viaversion.viaversion.libs.fastutil.ints.IntList;
import com.viaversion.viaversion.protocols.protocol1_8.ClientboundPackets1_8;
import com.viaversion.viaversion.protocols.protocol1_9to1_8.ClientboundPackets1_9;
import com.viaversion.viaversion.util.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class EntityPackets1_9 {

	public static void register(final Protocol1_8To1_9 protocol) {
		protocol.registerClientbound(ClientboundPackets1_9.ENTITY_STATUS, new PacketHandlers() {
			@Override
			public void register() {
				map(Type.INT); // Entity id
				handler(wrapper -> {
					final byte status = wrapper.read(Type.BYTE);
					if (status > 23) { // Remove op permission level 0-4 (24-28)
						wrapper.cancel();
						return;
					}
					wrapper.write(Type.BYTE, status);
				});
			}
		});

		protocol.registerClientbound(ClientboundPackets1_9.ENTITY_POSITION, wrapper -> {
			final int entityId = wrapper.passthrough(Type.VAR_INT);
			final int deltaX = wrapper.read(Type.SHORT);
			final int deltaY = wrapper.read(Type.SHORT);
			final int deltaZ = wrapper.read(Type.SHORT);

			final Vector[] moves = RelativeMoveUtil.calculateRelativeMoves(wrapper.user(), entityId, deltaX, deltaY, deltaZ);

			wrapper.write(Type.BYTE, (byte) moves[0].blockX());
			wrapper.write(Type.BYTE, (byte) moves[0].blockY());
			wrapper.write(Type.BYTE, (byte) moves[0].blockZ());

			final boolean onGround = wrapper.passthrough(Type.BOOLEAN);

			if (moves.length > 1) {
				final PacketWrapper secondPacket = PacketWrapper.create(ClientboundPackets1_8.ENTITY_POSITION, wrapper.user());
				secondPacket.write(Type.VAR_INT, entityId);
				secondPacket.write(Type.BYTE, (byte) moves[1].blockX());
				secondPacket.write(Type.BYTE, (byte) moves[1].blockY());
				secondPacket.write(Type.BYTE, (byte) moves[1].blockZ());
				secondPacket.write(Type.BOOLEAN, onGround);

				secondPacket.scheduleSend(Protocol1_8To1_9.class);
			}
		});

		protocol.registerClientbound(ClientboundPackets1_9.ENTITY_POSITION_AND_ROTATION, wrapper -> {
			final int entityId = wrapper.passthrough(Type.VAR_INT);
			final int deltaX = wrapper.read(Type.SHORT);
			final int deltaY = wrapper.read(Type.SHORT);
			final int deltaZ = wrapper.read(Type.SHORT);

			final Vector[] moves = RelativeMoveUtil.calculateRelativeMoves(wrapper.user(), entityId, deltaX, deltaY, deltaZ);

			wrapper.write(Type.BYTE, (byte) moves[0].blockX());
			wrapper.write(Type.BYTE, (byte) moves[0].blockY());
			wrapper.write(Type.BYTE, (byte) moves[0].blockZ());

			byte yaw = wrapper.passthrough(Type.BYTE);
			final byte pitch = wrapper.passthrough(Type.BYTE);
			final boolean onGround = wrapper.passthrough(Type.BOOLEAN);

			EntityType type = wrapper.user().getEntityTracker(Protocol1_8To1_9.class).entityType(entityId);
			if (type == EntityTypes1_10.EntityType.BOAT) {
				yaw -= 64;
				wrapper.set(Type.BYTE, 3, yaw);
			}

			if (moves.length > 1) {
				final PacketWrapper secondPacket = PacketWrapper.create(ClientboundPackets1_8.ENTITY_POSITION_AND_ROTATION, wrapper.user());
				secondPacket.write(Type.VAR_INT, entityId);
				secondPacket.write(Type.BYTE, (byte) moves[1].blockX());
				secondPacket.write(Type.BYTE, (byte) moves[1].blockY());
				secondPacket.write(Type.BYTE, (byte) moves[1].blockZ());
				secondPacket.write(Type.BYTE, yaw);
				secondPacket.write(Type.BYTE, pitch);
				secondPacket.write(Type.BOOLEAN, onGround);

				secondPacket.scheduleSend(Protocol1_8To1_9.class);
			}
		});

		protocol.registerClientbound(ClientboundPackets1_9.ENTITY_ROTATION, wrapper -> {
			final int entityId = wrapper.passthrough(Type.VAR_INT);
			final EntityType type = wrapper.user().getEntityTracker(Protocol1_8To1_9.class).entityType(entityId);
			if (type == EntityTypes1_10.EntityType.BOAT) {
				byte yaw = wrapper.read(Type.BYTE);
				yaw -= 64;
				wrapper.write(Type.BYTE, yaw);
			}
		});

		protocol.registerClientbound(ClientboundPackets1_9.VEHICLE_MOVE, ClientboundPackets1_8.ENTITY_TELEPORT, new PacketHandlers() {
			@Override
			public void register() {
				handler(wrapper -> {
					final EntityTracker1_9 tracker = wrapper.user().getEntityTracker(Protocol1_8To1_9.class);
					final int vehicle = tracker.getVehicle(tracker.clientEntityId());
					if (vehicle == -1) {
						wrapper.cancel();
					}
					wrapper.write(Type.VAR_INT, vehicle);
				});
				map(Type.DOUBLE, Protocol1_8To1_9.TO_OLD_INT); // X
				map(Type.DOUBLE, Protocol1_8To1_9.TO_OLD_INT); // Y
				map(Type.DOUBLE, Protocol1_8To1_9.TO_OLD_INT); // Z
				map(Type.FLOAT, Protocol1_8To1_9.DEGREES_TO_ANGLE); // Yaw
				map(Type.FLOAT, Protocol1_8To1_9.DEGREES_TO_ANGLE); // Pitch
				handler(wrapper -> {
					if (wrapper.isCancelled()) return;
					final PlayerPositionTracker position = wrapper.user().get(PlayerPositionTracker.class);
					double x = wrapper.get(Type.INT, 0) / 32d;
					double y = wrapper.get(Type.INT, 1) / 32d;
					double z = wrapper.get(Type.INT, 2) / 32d;
					position.setPos(x, y, z);
				});
				create(Type.BOOLEAN, true);
				handler(wrapper -> {
					final int entityId = wrapper.get(Type.VAR_INT, 0);
					final EntityType type = wrapper.user().getEntityTracker(Protocol1_8To1_9.class).entityType(entityId);
					if (type == EntityTypes1_10.EntityType.BOAT) {
						byte yaw = wrapper.get(Type.BYTE, 1);
						yaw -= 64;
						wrapper.set(Type.BYTE, 0, yaw);
						int y = wrapper.get(Type.INT, 1);
						y += 10;
						wrapper.set(Type.INT, 1, y);
					}
				});
			}
		});

		protocol.registerClientbound(ClientboundPackets1_9.REMOVE_ENTITY_EFFECT, wrapper -> {
			final int entityId = wrapper.passthrough(Type.VAR_INT);
			final int effectId = wrapper.passthrough(Type.BYTE);
			if (effectId > 23) { // Throw away new effects
				wrapper.cancel();
			}
			final EntityTracker1_9 tracker = wrapper.user().getEntityTracker(Protocol1_8To1_9.class);
			if (effectId == 25 && entityId == tracker.clientEntityId()) {
				wrapper.user().get(LevitationStorage.class).setActive(false);
			}
		});

		protocol.registerClientbound(ClientboundPackets1_9.ATTACH_ENTITY, new PacketHandlers() {
			@Override
			public void register() {
				map(Type.INT); // Attached entity id
				map(Type.INT); // Holding entity id
				create(Type.BOOLEAN, true); // Leash
			}
		});

		protocol.registerClientbound(ClientboundPackets1_9.ENTITY_EQUIPMENT, new PacketHandlers() {
			@Override
			public void register() {
				map(Type.VAR_INT); // Entity id
				handler(wrapper -> {
					// todo check if this is correct for the own player
					int slot = wrapper.read(Type.VAR_INT);
					if (slot == 1) {
						wrapper.cancel();
					} else if (slot > 1) {
						slot -= 1;
					}
					wrapper.write(Type.SHORT, (short) slot);
				});
				map(Type.ITEM1_8); // Item
				handler(wrapper -> protocol.getItemRewriter().handleItemToClient(wrapper.user(), wrapper.get(Type.ITEM1_8, 0)));
			}
		});

		protocol.registerClientbound(ClientboundPackets1_9.SET_PASSENGERS, null, wrapper -> {
			wrapper.cancel();

			final EntityTracker1_9 tracker = wrapper.user().getEntityTracker(Protocol1_8To1_9.class);
			final int vehicle = wrapper.read(Type.VAR_INT);
			final IntList oldPassengers = tracker.getPassengers(vehicle);

			final int count = wrapper.read(Type.VAR_INT);
			final IntList passengers = new IntArrayList();
			for (int i = 0; i < count; i++) {
				passengers.add(wrapper.read(Type.VAR_INT));
			}
			tracker.setPassengers(vehicle, passengers);

			if (!oldPassengers.isEmpty()) {
				for (Integer passenger : oldPassengers) {
					final PacketWrapper detach = PacketWrapper.create(ClientboundPackets1_8.ATTACH_ENTITY, wrapper.user());
					detach.write(Type.INT, passenger); // Attached entity id
					detach.write(Type.INT, -1); // Holding entity id
					detach.write(Type.BOOLEAN, false); // Leash
					detach.scheduleSend(Protocol1_8To1_9.class);
				}
			}
			for (int i = 0; i < count; i++) {
				final int attachedEntityId = passengers.getInt(i);
				final int holdingEntityId = i == 0 ? vehicle : passengers.getInt(i - 1);

				final PacketWrapper attach = PacketWrapper.create(ClientboundPackets1_8.ATTACH_ENTITY, wrapper.user());
				attach.write(Type.INT, attachedEntityId);
				attach.write(Type.INT, holdingEntityId);
				attach.write(Type.BOOLEAN, false); // Leash
				attach.scheduleSend(Protocol1_8To1_9.class);
			}
		});

		protocol.registerClientbound(ClientboundPackets1_9.ENTITY_TELEPORT, new PacketHandlers() {
			@Override
			public void register() {
				map(Type.VAR_INT); // Entity id
				map(Type.DOUBLE, Protocol1_8To1_9.TO_OLD_INT); // X
				map(Type.DOUBLE, Protocol1_8To1_9.TO_OLD_INT); // Y
				map(Type.DOUBLE, Protocol1_8To1_9.TO_OLD_INT); // Z
				map(Type.BYTE); // Yaw
				map(Type.BYTE); // Pitch
				map(Type.BOOLEAN); // On ground
				handler(wrapper -> {
					final int entityId = wrapper.get(Type.VAR_INT, 0);

					final EntityTracker1_9 tracker = wrapper.user().getEntityTracker(Protocol1_8To1_9.class);
					if (tracker.entityType(entityId) == EntityTypes1_10.EntityType.BOAT) {
						byte yaw = wrapper.get(Type.BYTE, 1);
						yaw -= 64;
						wrapper.set(Type.BYTE, 0, yaw);

						int y = wrapper.get(Type.INT, 1);
						y += 10;
						wrapper.set(Type.INT, 1, y);
					}
					tracker.resetEntityOffset(entityId);
				});
			}
		});

		protocol.registerClientbound(ClientboundPackets1_9.ENTITY_PROPERTIES, wrapper -> {
			final int entityId = wrapper.passthrough(Type.VAR_INT);

			final EntityTracker1_9 tracker = wrapper.user().getEntityTracker(Protocol1_8To1_9.class);
			final boolean player = entityId == tracker.clientEntityId();

			// Throw away new attributes and track attack speed
			int removed = 0;
			final int size = wrapper.passthrough(Type.INT);
			for (int i = 0; i < size; i++) {
				final String key = wrapper.read(Type.STRING);
				final double value = wrapper.read(Type.DOUBLE);
				final int modifierSize = wrapper.read(Type.VAR_INT);

				final boolean valid = protocol.getItemRewriter().VALID_ATTRIBUTES.contains(key);
				if (valid) {
					wrapper.write(Type.STRING, key);
					wrapper.write(Type.DOUBLE, value);
					wrapper.write(Type.VAR_INT, modifierSize);
				}

				final List<Pair<Byte, Double>> modifiers = new ArrayList<>();
				for (int j = 0; j < modifierSize; j++) {
					final UUID modifierId = wrapper.read(Type.UUID); // UUID
					final double amount = wrapper.read(Type.DOUBLE); // Amount
					final byte operation = wrapper.read(Type.BYTE); // Operation
					if (valid) {
						wrapper.write(Type.UUID, modifierId);
						wrapper.write(Type.DOUBLE, amount);
						wrapper.write(Type.BYTE, operation);
					}
					modifiers.add(new Pair<>(operation, amount));
				}
				if (!valid) {
					if (player && key.equals("generic.attackSpeed")) {
						wrapper.user().get(CooldownStorage.class).setAttackSpeed(value, modifiers);
					}
					removed++;
				}
			}
			wrapper.set(Type.INT, 0, size - removed);
		});

		protocol.registerClientbound(ClientboundPackets1_9.ENTITY_EFFECT, wrapper -> {
			final int entityId = wrapper.passthrough(Type.VAR_INT);
			final int effectId = wrapper.passthrough(Type.BYTE);
			final byte amplifier = wrapper.passthrough(Type.BYTE);
			if (effectId > 23) { // Throw away new effects
				wrapper.cancel();
			}
			final EntityTracker1_9 tracker = wrapper.user().getEntityTracker(Protocol1_8To1_9.class);
			if (effectId == 25 && entityId == tracker.clientEntityId()) {
				final LevitationStorage levitation = wrapper.user().get(LevitationStorage.class);
				levitation.setActive(true);
				levitation.setAmplifier(amplifier);
			}
		});
	}
}
