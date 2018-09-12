package de.gerrygames.viarewind.protocol.protocol1_8to1_9.storage;

import de.gerrygames.viarewind.utils.Tickable;
import de.gerrygames.viarewind.protocol.protocol1_8to1_9.Protocol1_8TO1_9;
import de.gerrygames.viarewind.utils.PacketUtil;
import us.myles.ViaVersion.api.PacketWrapper;
import us.myles.ViaVersion.api.data.StoredObject;
import us.myles.ViaVersion.api.data.UserConnection;
import us.myles.ViaVersion.api.type.Type;

public class Levitation extends StoredObject implements Tickable {
	private int amplifier;
	private volatile boolean active = false;

	public Levitation(UserConnection user) {
		super(user);
	}

	@Override
	public void tick() {
		if (!active) {
			return;
		}

		int vY = (amplifier+1) * 360;
		PacketWrapper packet = new PacketWrapper(0x12, null, Levitation.this.getUser());
		packet.write(Type.VAR_INT, getUser().get(EntityTracker.class).getPlayerId());
		packet.write(Type.SHORT, (short)0);
		packet.write(Type.SHORT, (short)vY);
		packet.write(Type.SHORT, (short)0);
		PacketUtil.sendPacket(packet, Protocol1_8TO1_9.class);
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public void setAmplifier(int amplifier) {
		this.amplifier = amplifier;
	}
}
