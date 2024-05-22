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
package com.viaversion.viarewind.protocol.v1_8to1_7_6_10.data;

import com.viaversion.viarewind.api.minecraft.entitydata.EntityDataTypes1_7_6_10;
import com.viaversion.viaversion.api.minecraft.entities.EntityType;
import com.viaversion.viaversion.api.minecraft.entities.EntityTypes1_8;
import com.viaversion.viaversion.api.minecraft.entitydata.types.EntityDataTypes1_8;
import com.viaversion.viaversion.util.Pair;

import java.util.HashMap;
import java.util.Optional;

import static com.viaversion.viaversion.api.minecraft.entities.EntityTypes1_8.EntityType.*;

public enum EntityDataIndex1_7_6_10 {

	ENTITY_FLAGS(ENTITY, 0, EntityDataTypes1_7_6_10.BYTE, EntityDataTypes1_8.BYTE),
	ENTITY_AIR(ENTITY, 1, EntityDataTypes1_7_6_10.SHORT, EntityDataTypes1_8.SHORT),
	ENTITY_NAME_TAG(ENTITY, -1, null, 2, EntityDataTypes1_8.STRING),
	ENTITY_NAME_TAG_VISIBILITY(ENTITY, -1, null, 3, EntityDataTypes1_8.BYTE),
	ENTITY_SILENT(ENTITY, -1, null, 4, EntityDataTypes1_8.BYTE),

	LIVING_ENTITY_BASE_HEALTH(LIVING_ENTITY_BASE, 6, EntityDataTypes1_7_6_10.FLOAT, EntityDataTypes1_8.FLOAT),
	LIVING_ENTITY_BASE_POTION_EFFECT_COLOR(LIVING_ENTITY_BASE, 7, EntityDataTypes1_7_6_10.INT, EntityDataTypes1_8.INT),
	LIVING_ENTITY_BASE_IS_POTION_EFFECT_AMBIENT(LIVING_ENTITY_BASE, 8, EntityDataTypes1_7_6_10.BYTE, EntityDataTypes1_8.BYTE),
	LIVING_ENTITY_BASE_ARROWS(LIVING_ENTITY_BASE, 9, EntityDataTypes1_7_6_10.BYTE, EntityDataTypes1_8.BYTE),
	LIVING_ENTITY_BASE_NAME_TAG(LIVING_ENTITY_BASE, 10, EntityDataTypes1_7_6_10.STRING, 2, EntityDataTypes1_8.STRING),
	LIVING_ENTITY_BASE_NAME_TAG_VISIBILITY(LIVING_ENTITY_BASE, 11, EntityDataTypes1_7_6_10.BYTE, 3, EntityDataTypes1_8.BYTE),

	LIVING_ENTITY_AI(LIVING_ENTITY, -1, null, 15, EntityDataTypes1_8.BYTE),

	ABSTRACT_AGEABLE_AGE(ABSTRACT_AGEABLE, 12, EntityDataTypes1_7_6_10.INT, EntityDataTypes1_8.BYTE),

	ARMOR_STAND_FLAGS(ARMOR_STAND, -1, null, 10, EntityDataTypes1_8.BYTE),
	ARMOR_STAND_HEAD_POSITION(ARMOR_STAND, -1, null, 11, EntityDataTypes1_8.ROTATIONS),
	ARMOR_STAND_BODY_POSITION(ARMOR_STAND, -1, null, 12, EntityDataTypes1_8.ROTATIONS),
	ARMOR_STAND_LEFT_ARM_POSITION(ARMOR_STAND, -1, null, 13, EntityDataTypes1_8.ROTATIONS),
	ARMOR_STAND_RIGHT_ARM_POSITION(ARMOR_STAND, -1, null, 14, EntityDataTypes1_8.ROTATIONS),
	ARMOR_STAND_LEFT_LEG_POSITION(ARMOR_STAND, -1, null, 15, EntityDataTypes1_8.ROTATIONS),
	ARMOR_STAND_RIGHT_LEG_POSITION(ARMOR_STAND, -1, null, 16, EntityDataTypes1_8.ROTATIONS),

