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
package com.viaversion.viarewind.protocol.v1_9to1_8.data;

import com.viaversion.viarewind.protocol.v1_7_6_10to1_7_2_5.packet.ClientboundPackets1_7_2_5;
import com.viaversion.viarewind.protocol.v1_9to1_8.Protocol1_9To1_8;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.legacy.bossbar.BossBar;
import com.viaversion.viaversion.api.legacy.bossbar.BossColor;
import com.viaversion.viaversion.api.legacy.bossbar.BossFlag;
import com.viaversion.viaversion.api.legacy.bossbar.BossStyle;
import com.viaversion.viaversion.api.minecraft.entitydata.EntityData;
import com.viaversion.viaversion.api.minecraft.entitydata.types.EntityDataTypes1_8;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.api.type.types.version.Types1_8;

import java.util.*;

public class WitherBossBar implements BossBar {

	private static int highestId = Integer.MAX_VALUE - 10000;

	private final UUID uuid;
	private String title;
	private float health;
	private boolean visible = false;

	private final UserConnection connection;

	private final int entityId = highestId++;
	private double locX, locY, locZ;

	public WitherBossBar(UserConnection connection, UUID uuid, String title, float health) {
		this.connection = connection;
		this.uuid = uuid;
		this.title = title;
		this.health = health;
	}

	@Override
	public String getTitle() {
		return title;
	}

	@Override
	public BossBar setTitle(String title) {
		this.title = title;
		if (this.visible) {
			updateEntityData();
		}
		return this;
	}

	@Override
	public float getHealth() {
		return health;
	}

	@Override
	public BossBar setHealth(float health) {
		this.health = health;
		if (this.health <= 0) {
			this.health = 0.0001f;
		}
		if (this.visible) {
			updateEntityData();
		}
		return this;
	}

	@Override
	public BossColor getColor() {
		return null;
	}

	@Override
	public BossBar setColor(BossColor bossColor) {
		throw new UnsupportedOperationException(this.getClass().getName() + " does not support color");
	}

	@Override
	public BossStyle getStyle() {
		return null;
	}

	@Override
	public BossBar setStyle(BossStyle bossStyle) {
		throw new UnsupportedOperationException(this.getClass().getName() + " does not support styles");
	}

	@Override
	public BossBar addPlayer(UUID uuid) {
		throw new UnsupportedOperationException(this.getClass().getName() + " is only for one UserConnection!");
	}

	@Override
	public BossBar addConnection(UserConnection userConnection) {
		throw new UnsupportedOperationException(this.getClass().getName() + " is only for one UserConnection!");
	}

	@Override
	public BossBar removePlayer(UUID uuid) {
		throw new UnsupportedOperationException(this.getClass().getName() + " is only for one UserConnection!");
	}

	@Override
	public BossBar removeConnection(UserConnection userConnection) {
		throw new UnsupportedOperationException(this.getClass().getName() + " is only for one UserConnection!");
	}

	@Override
	public BossBar addFlag(BossFlag bossFlag) {
		throw new UnsupportedOperationException(this.getClass().getName() + " does not support flags");
	}

	@Override
	public BossBar removeFlag(BossFlag bossFlag) {
		throw new UnsupportedOperationException(this.getClass().getName() + " does not support flags");
	}

	@Override
	public boolean hasFlag(BossFlag bossFlag) {
		return false;
	}

	@Override
	public Set<UUID> getPlayers() {
		return Collections.singleton(connection.getProtocolInfo().getUuid());
	}

	@Override
	public Set<UserConnection> getConnections() {
		throw new UnsupportedOperationException(this.getClass().getName() + " is only for one UserConnection!");
	}

	@Override
	public BossBar show() {
		if (!this.visible) {
			addWither();
			this.visible = true;
		}
		return this;
	}

	@Override
	public BossBar hide() {
		if (this.visible) {
			removeWither();
			this.visible = false;
		}
		return this;
	}

