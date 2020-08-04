package de.gerrygames.viarewind.protocol.protocol1_7_6_10to1_8.storage;

import us.myles.ViaVersion.api.data.StoredObject;
import us.myles.ViaVersion.api.data.UserConnection;

public class PlayerAbilities extends StoredObject {
	private boolean sprinting, allowFly, flying, invincible, creative;
	private float flySpeed, walkSpeed;

	public PlayerAbilities(UserConnection user) {
		super(user);
	}

	public byte getFlags() {
		byte flags = 0;
		if (invincible) flags |= 8;
		if (allowFly) flags |= 4;
		if (flying) flags |= 2;
		if (creative) flags |= 1;
		return flags;
	}

	public boolean isSprinting() {
		return sprinting;
	}

	public void setSprinting(boolean sprinting) {
		this.sprinting = sprinting;
	}

	public boolean isAllowFly() {
		return allowFly;
	}

	public void setAllowFly(boolean allowFly) {
		this.allowFly = allowFly;
	}

	public boolean isFlying() {
		return flying;
	}

	public void setFlying(boolean flying) {
		this.flying = flying;
	}

	public boolean isInvincible() {
		return invincible;
	}

	public void setInvincible(boolean invincible) {
		this.invincible = invincible;
	}

	public boolean isCreative() {
		return creative;
	}

	public void setCreative(boolean creative) {
		this.creative = creative;
	}

	public float getFlySpeed() {
		return flySpeed;
	}

	public void setFlySpeed(float flySpeed) {
		this.flySpeed = flySpeed;
	}

	public float getWalkSpeed() {
		return walkSpeed;
	}

	public void setWalkSpeed(float walkSpeed) {
		this.walkSpeed = walkSpeed;
	}
}