	PLAYER_SKIN_FLAGS(PLAYER, 16, EntityDataTypes1_7_6_10.BYTE, 10, EntityDataTypes1_8.BYTE),
	PLAYER_UNUSED(PLAYER, -1, null, 16, EntityDataTypes1_8.BYTE),
	PLAYER_ABSORPTION_HEATS(PLAYER, 17, EntityDataTypes1_7_6_10.FLOAT, EntityDataTypes1_8.FLOAT),
	PLAYER_SCORE(PLAYER, 18, EntityDataTypes1_7_6_10.INT, EntityDataTypes1_8.INT),

	HORSE_FLAGS(HORSE, 16, EntityDataTypes1_7_6_10.INT, EntityDataTypes1_8.INT),
	HORSE_TYPE(HORSE, 19, EntityDataTypes1_7_6_10.BYTE, EntityDataTypes1_8.BYTE),
	HORSE_COLOR(HORSE, 20, EntityDataTypes1_7_6_10.INT, EntityDataTypes1_8.INT),
	HORSE_OWNER(HORSE, 21, EntityDataTypes1_7_6_10.STRING, EntityDataTypes1_8.STRING),
	HORSE_ARMOR(HORSE, 22, EntityDataTypes1_7_6_10.INT, EntityDataTypes1_8.INT),

	BAT_HANGING(BAT, 16, EntityDataTypes1_7_6_10.BYTE, EntityDataTypes1_8.BYTE),

	TAMABLE_ANIMAL_FLAGS(TAMABLE_ANIMAL, 16, EntityDataTypes1_7_6_10.BYTE, EntityDataTypes1_8.BYTE),
	TAMABLE_ANIMAL_OWNER(TAMABLE_ANIMAL, 17, EntityDataTypes1_7_6_10.STRING, EntityDataTypes1_8.STRING),

	OCELOT_TYPE(OCELOT, 18, EntityDataTypes1_7_6_10.BYTE, EntityDataTypes1_8.BYTE),

	WOLF_FLAGS(WOLF, 16, EntityDataTypes1_7_6_10.BYTE, EntityDataTypes1_8.BYTE),
	WOLF_HEALTH(WOLF, 18, EntityDataTypes1_7_6_10.FLOAT, EntityDataTypes1_8.FLOAT),
	WOLF_BEGGING(WOLF, 19, EntityDataTypes1_7_6_10.BYTE, EntityDataTypes1_8.BYTE),
	WOLF_COLLAR_COLOR(WOLF, 20, EntityDataTypes1_7_6_10.BYTE, EntityDataTypes1_8.BYTE),

	PIG_SADDLE(PIG, 16, EntityDataTypes1_7_6_10.BYTE, EntityDataTypes1_8.BYTE),

	SHEEP_COLOR_OR_SHEARED(SHEEP, 16, EntityDataTypes1_7_6_10.BYTE, EntityDataTypes1_8.BYTE),

	VILLAGER_TYPE(VILLAGER, 16, EntityDataTypes1_7_6_10.INT, EntityDataTypes1_8.INT),

	ENDERMAN_CARRIED_BLOCK(ENDERMAN, 16, null, EntityDataTypes1_8.SHORT),
	ENDERMAN_CARRIED_BLOCK_DATA(ENDERMAN, 17, null, EntityDataTypes1_8.BYTE),
	ENDERMAN_IS_SCREAMING(ENDERMAN, 18, EntityDataTypes1_7_6_10.BYTE, EntityDataTypes1_8.BYTE),

	ZOMBIE_CHILD(ZOMBIE, 12, EntityDataTypes1_7_6_10.BYTE, EntityDataTypes1_8.BYTE),
	ZOMBIE_VILLAGER(ZOMBIE, 13, EntityDataTypes1_7_6_10.BYTE, EntityDataTypes1_8.BYTE),
	ZOMBIE_CONVERTING(ZOMBIE, 14, EntityDataTypes1_7_6_10.BYTE, EntityDataTypes1_8.BYTE),

