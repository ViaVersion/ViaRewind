package de.gerrygames.viarewind.protocol.protocol1_7_6_10to1_8.storage;

import de.gerrygames.viarewind.protocol.protocol1_7_6_10to1_8.Protocol1_7_6_10TO1_8;
import de.gerrygames.viarewind.protocol.protocol1_7_6_10to1_8.metadata.MetadataRewriter;
import de.gerrygames.viarewind.replacement.EntityReplacement;
import de.gerrygames.viarewind.utils.PacketUtil;
import us.myles.ViaVersion.api.PacketWrapper;
import us.myles.ViaVersion.api.data.ExternalJoinGameListener;
import us.myles.ViaVersion.api.data.StoredObject;
import us.myles.ViaVersion.api.data.UserConnection;
import us.myles.ViaVersion.api.entities.Entity1_10Types;
import us.myles.ViaVersion.api.minecraft.item.Item;
import us.myles.ViaVersion.api.minecraft.metadata.Metadata;
import us.myles.ViaVersion.api.type.Type;
import us.myles.ViaVersion.api.type.types.version.Types1_8;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class EntityTracker extends StoredObject implements ExternalJoinGameListener {
	private final Map<Integer, Entity1_10Types.EntityType> clientEntityTypes = new ConcurrentHashMap();
	private final Map<Integer, List<Metadata>> metadataBuffer = new ConcurrentHashMap();
	private final Map<Integer, Integer> vehicles = new ConcurrentHashMap<>();
	private final Map<Integer, EntityReplacement> entityReplacements = new ConcurrentHashMap<>();
	private final Map<Integer, UUID> playersByEntityId = new HashMap<>();
	private final Map<UUID, Integer> playersByUniqueId = new HashMap<>();
	private final Map<UUID, Item[]> playerEquipment = new HashMap<>();
	private int gamemode = 0;
	private int playerId = -1;
	private int spectating = -1;
	private int dimension = 0;

	public EntityTracker(UserConnection user) {
		super(user);
	}

	public void removeEntity(int entityId) {
		clientEntityTypes.remove(entityId);
		if (entityReplacements.containsKey(entityId)) {
			entityReplacements.remove(entityId).despawn();
		}
		if (playersByEntityId.containsKey(entityId)) {
			playersByUniqueId.remove(playersByEntityId.remove(entityId));
		}
	}

	public void addPlayer(Integer entityId, UUID uuid) {
		playersByUniqueId.put(uuid, entityId);
		playersByEntityId.put(entityId, uuid);
	}

	public UUID getPlayerUUID(int entityId) {
		return playersByEntityId.get(entityId);
	}

	public int getPlayerEntityId(UUID uuid) {
		return playersByUniqueId.getOrDefault(uuid, -1);
	}

	public Item[] getPlayerEquipment(UUID uuid) {
		return playerEquipment.get(uuid);
	}

	public void setPlayerEquipment(UUID uuid, Item[] equipment) {
		playerEquipment.put(uuid, equipment);
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

	public void addEntityReplacement(EntityReplacement entityReplacement) {
		entityReplacements.put(entityReplacement.getEntityId(), entityReplacement);
	}

	public EntityReplacement getEntityReplacement(int entityId) {
		return entityReplacements.get(entityId);
	}

	public List<Metadata> getBufferedMetadata(int entityId) {
		return metadataBuffer.get(entityId);
	}

	public void sendMetadataBuffer(int entityId) {
		if (!this.metadataBuffer.containsKey(entityId)) return;
		if (entityReplacements.containsKey(entityId)) {
			entityReplacements.get(entityId).updateMetadata(this.metadataBuffer.remove(entityId));
		} else {
			Entity1_10Types.EntityType type = this.getClientEntityTypes().get(entityId);
			PacketWrapper wrapper = new PacketWrapper(0x1C, null, this.getUser());
			wrapper.write(Type.VAR_INT, entityId);
			wrapper.write(Types1_8.METADATA_LIST, this.metadataBuffer.get(entityId));
			MetadataRewriter.transform(type, this.metadataBuffer.get(entityId));
			if (!this.metadataBuffer.get(entityId).isEmpty()) {
				PacketUtil.sendPacket(wrapper, Protocol1_7_6_10TO1_8.class);
			}

			this.metadataBuffer.remove(entityId);
		}
	}

	public int getVehicle(int passengerId) {
		for (Map.Entry<Integer, Integer> vehicle : vehicles.entrySet()) {
			if (vehicle.getValue()==passengerId) return vehicle.getValue();
		}
		return -1;
	}

	public int getPassenger(int vehicleId) {
		return vehicles.getOrDefault(vehicleId, -1);
	}

	public void setPassenger(int vehicleId, int passengerId) {
		if (vehicleId==this.spectating && this.spectating!=this.playerId) {
			try {
				PacketWrapper sneakPacket = new PacketWrapper(0x0B, null, getUser());
				sneakPacket.write(Type.VAR_INT, playerId);
				sneakPacket.write(Type.VAR_INT, 0);  //Start sneaking
				sneakPacket.write(Type.VAR_INT, 0);  //Action Parameter

				PacketWrapper unsneakPacket = new PacketWrapper(0x0B, null, getUser());
				unsneakPacket.write(Type.VAR_INT, playerId);
				unsneakPacket.write(Type.VAR_INT, 1);  //Stop sneaking
				unsneakPacket.write(Type.VAR_INT, 0);  //Action Parameter

				PacketUtil.sendToServer(sneakPacket, Protocol1_7_6_10TO1_8.class, true, true);

				setSpectating(playerId);
			} catch (Exception ex) {ex.printStackTrace();}
		}
		if (vehicleId==-1) {
			int oldVehicleId = getVehicle(passengerId);
			vehicles.remove(oldVehicleId);
		} else if (passengerId==-1) {
			vehicles.remove(vehicleId);
		} else {
			vehicles.put(vehicleId, passengerId);
		}
	}

	public int getSpectating() {
		return spectating;
	}

	public boolean setSpectating(int spectating) {
		if (spectating!=this.playerId && getPassenger(spectating)!=-1) {

			PacketWrapper sneakPacket = new PacketWrapper(0x0B, null, getUser());
			sneakPacket.write(Type.VAR_INT, playerId);
			sneakPacket.write(Type.VAR_INT, 0);  //Start sneaking
			sneakPacket.write(Type.VAR_INT, 0);  //Action Parameter

			PacketWrapper unsneakPacket = new PacketWrapper(0x0B, null, getUser());
			unsneakPacket.write(Type.VAR_INT, playerId);
			unsneakPacket.write(Type.VAR_INT, 1);  //Stop sneaking
			unsneakPacket.write(Type.VAR_INT, 0);  //Action Parameter

			PacketUtil.sendToServer(sneakPacket, Protocol1_7_6_10TO1_8.class, true, true);

			setSpectating(this.playerId);
			return false;  //Entity has Passenger
		}

		if (this.spectating!=spectating && this.spectating!=this.playerId) {
			PacketWrapper unmount = new PacketWrapper(0x1B, null, this.getUser());
			unmount.write(Type.INT, this.playerId);
			unmount.write(Type.INT, -1);
			unmount.write(Type.BOOLEAN, false);
			PacketUtil.sendPacket(unmount, Protocol1_7_6_10TO1_8.class);
		}
		this.spectating = spectating;
		if (spectating!=this.playerId) {
			PacketWrapper mount = new PacketWrapper(0x1B, null, this.getUser());
			mount.write(Type.INT, this.playerId);
			mount.write(Type.INT, this.spectating);
			mount.write(Type.BOOLEAN, false);
			PacketUtil.sendPacket(mount, Protocol1_7_6_10TO1_8.class);
		}
		return true;
	}

	public int getGamemode() {
		return gamemode;
	}

	public void setGamemode(int gamemode) {
		this.gamemode = gamemode;
	}

	public int getPlayerId() {
		return playerId;
	}

	public void setPlayerId(int playerId) {
		if (this.playerId!=-1) throw new IllegalStateException("playerId was already set!");
		this.playerId = this.spectating = playerId;
	}

	public void clearEntities() {
		clientEntityTypes.clear();
		entityReplacements.clear();
		vehicles.clear();
		metadataBuffer.clear();
	}

	public int getDimension() {
		return dimension;
	}

	public void setDimension(int dimension) {
		this.dimension = dimension;
	}

	@Override
	public void onExternalJoinGame(int playerEntityId) {
		if (this.spectating == this.playerId) {
			this.spectating = playerEntityId;
		}
		clientEntityTypes.remove(this.playerId);
		this.playerId = playerEntityId;
		clientEntityTypes.put(this.playerId, Entity1_10Types.EntityType.ENTITY_HUMAN);
	}
}
