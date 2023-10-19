package com.viaversion.viarewind.protocol.protocol1_7_6_10to1_8.storage;

import com.viaversion.viarewind.ViaRewind;
import com.viaversion.viarewind.api.rewriter.ReplacementEntityTracker;
import com.viaversion.viarewind.protocol.protocol1_7_2_5to1_7_6_10.ServerboundPackets1_7_2_5;
import com.viaversion.viarewind.protocol.protocol1_7_6_10to1_8.Protocol1_7_6_10To1_8;
import com.viaversion.viarewind.protocol.protocol1_7_6_10to1_8.model.VirtualHologramEntity;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.minecraft.entities.Entity1_10Types;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.protocols.protocol1_8.ClientboundPackets1_8;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

public class EntityTracker1_7_6_10 extends ReplacementEntityTracker {

	private final Map<Integer, Integer> vehicleMap = new ConcurrentHashMap<>();
	private final Map<Integer, VirtualHologramEntity> virtualHologramMap = new HashMap<>();

	private final Map<Integer, UUID> playersByEntityId = new HashMap<>();
	private final Map<UUID, Integer> playersByUniqueId = new HashMap<>();

	public int spectatingPlayerId = -1;

	public EntityTracker1_7_6_10(UserConnection user) {
		super(user, ProtocolVersion.v1_8);

		registerEntity(Entity1_10Types.EntityType.GUARDIAN, Entity1_10Types.EntityType.SQUID, "Guardian");
		registerEntity(Entity1_10Types.EntityType.ENDERMITE, Entity1_10Types.EntityType.SQUID, "Endermite");
		registerEntity(Entity1_10Types.EntityType.RABBIT, Entity1_10Types.EntityType.CHICKEN, "Rabbit");
	}

	@Override
	public void removeEntity(int entityId) {
		super.removeEntity(entityId);

		if (playersByEntityId.containsKey(entityId)) {
			final UUID playerId = playersByEntityId.remove(entityId);

			playersByUniqueId.remove(playerId);
			getUser().get(PlayerSessionStorage.class).getPlayerEquipment().remove(playerId);
		}
	}

	@Override
	public void clear() {
		super.clear();

		vehicleMap.clear();
	}

	public void addPlayer(final Integer entityId, final UUID uuid) {
		playersByUniqueId.put(uuid, entityId);
		playersByEntityId.put(entityId, uuid);
	}

	public UUID getPlayerUUID(final int entityId) {
		return playersByEntityId.get(entityId);
	}

	public int getPlayerEntityId(final UUID uuid) {
		return playersByUniqueId.getOrDefault(uuid, -1);
	}

	public int getVehicle(final int passengerId) {
		for (Map.Entry<Integer, Integer> vehicle : vehicleMap.entrySet()) {
			if (vehicle.getValue() == passengerId) {
				return vehicle.getValue();
			}
		}
		return -1;
	}

	public int getPassenger(int vehicleId) {
		return vehicleMap.getOrDefault(vehicleId, -1);
	}

	protected void startSneaking() {
		try {
			final PacketWrapper entityAction = PacketWrapper.create(ServerboundPackets1_7_2_5.ENTITY_ACTION, getUser());
			entityAction.write(Type.VAR_INT, this.getPlayerId()); // entity id
			entityAction.write(Type.VAR_INT, 0); // action id, start sneaking
			entityAction.write(Type.VAR_INT, 0); // jump boost

			entityAction.sendToServer(Protocol1_7_6_10To1_8.class, true);
		} catch (Exception e) {
			ViaRewind.getPlatform().getLogger().log(Level.SEVERE, "Failed to send sneak packet", e);
		}
	}

	public void setPassenger(int vehicleId, int passengerId) {
		if (vehicleId == this.spectatingPlayerId && this.spectatingPlayerId != this.getPlayerId()) {
			startSneaking();
		}

		if (vehicleId == -1) {
			vehicleMap.remove(getVehicle(passengerId));
		} else if (passengerId == -1) {
			vehicleMap.remove(vehicleId);
		} else {
			vehicleMap.put(vehicleId, passengerId);
		}
	}

	protected void attachEntity(final int target) {
		try {
			final PacketWrapper attachEntity = PacketWrapper.create(ClientboundPackets1_8.ATTACH_ENTITY, getUser());
			attachEntity.write(Type.INT, this.getPlayerId()); // vehicle id
			attachEntity.write(Type.INT, target); // passenger id
			attachEntity.write(Type.BOOLEAN, false); // leash

			attachEntity.scheduleSend(Protocol1_7_6_10To1_8.class, true);
		} catch (Exception e) {
			ViaRewind.getPlatform().getLogger().log(Level.SEVERE, "Failed to send attach packet", e);
		}
	}

	public boolean setSpectating(int spectating) {
		if (spectating != this.getPlayerId() && getPassenger(spectating) != -1) {
			startSneaking();
			setSpectating(this.getPlayerId());
			return false;
		}
		if (this.spectatingPlayerId != spectating && this.spectatingPlayerId != this.getPlayerId()) {
			attachEntity(-1);
		}
		this.spectatingPlayerId = spectating;
		if (spectating != this.getPlayerId()) {
			attachEntity(this.spectatingPlayerId);
		}
		return true;
	}

	public Map<Integer, Integer> getVehicleMap() {
		return vehicleMap;
	}

	public Map<Integer, VirtualHologramEntity> getVirtualHologramMap() {
		return virtualHologramMap;
	}
}