	BLAZE_ON_FIRE(BLAZE, 16, EntityDataTypes1_7_6_10.BYTE, EntityDataTypes1_8.BYTE),

	SPIDER_CLIMBING(SPIDER, 16, EntityDataTypes1_7_6_10.BYTE, EntityDataTypes1_8.BYTE),

	CREEPER_STATE(CREEPER, 16, EntityDataTypes1_7_6_10.BYTE, EntityDataTypes1_8.BYTE),
	CREEPER_POWERED(CREEPER, 17, EntityDataTypes1_7_6_10.BYTE, EntityDataTypes1_8.BYTE),
	CREEPER_IGNITED(CREEPER, 18, EntityDataTypes1_7_6_10.BYTE, EntityDataTypes1_8.BYTE),

	GHAST_STATE(GHAST, 16, EntityDataTypes1_7_6_10.BYTE, EntityDataTypes1_8.BYTE),
	GHAST_IS_POWERED(GHAST, 17, null, EntityDataTypes1_8.BYTE),

	SLIME_SIZE(SLIME, 16, EntityDataTypes1_7_6_10.BYTE, EntityDataTypes1_8.BYTE),

	SKELETON_TYPE(SKELETON, 13, EntityDataTypes1_7_6_10.BYTE, EntityDataTypes1_8.BYTE),

	WITCH_AGGRESSIVE(WITCH, 21, EntityDataTypes1_7_6_10.BYTE, EntityDataTypes1_8.BYTE),

	IRON_GOLEM_IS_PLAYER_CREATED(IRON_GOLEM, 16, EntityDataTypes1_7_6_10.BYTE, EntityDataTypes1_8.BYTE),

	WITHER_WATCHED_TARGET_1(WITHER, 17, EntityDataTypes1_7_6_10.INT, EntityDataTypes1_8.INT),
	WITHER_WATCHED_TARGET_2(WITHER, 18, EntityDataTypes1_7_6_10.INT, EntityDataTypes1_8.INT),
	WITHER_WATCHED_TARGET_3(WITHER, 19, EntityDataTypes1_7_6_10.INT, EntityDataTypes1_8.INT),
	WITHER_INVULNERABLE_TIME(WITHER, 20, EntityDataTypes1_7_6_10.INT, EntityDataTypes1_8.INT),

	GUARDIAN_FLAGS(GUARDIAN, 16, null, EntityDataTypes1_8.BYTE),
	GUARDIAN_TARGET(GUARDIAN, 17, null, EntityDataTypes1_8.INT),

	BOAT_TIME_SINCE_HIT(BOAT, 17, EntityDataTypes1_7_6_10.INT, EntityDataTypes1_8.INT),
	BOAT_FORWARD_DIRECTION(BOAT, 18, EntityDataTypes1_7_6_10.INT, EntityDataTypes1_8.INT),
	BOAT_DAMAGE_TAKEN(BOAT, 19, EntityDataTypes1_7_6_10.FLOAT, EntityDataTypes1_8.FLOAT),

	ABSTRACT_MINECART_SHAKING_POWER(ABSTRACT_MINECART, 17, EntityDataTypes1_7_6_10.INT, EntityDataTypes1_8.INT),
	ABSTRACT_MINECART_SHAKING_DIRECTION(ABSTRACT_MINECART, 18, EntityDataTypes1_7_6_10.INT, EntityDataTypes1_8.INT),
	ABSTRACT_MINECART_DAMAGE_TAKEN(ABSTRACT_MINECART, 19, EntityDataTypes1_7_6_10.FLOAT, EntityDataTypes1_8.FLOAT),
	ABSTRACT_MINECART_BLOCK_INSIDE(ABSTRACT_MINECART, 20, EntityDataTypes1_7_6_10.INT, EntityDataTypes1_8.INT),
	ABSTRACT_MINECART_BLOCK_Y(ABSTRACT_MINECART, 21, EntityDataTypes1_7_6_10.INT, EntityDataTypes1_8.INT),
	ABSTRACT_MINECART_SHOW_BLOCK(ABSTRACT_MINECART, 22, EntityDataTypes1_7_6_10.BYTE, EntityDataTypes1_8.BYTE),