	@Override
	public boolean isVisible() {
		return visible;
	}

	@Override
	public UUID getId() {
		return this.uuid;
	}

	public void setLocation(double x, double y, double z) {
		locX = x;
		locY = y;
		locZ = z;
		updateLocation();
	}

	private void addWither() {
		final PacketWrapper addMob = PacketWrapper.create(ClientboundPackets1_7_2_5.ADD_MOB, this.connection);
		addMob.write(Types.VAR_INT, entityId);
		addMob.write(Types.UNSIGNED_BYTE, (short) 64);
		addMob.write(Types.INT, (int) (locX * 32D));
		addMob.write(Types.INT, (int) (locY * 32D));
		addMob.write(Types.INT, (int) (locZ * 32D));
		addMob.write(Types.BYTE, (byte) 0);
		addMob.write(Types.BYTE, (byte) 0);
		addMob.write(Types.BYTE, (byte) 0);
		addMob.write(Types.SHORT, (short) 0);
		addMob.write(Types.SHORT, (short) 0);
		addMob.write(Types.SHORT, (short) 0);

		final List<EntityData> entityData = new ArrayList<>();
		entityData.add(new EntityData(0, EntityDataTypes1_8.BYTE, (byte) 0x20));
		entityData.add(new EntityData(2, EntityDataTypes1_8.STRING, title));
		entityData.add(new EntityData(3, EntityDataTypes1_8.BYTE, (byte) 1));
		entityData.add(new EntityData(6, EntityDataTypes1_8.FLOAT, health * 300f));

		addMob.write(Types1_8.ENTITY_DATA_LIST, entityData);
		addMob.scheduleSend(Protocol1_9To1_8.class);
	}

	private void updateLocation() {
		final PacketWrapper teleportEntity = PacketWrapper.create(ClientboundPackets1_7_2_5.TELEPORT_ENTITY, this.connection);
		teleportEntity.write(Types.VAR_INT, entityId);
		teleportEntity.write(Types.INT, (int) (locX * 32D));
		teleportEntity.write(Types.INT, (int) (locY * 32D));
		teleportEntity.write(Types.INT, (int) (locZ * 32D));
		teleportEntity.write(Types.BYTE, (byte) 0);
		teleportEntity.write(Types.BYTE, (byte) 0);
		teleportEntity.write(Types.BOOLEAN, false);
		teleportEntity.scheduleSend(Protocol1_9To1_8.class);
	}

	private void updateEntityData() {
		final PacketWrapper setEntityData = PacketWrapper.create(ClientboundPackets1_7_2_5.SET_ENTITY_DATA, this.connection);
		setEntityData.write(Types.VAR_INT, entityId);

		final List<EntityData> entityData = new ArrayList<>();
		entityData.add(new EntityData(2, EntityDataTypes1_8.STRING, title));
		entityData.add(new EntityData(6, EntityDataTypes1_8.FLOAT, health * 300f));

		setEntityData.write(Types1_8.ENTITY_DATA_LIST, entityData);
		setEntityData.scheduleSend(Protocol1_9To1_8.class);
	}

	private void removeWither() {
		final PacketWrapper removeEntity = PacketWrapper.create(ClientboundPackets1_7_2_5.REMOVE_ENTITIES, this.connection);
		removeEntity.write(Types.VAR_INT_ARRAY_PRIMITIVE, new int[]{entityId});
		removeEntity.scheduleSend(Protocol1_9To1_8.class);
	}

	public void setPlayerLocation(double posX, double posY, double posZ, float yaw, float pitch) {
		double yawR = Math.toRadians(yaw);
		double pitchR = Math.toRadians(pitch);

		posX -= Math.cos(pitchR) * Math.sin(yawR) * 48;
		posY -= Math.sin(pitchR) * 48;
		posZ += Math.cos(pitchR) * Math.cos(yawR) * 48;

		setLocation(posX, posY, posZ);
	}

}
