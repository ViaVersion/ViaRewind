package de.gerrygames.viarewind.protocol.protocol1_8to1_9.storage;

import com.viaversion.viaversion.api.connection.StoredObject;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.type.Type;
import de.gerrygames.viarewind.protocol.protocol1_8to1_9.Protocol1_8TO1_9;
import de.gerrygames.viarewind.utils.PacketUtil;
import de.gerrygames.viarewind.utils.Tickable;

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
		PacketWrapper packet = PacketWrapper.create(0x12, null, Levitation.this.getUser());
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
