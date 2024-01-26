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

package com.viaversion.viarewind.protocol.protocol1_7_6_10to1_8.model;

import com.viaversion.viarewind.protocol.protocol1_7_6_10to1_8.types.metadata.MetaType1_7_6_10;
import com.viaversion.viaversion.api.minecraft.entities.EntityTypes1_10;
import com.viaversion.viaversion.api.minecraft.metadata.types.MetaType1_8;
import com.viaversion.viaversion.util.Pair;

import java.util.HashMap;
import java.util.Optional;

public enum MetaIndex1_7_6_10To1_8 {

	ENTITY_FLAGS(EntityTypes1_10.EntityType.ENTITY, 0, MetaType1_7_6_10.Byte, MetaType1_8.Byte),
	ENTITY_AIR(EntityTypes1_10.EntityType.ENTITY, 1, MetaType1_7_6_10.Short, MetaType1_8.Short),
	ENTITY_NAME_TAG(EntityTypes1_10.EntityType.ENTITY, -1, MetaType1_7_6_10.NonExistent, 2, MetaType1_8.String),
	ENTITY_NAME_TAG_VISIBILITY(EntityTypes1_10.EntityType.ENTITY, -1, MetaType1_7_6_10.NonExistent, 3, MetaType1_8.Byte),
	ENTITY_SILENT(EntityTypes1_10.EntityType.ENTITY, -1, MetaType1_7_6_10.NonExistent, 4, MetaType1_8.Byte),
	ENTITY_LIVING_HEALTH(EntityTypes1_10.EntityType.ENTITY_LIVING, 6, MetaType1_7_6_10.Float, MetaType1_8.Float),
	ENTITY_LIVING_POTION_EFFECT_COLOR(EntityTypes1_10.EntityType.ENTITY_LIVING, 7, MetaType1_7_6_10.Int, MetaType1_8.Int),
	ENTITY_LIVING_IS_POTION_EFFECT_AMBIENT(EntityTypes1_10.EntityType.ENTITY_LIVING, 8, MetaType1_7_6_10.Byte, MetaType1_8.Byte),
	ENTITY_LIVING_ARROWS(EntityTypes1_10.EntityType.ENTITY_LIVING, 9, MetaType1_7_6_10.Byte, MetaType1_8.Byte),
	ENTITY_LIVING_NAME_TAG(EntityTypes1_10.EntityType.ENTITY_LIVING, 10, MetaType1_7_6_10.String, 2, MetaType1_8.String),
	ENTITY_LIVING_NAME_TAG_VISIBILITY(EntityTypes1_10.EntityType.ENTITY_LIVING, 11, MetaType1_7_6_10.Byte, 3, MetaType1_8.Byte),
	ENTITY_LIVING_AI(EntityTypes1_10.EntityType.ENTITY_LIVING, -1, MetaType1_7_6_10.NonExistent, 15, MetaType1_8.Byte),
	ENTITY_AGEABLE_AGE(EntityTypes1_10.EntityType.ENTITY_AGEABLE, 12, MetaType1_7_6_10.Int, MetaType1_8.Byte),
	ARMOR_STAND_FLAGS(EntityTypes1_10.EntityType.ARMOR_STAND, -1, MetaType1_7_6_10.NonExistent, 10, MetaType1_8.Byte),
	ARMOR_STAND_HEAD_POSITION(EntityTypes1_10.EntityType.ARMOR_STAND, -1, MetaType1_7_6_10.NonExistent, 11, MetaType1_8.Rotation),
	ARMOR_STAND_BODY_POSITION(EntityTypes1_10.EntityType.ARMOR_STAND, -1, MetaType1_7_6_10.NonExistent, 12, MetaType1_8.Rotation),
	ARMOR_STAND_LEFT_ARM_POSITION(EntityTypes1_10.EntityType.ARMOR_STAND, -1, MetaType1_7_6_10.NonExistent, 13, MetaType1_8.Rotation),
	ARMOR_STAND_RIGHT_ARM_POSITION(EntityTypes1_10.EntityType.ARMOR_STAND, -1, MetaType1_7_6_10.NonExistent, 14, MetaType1_8.Rotation),
	ARMOR_STAND_LEFT_LEG_POSITION(EntityTypes1_10.EntityType.ARMOR_STAND, -1, MetaType1_7_6_10.NonExistent, 15, MetaType1_8.Rotation),
	ARMOR_STAND_RIGHT_LEG_POSITION(EntityTypes1_10.EntityType.ARMOR_STAND, -1, MetaType1_7_6_10.NonExistent, 16, MetaType1_8.Rotation),
	HUMAN_SKIN_FLAGS(EntityTypes1_10.EntityType.ENTITY_HUMAN, 16, MetaType1_7_6_10.Byte, 10, MetaType1_8.Byte),
	HUMAN_UNUSED(EntityTypes1_10.EntityType.ENTITY_HUMAN, -1, MetaType1_7_6_10.NonExistent, 16, MetaType1_8.Byte),
	HUMAN_ABSORPTION_HEATS(EntityTypes1_10.EntityType.ENTITY_HUMAN, 17, MetaType1_7_6_10.Float, MetaType1_8.Float),
	HUMAN_SCORE(EntityTypes1_10.EntityType.ENTITY_HUMAN, 18, MetaType1_7_6_10.Int, MetaType1_8.Int),
	HORSE_FLAGS(EntityTypes1_10.EntityType.HORSE, 16, MetaType1_7_6_10.Int, MetaType1_8.Int),
	HORSE_TYPE(EntityTypes1_10.EntityType.HORSE, 19, MetaType1_7_6_10.Byte, MetaType1_8.Byte),
	HORSE_COLOR(EntityTypes1_10.EntityType.HORSE, 20, MetaType1_7_6_10.Int, MetaType1_8.Int),
	HORSE_OWNER(EntityTypes1_10.EntityType.HORSE, 21, MetaType1_7_6_10.String, MetaType1_8.String),
	HORSE_ARMOR(EntityTypes1_10.EntityType.HORSE, 22, MetaType1_7_6_10.Int, MetaType1_8.Int),
	BAT_HANGING(EntityTypes1_10.EntityType.BAT, 16, MetaType1_7_6_10.Byte, MetaType1_8.Byte),
	TAMEABLE_FLAGS(EntityTypes1_10.EntityType.ENTITY_TAMEABLE_ANIMAL, 16, MetaType1_7_6_10.Byte, MetaType1_8.Byte),
	TAMEABLE_OWNER(EntityTypes1_10.EntityType.ENTITY_TAMEABLE_ANIMAL, 17, MetaType1_7_6_10.String, MetaType1_8.String),
	OCELOT_TYPE(EntityTypes1_10.EntityType.OCELOT, 18, MetaType1_7_6_10.Byte, MetaType1_8.Byte),
	WOLF_FLAGS(EntityTypes1_10.EntityType.WOLF, 16, MetaType1_7_6_10.Byte, MetaType1_8.Byte),
	WOLF_HEALTH(EntityTypes1_10.EntityType.WOLF, 18, MetaType1_7_6_10.Float, MetaType1_8.Float),
	WOLF_BEGGING(EntityTypes1_10.EntityType.WOLF, 19, MetaType1_7_6_10.Byte, MetaType1_8.Byte),
	WOLF_COLLAR_COLOR(EntityTypes1_10.EntityType.WOLF, 20, MetaType1_7_6_10.Byte, MetaType1_8.Byte),
	PIG_SADDLE(EntityTypes1_10.EntityType.PIG, 16, MetaType1_7_6_10.Byte, MetaType1_8.Byte),
	SHEEP_COLOR_OR_SHEARED(EntityTypes1_10.EntityType.SHEEP, 16, MetaType1_7_6_10.Byte, MetaType1_8.Byte),
	VILLAGER_TYPE(EntityTypes1_10.EntityType.VILLAGER, 16, MetaType1_7_6_10.Int, MetaType1_8.Int),
	ENDERMAN_CARRIED_BLOCK(EntityTypes1_10.EntityType.ENDERMAN, 16, MetaType1_7_6_10.NonExistent, MetaType1_8.Short),
	ENDERMAN_CARRIED_BLOCK_DATA(EntityTypes1_10.EntityType.ENDERMAN, 17, MetaType1_7_6_10.NonExistent, MetaType1_8.Byte),
	ENDERMAN_IS_SCREAMING(EntityTypes1_10.EntityType.ENDERMAN, 18, MetaType1_7_6_10.Byte, MetaType1_8.Byte),
	ZOMBIE_CHILD(EntityTypes1_10.EntityType.ZOMBIE, 12, MetaType1_7_6_10.Byte, MetaType1_8.Byte),
	ZOMBIE_VILLAGER(EntityTypes1_10.EntityType.ZOMBIE, 13, MetaType1_7_6_10.Byte, MetaType1_8.Byte),
	ZOMBIE_CONVERTING(EntityTypes1_10.EntityType.ZOMBIE, 14, MetaType1_7_6_10.Byte, MetaType1_8.Byte),
	BLAZE_ON_FIRE(EntityTypes1_10.EntityType.BLAZE, 16, MetaType1_7_6_10.Byte, MetaType1_8.Byte),
	SPIDER_CLIMBING(EntityTypes1_10.EntityType.SPIDER, 16, MetaType1_7_6_10.Byte, MetaType1_8.Byte),
	CREEPER_STATE(EntityTypes1_10.EntityType.CREEPER, 16, MetaType1_7_6_10.Byte, MetaType1_8.Byte),
	CREEPER_POWERED(EntityTypes1_10.EntityType.CREEPER, 17, MetaType1_7_6_10.Byte, MetaType1_8.Byte),
	CREEPER_IGNITED(EntityTypes1_10.EntityType.CREEPER, 18, MetaType1_7_6_10.Byte, MetaType1_8.Byte),
	GHAST_STATE(EntityTypes1_10.EntityType.GHAST, 16, MetaType1_7_6_10.Byte, MetaType1_8.Byte),
	GHAST_IS_POWERED(EntityTypes1_10.EntityType.GHAST, 17, MetaType1_7_6_10.NonExistent, MetaType1_8.Byte),
	SLIME_SIZE(EntityTypes1_10.EntityType.SLIME, 16, MetaType1_7_6_10.Byte, MetaType1_8.Byte),
	SKELETON_TYPE(EntityTypes1_10.EntityType.SKELETON, 13, MetaType1_7_6_10.Byte, MetaType1_8.Byte),
	WITCH_AGGRESSIVE(EntityTypes1_10.EntityType.WITCH, 21, MetaType1_7_6_10.Byte, MetaType1_8.Byte),
	IRON_GOLEM_IS_PLAYER_CREATED(EntityTypes1_10.EntityType.IRON_GOLEM, 16, MetaType1_7_6_10.Byte, MetaType1_8.Byte),
	WITHER_WATCHED_TARGET_1(EntityTypes1_10.EntityType.WITHER, 17, MetaType1_7_6_10.Int, MetaType1_8.Int),
	WITHER_WATCHED_TARGET_2(EntityTypes1_10.EntityType.WITHER, 18, MetaType1_7_6_10.Int, MetaType1_8.Int),
	WITHER_WATCHED_TARGET_3(EntityTypes1_10.EntityType.WITHER, 19, MetaType1_7_6_10.Int, MetaType1_8.Int),
	WITHER_INVULNERABLE_TIME(EntityTypes1_10.EntityType.WITHER, 20, MetaType1_7_6_10.Int, MetaType1_8.Int),
	GUARDIAN_FLAGS(EntityTypes1_10.EntityType.GUARDIAN, 16, MetaType1_7_6_10.NonExistent, MetaType1_8.Byte),
	GUARDIAN_TARGET(EntityTypes1_10.EntityType.GUARDIAN, 17, MetaType1_7_6_10.NonExistent, MetaType1_8.Int),
	BOAT_TIME_SINCE_HIT(EntityTypes1_10.EntityType.BOAT, 17, MetaType1_7_6_10.Int, MetaType1_8.Int),
	BOAT_FORWARD_DIRECTION(EntityTypes1_10.EntityType.BOAT, 18, MetaType1_7_6_10.Int, MetaType1_8.Int),
	BOAT_DAMAGE_TAKEN(EntityTypes1_10.EntityType.BOAT, 19, MetaType1_7_6_10.Float, MetaType1_8.Float),
	MINECART_SHAKING_POWER(EntityTypes1_10.EntityType.MINECART_ABSTRACT, 17, MetaType1_7_6_10.Int, MetaType1_8.Int),
	MINECART_SHAKING_DIRECTION(EntityTypes1_10.EntityType.MINECART_ABSTRACT, 18, MetaType1_7_6_10.Int, MetaType1_8.Int),
	MINECART_DAMAGE_TAKEN(EntityTypes1_10.EntityType.MINECART_ABSTRACT, 19, MetaType1_7_6_10.Float, MetaType1_8.Float),
	MINECART_BLOCK_INSIDE(EntityTypes1_10.EntityType.MINECART_ABSTRACT, 20, MetaType1_7_6_10.Int, MetaType1_8.Int),
	MINECART_BLOCK_Y(EntityTypes1_10.EntityType.MINECART_ABSTRACT, 21, MetaType1_7_6_10.Int, MetaType1_8.Int),
	MINECART_SHOW_BLOCK(EntityTypes1_10.EntityType.MINECART_ABSTRACT, 22, MetaType1_7_6_10.Byte, MetaType1_8.Byte),
	FURNACE_MINECART_IS_POWERED(EntityTypes1_10.EntityType.MINECART_FURNACE, 16, MetaType1_7_6_10.Byte, MetaType1_8.Byte),
	ITEM_ITEM(EntityTypes1_10.EntityType.DROPPED_ITEM, 10, MetaType1_7_6_10.Slot, MetaType1_8.Slot),
	ARROW_IS_CRITICAL(EntityTypes1_10.EntityType.ARROW, 16, MetaType1_7_6_10.Byte, MetaType1_8.Byte),
	FIREWORK_INFO(EntityTypes1_10.EntityType.FIREWORK, 8, MetaType1_7_6_10.Slot, MetaType1_8.Slot),
	ITEM_FRAME_ITEM(EntityTypes1_10.EntityType.ITEM_FRAME, 2, MetaType1_7_6_10.Slot, 8, MetaType1_8.Slot),
	ITEM_FRAME_ROTATION(EntityTypes1_10.EntityType.ITEM_FRAME, 3, MetaType1_7_6_10.Byte, 9, MetaType1_8.Byte),
	ENDER_CRYSTAL_HEALTH(EntityTypes1_10.EntityType.ENDER_CRYSTAL, 8, MetaType1_7_6_10.Int, 9, MetaType1_8.Int),
	;

