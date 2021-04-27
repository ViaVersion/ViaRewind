package de.gerrygames.viarewind.protocol.protocol1_8to1_9.storage;

import lombok.Data;
import com.viaversion.viaversion.api.connection.StoredObject;
import com.viaversion.viaversion.api.connection.UserConnection;

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

	public void setYaw(float yaw) {
		this.yaw = yaw % 360f;
	}

	public void setPitch(float pitch) {
		this.pitch = pitch % 360f;
	}
}
