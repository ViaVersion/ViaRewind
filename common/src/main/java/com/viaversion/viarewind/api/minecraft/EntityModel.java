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

package com.viaversion.viarewind.api.minecraft;

import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.minecraft.metadata.Metadata;
import com.viaversion.viaversion.api.protocol.Protocol;

import java.util.List;

@Deprecated
public abstract class EntityModel<T extends Protocol<?, ?, ?, ?>> {

	protected final UserConnection user;
	protected final T protocol;

	public EntityModel(final UserConnection user, final T protocol) {
		this.user = user;
		this.protocol = protocol;
	}

	/**
	 * @return The entity id of the entity this model is replacing
	 */
	public abstract int getEntityId();

	/**
	 * Updates the position of the entity this model is replacing
	 *
	 * @param x The new x position
	 * @param y The new y position
	 * @param z The new z position
	 */
	public abstract void updateReplacementPosition(final double x, final double y, final double z);

	/**
	 * Handles the original movement packet of the entity this model is replacing
	 *
	 * @param x The x position
	 * @param y The y position
	 * @param z The z position
	 */
	public abstract void handleOriginalMovementPacket(final double x, final double y, final double z);

	/**
	 * Sets the yaw and pitch of the entity this model is replacing
	 *
	 * @param yaw   The new yaw
	 * @param pitch The new pitch
	 */
	public abstract void setYawPitch(final float yaw, final float pitch);

	/**
	 * Sets the head yaw of the entity this model is replacing
	 *
	 * @param yaw The new head yaw
	 */
	public abstract void setHeadYaw(final float yaw);

	/**
	 * Spawns the entity this model is replacing
	 */
	public abstract void sendSpawnPacket();

	/**
	 * Destroys the entity this model is replacing
	 */
	public abstract void deleteEntity();

	/**
	 * Updates the metadata of the entity this model is replacing
	 *
	 * @param metadataList The new metadata
	 */
	public abstract void updateMetadata(final List<Metadata> metadataList);

	public UserConnection getUser() {
		return user;
	}

	public T getProtocol() {
		return protocol;
	}
}
