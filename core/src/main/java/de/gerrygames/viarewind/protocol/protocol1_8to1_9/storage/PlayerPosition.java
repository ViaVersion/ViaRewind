package de.gerrygames.viarewind.protocol.protocol1_8to1_9.storage;

import com.viaversion.viaversion.api.connection.StoredObject;
import com.viaversion.viaversion.api.connection.UserConnection;

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

	public double getPosX() {
		return this.posX;
	}

	public double getPosY() {
		return this.posY;
	}

	public double getPosZ() {
		return this.posZ;
	}

	public float getYaw() {
		return this.yaw;
	}

	public float getPitch() {
		return this.pitch;
	}

	public boolean isOnGround() {
		return this.onGround;
	}

	public int getConfirmId() {
		return this.confirmId;
	}

	public void setPosX(double posX) {
		this.posX = posX;
	}

	public void setPosY(double posY) {
		this.posY = posY;
	}

	public void setPosZ(double posZ) {
		this.posZ = posZ;
	}

	public void setOnGround(boolean onGround) {
		this.onGround = onGround;
	}

	public void setConfirmId(int confirmId) {
		this.confirmId = confirmId;
	}

	public boolean equals(final Object o) {
		if (o == this) return true;
		if (!(o instanceof PlayerPosition)) return false;
		final PlayerPosition other = (PlayerPosition) o;
		if (!other.canEqual(this)) return false;
		if (Double.compare(this.getPosX(), other.getPosX()) != 0) return false;
		if (Double.compare(this.getPosY(), other.getPosY()) != 0) return false;
		if (Double.compare(this.getPosZ(), other.getPosZ()) != 0) return false;
		if (Float.compare(this.getYaw(), other.getYaw()) != 0) return false;
		if (Float.compare(this.getPitch(), other.getPitch()) != 0) return false;
		if (this.isOnGround() != other.isOnGround()) return false;
		return this.getConfirmId() == other.getConfirmId();
	}

	protected boolean canEqual(final Object other) {
		return other instanceof PlayerPosition;
	}

	public int hashCode() {
		final int PRIME = 59;
		int result = 1;
		final long $posX = Double.doubleToLongBits(this.getPosX());
		result = result * PRIME + (int) ($posX >>> 32 ^ $posX);
		final long $posY = Double.doubleToLongBits(this.getPosY());
		result = result * PRIME + (int) ($posY >>> 32 ^ $posY);
		final long $posZ = Double.doubleToLongBits(this.getPosZ());
		result = result * PRIME + (int) ($posZ >>> 32 ^ $posZ);
		result = result * PRIME + Float.floatToIntBits(this.getYaw());
		result = result * PRIME + Float.floatToIntBits(this.getPitch());
		result = result * PRIME + (this.isOnGround() ? 79 : 97);
		result = result * PRIME + this.getConfirmId();
		return result;
	}

	public String toString() {
		return "PlayerPosition(posX=" + this.getPosX() + ", posY=" + this.getPosY() + ", posZ=" + this.getPosZ() + ", yaw=" + this.getYaw() + ", pitch=" + this.getPitch() + ", onGround=" + this.isOnGround() + ", confirmId=" + this.getConfirmId() + ")";
	}
}
