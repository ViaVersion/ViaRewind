package de.gerrygames.viarewind.protocol.protocol1_8to1_9.storage;

import de.gerrygames.viarewind.protocol.protocol1_8to1_9.Protocol1_8TO1_9;
import de.gerrygames.viarewind.protocol.protocol1_8to1_9.metadata.MetadataRewriter;
import de.gerrygames.viarewind.replacement.EntityReplacement;
import us.myles.ViaVersion.api.PacketWrapper;
import us.myles.ViaVersion.api.data.ExternalJoinGameListener;
import us.myles.ViaVersion.api.data.StoredObject;
import us.myles.ViaVersion.api.data.UserConnection;
import us.myles.ViaVersion.api.entities.Entity1_10Types;
import us.myles.ViaVersion.api.minecraft.Vector;
import us.myles.ViaVersion.api.minecraft.metadata.Metadata;
import us.myles.ViaVersion.api.type.Type;
import us.myles.ViaVersion.api.type.types.version.Types1_8;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class EntityTracker extends StoredObject implements ExternalJoinGameListener {
	private final Map<Integer, List<Integer>> vehicleMap = new ConcurrentHashMap();
	private final Map<Integer, Entity1_10Types.EntityType> clientEntityTypes = new ConcurrentHashMap();
	private final Map<Integer, List<Metadata>> metadataBuffer = new ConcurrentHashMap();
	private final Map<Integer, EntityReplacement> entityReplacements = new ConcurrentHashMap<>();
	private final Map<Integer, Vector> entityOffsets = new ConcurrentHashMap<>();
	private int playerId;
	private int playerGamemode = 0;

	public EntityTracker(UserConnection user) {
		super(user);
	}

	public void setPlayerId(int entityId) {
		playerId = entityId;
	}

	public int getPlayerId() {
		return playerId;
	}

	public int getPlayerGamemode() {
		return playerGamemode;
	}

	public void setPlayerGamemode(int playerGamemode) {
		this.playerGamemode = playerGamemode;
	}

	public void removeEntity(int entityId) {
		vehicleMap.remove(entityId);
		vehicleMap.forEach((vehicle, passengers) -> passengers.remove((Integer)entityId));
		vehicleMap.entrySet().removeIf(entry -> entry.getValue().isEmpty());
		clientEntityTypes.remove(entityId);
		entityOffsets.remove(entityId);
		if (entityReplacements.containsKey(entityId)) {
			entityReplacements.remove(entityId).despawn();
		}
	}

	public void resetEntityOffset(int entityId) {
		entityOffsets.remove(entityId);
	}

	public Vector getEntityOffset(int entityId) {
		return entityOffsets.computeIfAbsent(entityId, key -> new Vector(0, 0, 0));
	}

	public void addToEntityOffset(int entityId, short relX, short relY, short relZ) {
		entityOffsets.compute(entityId, (key, offset) -> {
			if (offset == null) {
				return new Vector(relX, relY, relZ);
			} else {
				offset.setBlockX(offset.getBlockX() + relX);
				offset.setBlockY(offset.getBlockY() + relY);
				offset.setBlockZ(offset.getBlockZ() + relZ);
				return offset;
			}
		});
	}

	public void setEntityOffset(int entityId, short relX, short relY, short relZ) {
		entityOffsets.compute(entityId, (key, offset) -> {
			if (offset == null) {
				return new Vector(relX, relY, relZ);
			} else {
				offset.setBlockX(relX);
				offset.setBlockY(relY);
				offset.setBlockZ(relZ);
				return offset;
			}
		});
	}

	public void setEntityOffset(int entityId, Vector offset) {
		entityOffsets.put(entityId, offset);
	}

	public List<Integer> getPassengers(int entityId) {
		return vehicleMap.getOrDefault(entityId, new ArrayList<>());
	}

	public void setPassengers(int entityId, List<Integer> passengers) {
		vehicleMap.put(entityId, passengers);
	}

	public void addEntityReplacement(EntityReplacement entityReplacement) {
		entityReplacements.put(entityReplacement.getEntityId(), entityReplacement);
	}

	public EntityReplacement getEntityReplacement(int entityId) {
		return entityReplacements.get(entityId);
	}

	public Map<Integer, Entity1_10Types.EntityType> getClientEntityTypes() {
		return this.clientEntityTypes;
	}

	public void addMetadataToBuffer(int entityID, List<Metadata> metadataList) {
		if (this.metadataBuffer.containsKey(entityID)) {
			this.metadataBuffer.get(entityID).addAll(metadataList);
		} else if (!metadataList.isEmpty()) {
			this.metadataBuffer.put(entityID, metadataList);
		}
	}

	public List<Metadata> getBufferedMetadata(int entityId) {
		return metadataBuffer.get(entityId);
	}

	public boolean isInsideVehicle(int entityId) {
		for (List<Integer> vehicle : vehicleMap.values()) {
			if (vehicle.contains(entityId)) return true;
		}
		return false;
	}

	public int getVehicle(int passenger) {
		for (Map.Entry<Integer, List<Integer>> vehicle : vehicleMap.entrySet()) {
			if (vehicle.getValue().contains(passenger)) return vehicle.getKey();
		}
		return -1;
	}

	public boolean isPassenger(int vehicle, int passenger) {
		return vehicleMap.containsKey(vehicle) && vehicleMap.get(vehicle).contains(passenger);
	}

	public void sendMetadataBuffer(int entityId) {
		if (!this.metadataBuffer.containsKey(entityId)) return;
		if (entityReplacements.containsKey(entityId)) {
			entityReplacements.get(entityId).updateMetadata(this.metadataBuffer.remove(entityId));
		} else {
			PacketWrapper wrapper = new PacketWrapper(0x1C, null, this.getUser());
			wrapper.write(Type.VAR_INT, entityId);
			wrapper.write(Types1_8.METADATA_LIST, this.metadataBuffer.get(entityId));
			MetadataRewriter.transform(this.getClientEntityTypes().get(entityId), this.metadataBuffer.get(entityId));
			if (!this.metadataBuffer.get(entityId).isEmpty()) {
				try {
					wrapper.send(Protocol1_8TO1_9.class);
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}

			this.metadataBuffer.remove(entityId);
		}
	}

	@Override
	public void onExternalJoinGame(int playerEntityId) {
		clientEntityTypes.remove(this.playerId);
		this.playerId = playerEntityId;
		clientEntityTypes.put(this.playerId, Entity1_10Types.EntityType.ENTITY_HUMAN);
	}
}
