package de.gerrygames.viarewind.protocol.protocol1_8to1_9.storage;

import us.myles.ViaVersion.api.data.StoredObject;
import us.myles.ViaVersion.api.data.UserConnection;

public class PlayerPosition extends StoredObject {
	private double posX, posY, posZ;
	private float yaw, pitch;
	private boolean onGround;

	public PlayerPosition(UserConnection user) {
		super(user);
	}

	public void setPos(double x, double y, double z) {
		posX = x;
		posY = y;
		posZ = z;
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

	public float getYaw() {
		return yaw;
	}

	public void setYaw(float yaw) {
		this.yaw = yaw;
	}

	public float getPitch() {
		return pitch;
	}

	public void setPitch(float pitch) {
		this.pitch = pitch;
	}

	public boolean isOnGround() {
		return onGround;
	}

	public void setOnGround(boolean onGround) {
		this.onGround = onGround;
	}
}
