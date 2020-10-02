package de.gerrygames.viarewind.protocol.protocol1_8to1_9.storage;

import lombok.Data;
import us.myles.ViaVersion.api.data.StoredObject;
import us.myles.ViaVersion.api.data.UserConnection;

@Data
public class PlayerPosition extends StoredObject {
	private double posX, posY, posZ;
	private float yaw, pitch;
	private boolean onGround;
	private int confirmId = -1;

	public PlayerPosition(UserConnection user) {
		super(user);
	}

	public void setPos(double x, double y, double z) {
		this.posX = x;
		this.posY = y;
		this.posZ = z;
	}
}