	FURNACE_MINECART_IS_POWERED(FURNACE_MINECART, 16, EntityDataTypes1_7_6_10.BYTE, EntityDataTypes1_8.BYTE),

	ITEM_ITEM(ITEM, 10, EntityDataTypes1_7_6_10.ITEM, EntityDataTypes1_8.ITEM),

	ARROW_IS_CRITICAL(ARROW, 16, EntityDataTypes1_7_6_10.BYTE, EntityDataTypes1_8.BYTE),

	FIREWORK_ROCKET_INFO(FIREWORK_ROCKET, 8, EntityDataTypes1_7_6_10.ITEM, EntityDataTypes1_8.ITEM),

	ITEM_FRAME_ITEM(ITEM_FRAME, 2, EntityDataTypes1_7_6_10.ITEM, 8, EntityDataTypes1_8.ITEM),
	ITEM_FRAME_ROTATION(ITEM_FRAME, 3, EntityDataTypes1_7_6_10.BYTE, 9, EntityDataTypes1_8.BYTE),

	END_CRYSTAL_HEALTH(END_CRYSTAL, 8, EntityDataTypes1_7_6_10.INT, 9, EntityDataTypes1_8.INT);

	private static final HashMap<Pair<EntityTypes1_8.EntityType, Integer>, EntityDataIndex1_7_6_10> ENTITY_DATA_REWRITES = new HashMap<>();

	static {
		for (EntityDataIndex1_7_6_10 index : EntityDataIndex1_7_6_10.values()) {
			ENTITY_DATA_REWRITES.put(new Pair<>(index.getClazz(), index.getNewIndex()), index);
		}
	}

	private final EntityTypes1_8.EntityType clazz;
	private final int newIndex;
	private final EntityDataTypes1_8 newType;
	private final EntityDataTypes1_7_6_10 oldType;
	private final int index;

	EntityDataIndex1_7_6_10(EntityTypes1_8.EntityType type, int index, EntityDataTypes1_7_6_10 oldType, EntityDataTypes1_8 newType) {
		this.clazz = type;
		this.index = index;
		this.newIndex = index;
		this.oldType = oldType;
		this.newType = newType;
	}

	EntityDataIndex1_7_6_10(EntityTypes1_8.EntityType type, int index, EntityDataTypes1_7_6_10 oldType, int newIndex, EntityDataTypes1_8 newType) {
		this.clazz = type;
		this.index = index;
		this.oldType = oldType;
		this.newIndex = newIndex;
		this.newType = newType;
	}

	private static Optional<EntityDataIndex1_7_6_10> getIndex(EntityType type, int index) {
		Pair<EntityType, Integer> pair = new Pair<>(type, index);
		if (ENTITY_DATA_REWRITES.containsKey(pair)) {
			return Optional.of(ENTITY_DATA_REWRITES.get(pair));
		}

		return Optional.empty();
	}

	public EntityTypes1_8.EntityType getClazz() {
		return clazz;
	}

	public int getNewIndex() {
		return newIndex;
	}

	public EntityDataTypes1_8 getNewType() {
		return newType;
	}

	public EntityDataTypes1_7_6_10 getOldType() {
		return oldType;
	}

	public int getIndex() {
		return index;
	}

	public static EntityDataIndex1_7_6_10 searchIndex(EntityType type, int index) {
		EntityType currentType = type;
		do {
			Optional<EntityDataIndex1_7_6_10> optMeta = getIndex(currentType, index);

			if (optMeta.isPresent()) {
				return optMeta.get();
			}

			currentType = currentType.getParent();
		} while (currentType != null);

		return null;
	}
}