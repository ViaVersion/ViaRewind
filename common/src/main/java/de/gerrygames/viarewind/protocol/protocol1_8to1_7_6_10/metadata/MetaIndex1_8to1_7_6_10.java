package de.gerrygames.viarewind.protocol.protocol1_8to1_7_6_10.metadata;

import com.viaversion.viaversion.api.minecraft.entities.Entity1_10Types;
import com.viaversion.viaversion.api.minecraft.metadata.types.MetaType1_8;
import com.viaversion.viaversion.util.Pair;
import de.gerrygames.viarewind.protocol.protocol1_7_6_10to1_8.types.MetaType1_7_6_10;

import java.util.HashMap;
import java.util.Optional;

public enum MetaIndex1_8to1_7_6_10 {

	ENTITY_FLAGS(Entity1_10Types.EntityType.ENTITY, 0, MetaType1_7_6_10.Byte, MetaType1_8.Byte),
	ENTITY_AIR(Entity1_10Types.EntityType.ENTITY, 1, MetaType1_7_6_10.Short, MetaType1_8.Short),
	ENTITY_NAME_TAG(Entity1_10Types.EntityType.ENTITY, -1, MetaType1_7_6_10.NonExistent, 2, MetaType1_8.String),
	ENTITY_NAME_TAG_VISIBILITY(Entity1_10Types.EntityType.ENTITY, -1, MetaType1_7_6_10.NonExistent, 3, MetaType1_8.Byte),
	ENTITY_SILENT(Entity1_10Types.EntityType.ENTITY, -1, MetaType1_7_6_10.NonExistent, 4, MetaType1_8.Byte),
	ENTITY_LIVING_HEALTH(Entity1_10Types.EntityType.ENTITY_LIVING, 6, MetaType1_7_6_10.Float, MetaType1_8.Float),
	ENTITY_LIVING_POTION_EFFECT_COLOR(Entity1_10Types.EntityType.ENTITY_LIVING, 7, MetaType1_7_6_10.Int, MetaType1_8.Int),
	ENTITY_LIVING_IS_POTION_EFFECT_AMBIENT(Entity1_10Types.EntityType.ENTITY_LIVING, 8, MetaType1_7_6_10.Byte, MetaType1_8.Byte),
	ENTITY_LIVING_ARROWS(Entity1_10Types.EntityType.ENTITY_LIVING, 9, MetaType1_7_6_10.Byte, MetaType1_8.Byte),
	ENTITY_LIVING_NAME_TAG(Entity1_10Types.EntityType.ENTITY_LIVING, 10, MetaType1_7_6_10.String, 2, MetaType1_8.String),
	ENTITY_LIVING_NAME_TAG_VISIBILITY(Entity1_10Types.EntityType.ENTITY_LIVING, 11, MetaType1_7_6_10.Byte, 3, MetaType1_8.Byte),
	ENTITY_LIVING_AI(Entity1_10Types.EntityType.ENTITY_LIVING, -1, MetaType1_7_6_10.NonExistent, 15, MetaType1_8.Byte),
	ENTITY_AGEABLE_AGE(Entity1_10Types.EntityType.ENTITY_AGEABLE, 12, MetaType1_7_6_10.Int, MetaType1_8.Byte),
	ARMOR_STAND_FLAGS(Entity1_10Types.EntityType.ARMOR_STAND, -1, MetaType1_7_6_10.NonExistent, 10, MetaType1_8.Byte),
	ARMOR_STAND_HEAD_POSITION(Entity1_10Types.EntityType.ARMOR_STAND, -1, MetaType1_7_6_10.NonExistent, 11, MetaType1_8.Rotation),
	ARMOR_STAND_BODY_POSITION(Entity1_10Types.EntityType.ARMOR_STAND, -1, MetaType1_7_6_10.NonExistent, 12, MetaType1_8.Rotation),
	ARMOR_STAND_LEFT_ARM_POSITION(Entity1_10Types.EntityType.ARMOR_STAND, -1, MetaType1_7_6_10.NonExistent, 13, MetaType1_8.Rotation),
	ARMOR_STAND_RIGHT_ARM_POSITION(Entity1_10Types.EntityType.ARMOR_STAND, -1, MetaType1_7_6_10.NonExistent, 14, MetaType1_8.Rotation),
	ARMOR_STAND_LEFT_LEG_POSITION(Entity1_10Types.EntityType.ARMOR_STAND, -1, MetaType1_7_6_10.NonExistent, 15, MetaType1_8.Rotation),
	ARMOR_STAND_RIGHT_LEG_POSITION(Entity1_10Types.EntityType.ARMOR_STAND, -1, MetaType1_7_6_10.NonExistent, 16, MetaType1_8.Rotation),
	HUMAN_SKIN_FLAGS(Entity1_10Types.EntityType.ENTITY_HUMAN, 16, MetaType1_7_6_10.Byte, 10, MetaType1_8.Byte),
	HUMAN_UNUSED(Entity1_10Types.EntityType.ENTITY_HUMAN, -1, MetaType1_7_6_10.NonExistent, 16, MetaType1_8.Byte),
	HUMAN_ABSORPTION_HEATS(Entity1_10Types.EntityType.ENTITY_HUMAN, 17, MetaType1_7_6_10.Float, MetaType1_8.Float),
	HUMAN_SCORE(Entity1_10Types.EntityType.ENTITY_HUMAN, 18, MetaType1_7_6_10.Int, MetaType1_8.Int),
	HORSE_FLAGS(Entity1_10Types.EntityType.HORSE, 16, MetaType1_7_6_10.Int, MetaType1_8.Int),
	HORSE_TYPE(Entity1_10Types.EntityType.HORSE, 19, MetaType1_7_6_10.Byte, MetaType1_8.Byte),
	HORSE_COLOR(Entity1_10Types.EntityType.HORSE, 20, MetaType1_7_6_10.Int, MetaType1_8.Int),
	HORSE_OWNER(Entity1_10Types.EntityType.HORSE, 21, MetaType1_7_6_10.String, MetaType1_8.String),
	HORSE_ARMOR(Entity1_10Types.EntityType.HORSE, 22, MetaType1_7_6_10.Int, MetaType1_8.Int),
	BAT_HANGING(Entity1_10Types.EntityType.BAT, 16, MetaType1_7_6_10.Byte, MetaType1_8.Byte),
	TAMEABLE_FLAGS(Entity1_10Types.EntityType.ENTITY_TAMEABLE_ANIMAL, 16, MetaType1_7_6_10.Byte, MetaType1_8.Byte),
	TAMEABLE_OWNER(Entity1_10Types.EntityType.ENTITY_TAMEABLE_ANIMAL, 17, MetaType1_7_6_10.String, MetaType1_8.String),
	OCELOT_TYPE(Entity1_10Types.EntityType.OCELOT, 18, MetaType1_7_6_10.Byte, MetaType1_8.Byte),
	WOLF_FLAGS(Entity1_10Types.EntityType.WOLF, 16, MetaType1_7_6_10.Byte, MetaType1_8.Byte),
	WOLF_HEALTH(Entity1_10Types.EntityType.WOLF, 18, MetaType1_7_6_10.Float, MetaType1_8.Float),
	WOLF_BEGGING(Entity1_10Types.EntityType.WOLF, 19, MetaType1_7_6_10.Byte, MetaType1_8.Byte),
	WOLF_COLLAR_COLOR(Entity1_10Types.EntityType.WOLF, 20, MetaType1_7_6_10.Byte, MetaType1_8.Byte),
	PIG_SADDLE(Entity1_10Types.EntityType.PIG, 16, MetaType1_7_6_10.Byte, MetaType1_8.Byte),
	SHEEP_COLOR_OR_SHEARED(Entity1_10Types.EntityType.SHEEP, 16, MetaType1_7_6_10.Byte, MetaType1_8.Byte),
	VILLAGER_TYPE(Entity1_10Types.EntityType.VILLAGER, 16, MetaType1_7_6_10.Int, MetaType1_8.Int),
	ENDERMAN_CARRIED_BLOCK(Entity1_10Types.EntityType.ENDERMAN, 16, MetaType1_7_6_10.NonExistent, MetaType1_8.Short),
	ENDERMAN_CARRIED_BLOCK_DATA(Entity1_10Types.EntityType.ENDERMAN, 17, MetaType1_7_6_10.NonExistent, MetaType1_8.Byte),
	ENDERMAN_IS_SCREAMING(Entity1_10Types.EntityType.ENDERMAN, 18, MetaType1_7_6_10.Byte, MetaType1_8.Byte),
	ZOMBIE_CHILD(Entity1_10Types.EntityType.ZOMBIE, 12, MetaType1_7_6_10.Byte, MetaType1_8.Byte),
	ZOMBIE_VILLAGER(Entity1_10Types.EntityType.ZOMBIE, 13, MetaType1_7_6_10.Byte, MetaType1_8.Byte),
	ZOMBIE_CONVERTING(Entity1_10Types.EntityType.ZOMBIE, 14, MetaType1_7_6_10.Byte, MetaType1_8.Byte),
	BLAZE_ON_FIRE(Entity1_10Types.EntityType.BLAZE, 16, MetaType1_7_6_10.Byte, MetaType1_8.Byte),
	SPIDER_CLIMBING(Entity1_10Types.EntityType.SPIDER, 16, MetaType1_7_6_10.Byte, MetaType1_8.Byte),
	CREEPER_STATE(Entity1_10Types.EntityType.CREEPER, 16, MetaType1_7_6_10.Byte, MetaType1_8.Byte),
	CREEPER_POWERED(Entity1_10Types.EntityType.CREEPER, 17, MetaType1_7_6_10.Byte, MetaType1_8.Byte),
	GHAST_STATE(Entity1_10Types.EntityType.GHAST, 16, MetaType1_7_6_10.Byte, MetaType1_8.Byte),
	GHAST_IS_POWERED(Entity1_10Types.EntityType.GHAST, 17, MetaType1_7_6_10.NonExistent, MetaType1_8.Byte),
	SLIME_SIZE(Entity1_10Types.EntityType.SLIME, 16, MetaType1_7_6_10.Byte, MetaType1_8.Byte),
	SKELETON_TYPE(Entity1_10Types.EntityType.SKELETON, 13, MetaType1_7_6_10.Byte, MetaType1_8.Byte),
	WITCH_AGRESSIVE(Entity1_10Types.EntityType.WITCH, 21, MetaType1_7_6_10.Byte, MetaType1_8.Byte),
	IRON_GOLEM_IS_PLAYER_CREATED(Entity1_10Types.EntityType.IRON_GOLEM, 16, MetaType1_7_6_10.Byte, MetaType1_8.Byte),
	WITHER_WATCHED_TAGRET_1(Entity1_10Types.EntityType.WITHER, 17, MetaType1_7_6_10.Int, MetaType1_8.Int),
	WITHER_WATCHED_TAGRET_2(Entity1_10Types.EntityType.WITHER, 18, MetaType1_7_6_10.Int, MetaType1_8.Int),
	WITHER_WATCHED_TAGRET_3(Entity1_10Types.EntityType.WITHER, 19, MetaType1_7_6_10.Int, MetaType1_8.Int),
	WITHER_INVULNERABLE_TIME(Entity1_10Types.EntityType.WITHER, 20, MetaType1_7_6_10.Int, MetaType1_8.Int),
	GUARDIAN_FLAGS(Entity1_10Types.EntityType.GUARDIAN, 16, MetaType1_7_6_10.NonExistent, MetaType1_8.Byte),
	GUARDIAN_TARGET(Entity1_10Types.EntityType.GUARDIAN, 17, MetaType1_7_6_10.NonExistent, MetaType1_8.Int),
	BOAT_TIME_SINCE_HIT(Entity1_10Types.EntityType.BOAT, 17, MetaType1_7_6_10.Int, MetaType1_8.Int),
	BOAT_FORWARD_DIRECTION(Entity1_10Types.EntityType.BOAT, 18, MetaType1_7_6_10.Int, MetaType1_8.Int),
	BOAT_DAMAGE_TAKEN(Entity1_10Types.EntityType.BOAT, 19, MetaType1_7_6_10.Float, MetaType1_8.Float),
	MINECART_SHAKING_POWER(Entity1_10Types.EntityType.MINECART_ABSTRACT, 17, MetaType1_7_6_10.Int, MetaType1_8.Int),
	MINECART_SHAKING_DIRECTION(Entity1_10Types.EntityType.MINECART_ABSTRACT, 18, MetaType1_7_6_10.Int, MetaType1_8.Int),
	MINECART_DAMAGE_TAKEN(Entity1_10Types.EntityType.MINECART_ABSTRACT, 19, MetaType1_7_6_10.Float, MetaType1_8.Float),
	MINECART_BLOCK_INSIDE(Entity1_10Types.EntityType.MINECART_ABSTRACT, 20, MetaType1_7_6_10.Int, MetaType1_8.Int),
	MINECART_BLOCK_Y(Entity1_10Types.EntityType.MINECART_ABSTRACT, 21, MetaType1_7_6_10.Int, MetaType1_8.Int),
	MINECART_SHOW_BLOCK(Entity1_10Types.EntityType.MINECART_ABSTRACT, 22, MetaType1_7_6_10.Byte, MetaType1_8.Byte),
	FURNACE_MINECART_IS_POWERED(Entity1_10Types.EntityType.MINECART_FURNACE, 16, MetaType1_7_6_10.Byte, MetaType1_8.Byte),
	ITEM_ITEM(Entity1_10Types.EntityType.DROPPED_ITEM, 10, MetaType1_7_6_10.Slot, MetaType1_8.Slot),
	ARROW_IS_CRITICAL(Entity1_10Types.EntityType.ARROW, 16, MetaType1_7_6_10.Byte, MetaType1_8.Byte),
	FIREWORK_INFO(Entity1_10Types.EntityType.FIREWORK, 8, MetaType1_7_6_10.Slot, MetaType1_8.Slot),
	ITEM_FRAME_ITEM(Entity1_10Types.EntityType.ITEM_FRAME, 2, MetaType1_7_6_10.Slot, 8, MetaType1_8.Slot),
	ITEM_FRAME_ROTATION(Entity1_10Types.EntityType.ITEM_FRAME, 3, MetaType1_7_6_10.Byte, 9, MetaType1_8.Byte),
	ENDER_CRYSTAL_HEALTH(Entity1_10Types.EntityType.ENDER_CRYSTAL, 8, MetaType1_7_6_10.Int, 9, MetaType1_8.Int),
	;

