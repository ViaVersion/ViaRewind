package com.viaversion.viarewind.protocol.protocol1_8to1_9.storage;

import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.minecraft.Vector;
import com.viaversion.viaversion.api.minecraft.entities.EntityTypes1_10;
import com.viaversion.viaversion.data.entity.EntityTrackerBase;
import com.viaversion.viaversion.libs.fastutil.ints.*;

import java.util.List;
import java.util.Map;

public class EntityTracker1_9 extends EntityTrackerBase {

	private final Int2ObjectMap<IntList> vehicles = new Int2ObjectOpenHashMap<>();
	private final Int2ObjectMap<Vector> offsets = new Int2ObjectOpenHashMap<>();
	private final Int2IntMap status = new Int2IntOpenHashMap();

	private int clientEntityGameMode;

	public EntityTracker1_9(UserConnection connection) {
		super(connection, EntityTypes1_10.EntityType.PLAYER);
	}

	@Override
	public void removeEntity(int id) {
		vehicles.remove(id);
		offsets.remove(id);
		status.remove(id);

		vehicles.forEach((vehicle, passengers) -> passengers.removeInt(id));
		vehicles.int2ObjectEntrySet().removeIf(entry -> entry.getValue().isEmpty());
		super.removeEntity(id);
	}

	public void resetEntityOffset(final int id) {
		offsets.remove(id);
	}

	public Vector getEntityOffset(final int id) {
		return offsets.get(id);
	}

	public void setEntityOffset(final int id, Vector offset) {
		offsets.put(id, offset);
	}

	public List<Integer> getPassengers(final int id) {
		return vehicles.getOrDefault(id, new IntArrayList());
	}

	public void setPassengers(final int id, final IntList passengers) {
		vehicles.put(id, passengers);
	}

	public boolean isInsideVehicle(final int id) {
		for (List<Integer> vehicle : vehicles.values()) {
			if (vehicle.contains(id)) return true;
		}
		return false;
	}

	public int getVehicle(final int passenger) {
		for (Map.Entry<Integer, IntList> vehicle : vehicles.int2ObjectEntrySet()) {
			if (vehicle.getValue().contains(passenger)) return vehicle.getKey();
		}
		return -1;
	}

	public boolean isPassenger(final int vehicle, final int passenger) {
		return vehicles.containsKey(vehicle) && vehicles.get(vehicle).contains(passenger);
	}

	public Int2IntMap getStatus() {
		return status;
	}

	public int getClientEntityGameMode() {
		return clientEntityGameMode;
	}

	public void setClientEntityGameMode(int clientEntityGameMode) {
		this.clientEntityGameMode = clientEntityGameMode;
	}
}
