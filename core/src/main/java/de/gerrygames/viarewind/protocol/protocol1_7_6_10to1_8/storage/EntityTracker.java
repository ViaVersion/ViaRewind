package de.gerrygames.viarewind.protocol.protocol1_7_6_10to1_8.storage;

import de.gerrygames.viarewind.protocol.protocol1_7_6_10to1_8.Protocol1_7_6_10TO1_8;
import de.gerrygames.viarewind.protocol.protocol1_7_6_10to1_8.metadata.MetadataRewriter;
import de.gerrygames.viarewind.utils.PacketUtil;
import us.myles.ViaVersion.api.PacketWrapper;
import us.myles.ViaVersion.api.data.StoredObject;
import us.myles.ViaVersion.api.data.UserConnection;
import us.myles.ViaVersion.api.entities.Entity1_10Types;
import us.myles.ViaVersion.api.minecraft.metadata.Metadata;
import us.myles.ViaVersion.api.type.Type;
import us.myles.ViaVersion.api.type.types.version.Types1_8;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class EntityTracker extends StoredObject {
	private final Map<Integer, Entity1_10Types.EntityType> clientEntityTypes = new ConcurrentHashMap();
	private final Map<Integer, List<Metadata>> metadataBuffer = new ConcurrentHashMap();
	private final Map<Integer, Integer> vehicles = new ConcurrentHashMap<>();
	private int gamemode = 0;
	private int playerId = -1;
	private int spectating = -1;

	public EntityTracker(UserConnection user) {
		super(user);
	}

	public void removeEntity(int entityId) {
		clientEntityTypes.remove(entityId);
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

	public void sendMetadataBuffer(int entityId) {
		if (!this.metadataBuffer.containsKey(entityId)) return;
		PacketWrapper wrapper = new PacketWrapper(0x1C, null, this.getUser());
		wrapper.write(Type.VAR_INT, entityId);
		wrapper.write(Types1_8.METADATA_LIST, this.metadataBuffer.get(entityId));
		MetadataRewriter.transform(this.getClientEntityTypes().get(entityId), this.metadataBuffer.get(entityId));
		if (!this.metadataBuffer.get(entityId).isEmpty()) {
			try {
				wrapper.send(Protocol1_7_6_10TO1_8.class);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}

		this.metadataBuffer.remove(entityId);
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
				//PacketUtil.sendToServer(unsneakPacket, Protocol1_7_6_10TO1_8.class, true, false);

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

	public boolean setSpectating(int spectating) throws Exception {
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
			//PacketUtil.sendToServer(unsneakPacket, Protocol1_7_6_10TO1_8.class, true, false);

			setSpectating(this.playerId);
			return false;  //Entity has Passenger
		}

		if (this.spectating!=spectating && this.spectating!=this.playerId) {
			PacketWrapper unmount = new PacketWrapper(0x1B, null, this.getUser());
			unmount.write(Type.INT, this.playerId);
			unmount.write(Type.INT, -1);
			unmount.write(Type.BOOLEAN, false);
			unmount.send(Protocol1_7_6_10TO1_8.class, true, false);
		}
		this.spectating = spectating;
		if (spectating!=this.playerId) {
			PacketWrapper mount = new PacketWrapper(0x1B, null, this.getUser());
			mount.write(Type.INT, this.playerId);
			mount.write(Type.INT, this.spectating);
			mount.write(Type.BOOLEAN, false);
			mount.send(Protocol1_7_6_10TO1_8.class, true, false);
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
}
