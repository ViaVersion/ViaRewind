package com.viaversion.viarewind.protocol.protocol1_7_6_10to1_8.storage;

import com.viaversion.viaversion.api.connection.StoredObject;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.minecraft.item.Item;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerSessionStorage extends StoredObject {

	// Player info
	public int gameMode;

	// Player abilities
	public boolean sprinting, allowFly, flying, invincible, creative;
	public float flySpeed, walkSpeed;

	// Player position, rotation and data
	private double posX, posY, posZ;
	public double receivedPosY;
	public float yaw, pitch;
	public boolean onGround;

	// Player inventory
	private final Map<UUID, Item[]> playerEquipment = new HashMap<>();

	public PlayerSessionStorage(UserConnection user) {
		super(user);
	}

	public byte combineAbilities() {
		byte flags = 0;

		if (invincible) flags |= 8;
		if (allowFly) flags |= 4;
		if (flying) flags |= 2;
		if (creative) flags |= 1;

		return flags;
	}

	public double getPosX() {
		return posX;
	}

	public double getPosY() {
		return posY;
	}

	public double getPosZ() {
		return posZ;
	}

	public void setPos(double x, double y, double z) {
		posX = x;
		posY = y;
		posZ = z;
	}

	public Item getPlayerEquipment(final UUID uuid, final int slot) {
		final Item[] items = playerEquipment.get(uuid);
		if (items == null || slot < 0 || slot >= items.length) {
			return null;
		}

		return items[slot];
	}

	public void setPlayerEquipment(final UUID uuid, final Item equipment, final int slot) {
		final Item[] items = playerEquipment.computeIfAbsent(uuid, it -> new Item[5]);
		if (slot < 0 || slot >= items.length) {
			return;
		}

		items[slot] = equipment;
	}

	public boolean isSpectator() {
		return gameMode == 3;
	}

	public Map<UUID, Item[]> getPlayerEquipment() {
		return playerEquipment;
	}
}