	private final static HashMap<Pair<EntityTypes1_10.EntityType, Integer>, MetaIndex1_7_6_10To1_8> metadataRewrites = new HashMap<>();

	static {
		for (MetaIndex1_7_6_10To1_8 index : MetaIndex1_7_6_10To1_8.values()) {
			metadataRewrites.put(new Pair<>(index.getClazz(), index.getNewIndex()), index);
		}
	}

	private final EntityTypes1_10.EntityType clazz;
	private final int newIndex;
	private final MetaType1_8 newType;
	private final MetaType1_7_6_10 oldType;
	private final int index;

	MetaIndex1_7_6_10To1_8(EntityTypes1_10.EntityType type, int index, MetaType1_7_6_10 oldType, MetaType1_8 newType) {
		this.clazz = type;
		this.index = index;
		this.newIndex = index;
		this.oldType = oldType;
		this.newType = newType;
	}

	MetaIndex1_7_6_10To1_8(EntityTypes1_10.EntityType type, int index, MetaType1_7_6_10 oldType, int newIndex, MetaType1_8 newType) {
		this.clazz = type;
		this.index = index;
		this.oldType = oldType;
		this.newIndex = newIndex;
		this.newType = newType;
	}

	private static Optional<MetaIndex1_7_6_10To1_8> getIndex(EntityTypes1_10.EntityType type, int index) {
		Pair<EntityTypes1_10.EntityType, Integer> pair = new Pair<>(type, index);
		if (metadataRewrites.containsKey(pair)) {
			return Optional.of(metadataRewrites.get(pair));
		}

		return Optional.empty();
	}

	public EntityTypes1_10.EntityType getClazz() {
		return clazz;
	}

	public int getNewIndex() {
		return newIndex;
	}

	public MetaType1_8 getNewType() {
		return newType;
	}

	public MetaType1_7_6_10 getOldType() {
		return oldType;
	}

	public int getIndex() {
		return index;
	}

	public static MetaIndex1_7_6_10To1_8 searchIndex(EntityTypes1_10.EntityType type, int index) {
		EntityTypes1_10.EntityType currentType = type;
		do {
			Optional<MetaIndex1_7_6_10To1_8> optMeta = getIndex(currentType, index);

			if (optMeta.isPresent()) {
				return optMeta.get();
			}

			currentType = currentType.getParent();
		} while (currentType != null);

		return null;
	}
}