	private static final HashMap<Pair<Entity1_10Types.EntityType, Integer>, MetaIndex1_8to1_7_6_10> metadataRewrites = new HashMap<>();

	static {
		for (MetaIndex1_8to1_7_6_10 index : MetaIndex1_8to1_7_6_10.values())
			metadataRewrites.put(new Pair<>(index.getClazz(), index.getIndex()), index);
	}

	private final Entity1_10Types.EntityType clazz;
	private final int newIndex;
	private final MetaType1_8 newType;
	private final MetaType1_7_6_10 oldType;
	private final int index;

	MetaIndex1_8to1_7_6_10(Entity1_10Types.EntityType type, int index, MetaType1_7_6_10 oldType, MetaType1_8 newType) {
		this.clazz = type;
		this.index = index;
		this.newIndex = index;
		this.oldType = oldType;
		this.newType = newType;
	}

	MetaIndex1_8to1_7_6_10(Entity1_10Types.EntityType type, int index, MetaType1_7_6_10 oldType, int newIndex, MetaType1_8 newType) {
		this.clazz = type;
		this.index = index;
		this.oldType = oldType;
		this.newIndex = newIndex;
		this.newType = newType;
	}

	private static Optional<MetaIndex1_8to1_7_6_10> getIndex(Entity1_10Types.EntityType type, int index) {
		Pair pair = new Pair<>(type, index);
		if (metadataRewrites.containsKey(pair)) {
			return Optional.of(metadataRewrites.get(pair));
		}

		return Optional.empty();
	}

	public Entity1_10Types.EntityType getClazz() {
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

	public static MetaIndex1_8to1_7_6_10 searchIndex(Entity1_10Types.EntityType type, int index) {
		Entity1_10Types.EntityType currentType = type;
		do {
			Optional<MetaIndex1_8to1_7_6_10> optMeta = getIndex(currentType, index);

			if (optMeta.isPresent()) {
				return optMeta.get();
			}

			currentType = currentType.getParent();
		} while (currentType != null);

		return null;
	}

}