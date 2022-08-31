package de.gerrygames.viarewind.protocol.protocol1_7_6_10to1_8.storage;

import com.viaversion.viaversion.api.connection.StoredObject;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.type.Type;
import de.gerrygames.viarewind.protocol.protocol1_7_6_10to1_8.Protocol1_7_6_10TO1_8;
import de.gerrygames.viarewind.utils.PacketUtil;
import de.gerrygames.viarewind.utils.Tickable;

public class WorldBorder extends StoredObject implements Tickable {
	private double x, z;
	private double oldDiameter, newDiameter;
	private long lerpTime;
	private long lerpStartTime;
	private int portalTeleportBoundary;
	private int warningTime, warningBlocks;
	private boolean init = false;

	private final int VIEW_DISTANCE = 16;

	public WorldBorder(UserConnection user) {
		super(user);
	}

	@Override
	public void tick() {
		if (!isInit()) {
			return;
		}

		sendPackets();
	}

	private enum Side {
		NORTH(0, -1),
		EAST(1, 0),
		SOUTH(0, 1),
		WEST(-1, 0),
		;

		private final int modX;
		private final int modZ;

		Side(int modX, int modZ) {
			this.modX = modX;
			this.modZ = modZ;
		}
	}

	private void sendPackets() {
		PlayerPosition position = getUser().get(PlayerPosition.class);

		double radius = getSize() / 2.0;

		for (Side side : Side.values()) {
			double d;
			double pos;
			double center;
			if (side.modX!=0) {
				pos = position.getPosZ();
				center = z;
				d = Math.abs(x + radius * side.modX - position.getPosX());
			} else {
				center = x;
				pos = position.getPosX();
				d = Math.abs(z + radius * side.modZ - position.getPosZ());
			}
			if (d >= VIEW_DISTANCE) continue;

			double r = Math.sqrt(VIEW_DISTANCE * VIEW_DISTANCE - d * d);

			double minH = Math.ceil(pos - r);
			double maxH = Math.floor(pos + r);
			double minV = Math.ceil(position.getPosY() - r);
			double maxV = Math.floor(position.getPosY() + r);

			if (minH<center-radius) minH = Math.ceil(center-radius);
			if (maxH>center+radius) maxH = Math.floor(center+radius);
			if (minV<0.0) minV = 0.0;

			double centerH = (minH+maxH) / 2.0;
			double centerV = (minV+maxV) / 2.0;

			int a = (int) Math.floor((maxH-minH) * (maxV-minV) * 0.5);

			double b = 2.5;

			PacketWrapper particles = PacketWrapper.create(0x2A, null, getUser());
			particles.write(Type.STRING, "fireworksSpark");
			particles.write(Type.FLOAT, (float)(side.modX!=0 ? x + (radius * side.modX) : centerH));
			particles.write(Type.FLOAT, (float)centerV);
			particles.write(Type.FLOAT, (float)(side.modX==0 ? z + (radius * side.modZ) : centerH));
			particles.write(Type.FLOAT, (float)(side.modX!=0 ? 0f : (maxH-minH) / b));
			particles.write(Type.FLOAT, (float)((maxV-minV) / b));
			particles.write(Type.FLOAT, (float)(side.modX==0 ? 0f : (maxH-minH) / b));
			particles.write(Type.FLOAT, 0f);
			particles.write(Type.INT, a);

			PacketUtil.sendPacket(particles, Protocol1_7_6_10TO1_8.class, true, true);
		}
	}

	private boolean isInit() {
		return init;
	}

	public void init(double x, double z, double oldDiameter, double newDiameter, long lerpTime, int portalTeleportBoundary, int warningTime, int warningBlocks) {
		this.x = x;
		this.z = z;
		this.oldDiameter = oldDiameter;
		this.newDiameter = newDiameter;
		this.lerpTime = lerpTime;
		this.portalTeleportBoundary = portalTeleportBoundary;
		this.warningTime = warningTime;
		this.warningBlocks = warningBlocks;
		init = true;
	}

	public double getX() {
		return x;
	}

	public double getZ() {
		return z;
	}

	public void setCenter(double x, double z) {
		this.x = x;
		this.z = z;
	}

	public double getOldDiameter() {
		return oldDiameter;
	}

	public double getNewDiameter() {
		return newDiameter;
	}

	public long getLerpTime() {
		return lerpTime;
	}

	public void lerpSize(double oldDiameter, double newDiameter, long lerpTime) {
		this.oldDiameter = oldDiameter;
		this.newDiameter = newDiameter;
		this.lerpTime = lerpTime;
		this.lerpStartTime = System.currentTimeMillis();
	}

	public void setSize(double size) {
		this.oldDiameter = size;
		this.newDiameter = size;
		this.lerpTime = 0;
	}

	public double getSize() {
		if (lerpTime==0) return newDiameter;

		long time = System.currentTimeMillis() - lerpStartTime;
		double percent = ((double)(time) / (double)(lerpTime));
		if (percent>1.0d) percent = 1.0d;
		else if (percent<0.0d) percent = 0.0d;

		return oldDiameter + (newDiameter-oldDiameter) * percent;
	}

	public int getPortalTeleportBoundary() {
		return portalTeleportBoundary;
	}

	public void setPortalTeleportBoundary(int portalTeleportBoundary) {
		this.portalTeleportBoundary = portalTeleportBoundary;
	}

	public int getWarningTime() {
		return warningTime;
	}

	public void setWarningTime(int warningTime) {
		this.warningTime = warningTime;
	}

	public int getWarningBlocks() {
		return warningBlocks;
	}

	public void setWarningBlocks(int warningBlocks) {
		this.warningBlocks = warningBlocks;
	}
}
