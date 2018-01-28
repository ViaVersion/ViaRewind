package de.gerrygames.viarewind.protocol.protocol1_7_6_10to1_8.storage;

import lombok.Data;
import us.myles.ViaVersion.api.data.StoredObject;
import us.myles.ViaVersion.api.data.UserConnection;

@Data
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
}
