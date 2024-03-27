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

package com.viaversion.viarewind.api.rewriter;

import com.viaversion.viaversion.api.connection.StoredObject;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.data.entity.ClientEntityIdChangeListener;
import com.viaversion.viaversion.api.minecraft.entities.EntityType;
import com.viaversion.viaversion.api.minecraft.entities.EntityTypes1_10;
import com.viaversion.viaversion.api.minecraft.metadata.MetaType;
import com.viaversion.viaversion.api.minecraft.metadata.Metadata;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import com.viaversion.viaversion.util.Pair;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Deprecated
public abstract class ReplacementEntityTracker extends StoredObject implements ClientEntityIdChangeListener {
	private final Map<EntityType, Pair<EntityType, String>> ENTITY_REPLACEMENTS = new HashMap<>();

	private final Map<Integer, EntityType> entityMap = new HashMap<>();
	private final Map<Integer, EntityType> entityReplacementMap = new HashMap<>();

	private int playerId = -1;

	private final ProtocolVersion version;

	private final int displayNameVisibilityIndex;
	private final MetaType displayNameVisibilityType;
	private final int displayNameIndex;
	private final MetaType displayNameType;

	public ReplacementEntityTracker(UserConnection user, ProtocolVersion version, MetaType displayNameVisibilityType, MetaType displayNameType) {
		this(user, version, 3, displayNameVisibilityType, 2, displayNameType);
	}

	public ReplacementEntityTracker(UserConnection user, ProtocolVersion version, int displayNameVisibilityIndex, MetaType displayNameVisibilityType, int displayNameIndex, MetaType displayNameType) {
		super(user);
		this.version = version;
		this.displayNameVisibilityIndex = displayNameVisibilityIndex;
		this.displayNameVisibilityType = displayNameVisibilityType;
		this.displayNameIndex = displayNameIndex;
		this.displayNameType = displayNameType;
	}

	public void registerEntity(final EntityType oldType, final EntityType newType, final String name) {
		ENTITY_REPLACEMENTS.put(oldType, new Pair<>(newType, this.version.getName() + " " + name));
	}

	public void addEntity(final int entityId, final EntityTypes1_10.EntityType type) {
		entityMap.put(entityId, type);
	}

	public int replaceEntity(final int entityId, final EntityTypes1_10.EntityType type) {
		entityReplacementMap.put(entityId, type);

		return ENTITY_REPLACEMENTS.get(type).key().getId();
	}

	public void removeEntity(final int entityId) {
		entityMap.remove(entityId);
		entityReplacementMap.remove(entityId);
	}

	public void clear() {
		entityMap.clear();
		entityReplacementMap.clear();
	}

	public boolean isReplaced(final EntityTypes1_10.EntityType type) {
		return ENTITY_REPLACEMENTS.containsKey(type);
	}

	public void updateMetadata(final int entityId, final List<Metadata> metadata) throws Exception {
		final String name = ENTITY_REPLACEMENTS.get(entityMap.get(entityId)).value();
		metadata.add(new Metadata(this.displayNameVisibilityIndex, this.displayNameVisibilityType, (byte) 1));
		metadata.add(new Metadata(this.displayNameIndex, this.displayNameType, name));
	}

	@Override
	public void setClientEntityId(int entityId) {
		removeEntity(this.playerId);
		addEntity(entityId, EntityTypes1_10.EntityType.ENTITY_HUMAN);

		this.playerId = entityId;
	}

	public Map<Integer, EntityType> getEntityMap() {
		return entityMap;
	}

	public Map<Integer, EntityType> getEntityReplacementMap() {
		return entityReplacementMap;
	}

	public int getPlayerId() {
		return playerId;
	}
}
