package com.viaversion.viarewind.protocol.protocol1_7_6_10to1_8.task;

import com.viaversion.viarewind.protocol.protocol1_7_6_10to1_8.Protocol1_7_6_10To1_8;
import com.viaversion.viarewind.protocol.protocol1_7_6_10to1_8.storage.PlayerPositionTracker;
import com.viaversion.viarewind.protocol.protocol1_7_6_10to1_8.storage.WorldBorder;
import com.viaversion.viarewind.utils.PacketUtil;
import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.type.Type;

public class WorldBorderUpdateTask implements Runnable {
	public final static int VIEW_DISTANCE = 16;

	@Override
	public void run() {
		for (UserConnection connection : Via.getManager().getConnectionManager().getConnections()) {
			final WorldBorder worldBorderTracker = connection.get(WorldBorder.class);
			if (!worldBorderTracker.isInit()) continue;

			final PlayerPositionTracker position = connection.get(PlayerPositionTracker.class);

			double radius = worldBorderTracker.getSize() / 2.0;

			for (WorldBorder.Side side : WorldBorder.Side.values()) {
				double d;
				double pos;
				double center;
				if (side.modX != 0) {
					pos = position.getPosZ();
					center = worldBorderTracker.getZ();
					d = Math.abs(worldBorderTracker.getX() + radius * side.modX - position.getPosX());
				} else {
					center = worldBorderTracker.getX();
					pos = position.getPosX();
					d = Math.abs(worldBorderTracker.getZ() + radius * side.modZ - position.getPosZ());
				}
				if (d >= VIEW_DISTANCE) continue;

				double r = Math.sqrt(VIEW_DISTANCE * VIEW_DISTANCE - d * d);

				double minH = Math.ceil(pos - r);
				double maxH = Math.floor(pos + r);
				double minV = Math.ceil(position.getPosY() - r);
				double maxV = Math.floor(position.getPosY() + r);

				if (minH < center - radius) minH = Math.ceil(center - radius);
				if (maxH > center + radius) maxH = Math.floor(center + radius);
				if (minV < 0.0) minV = 0.0;

				double centerH = (minH + maxH) / 2.0;
				double centerV = (minV + maxV) / 2.0;

				int a = (int) Math.floor((maxH - minH) * (maxV - minV) * 0.5);

				double b = 2.5;

				PacketWrapper particles = PacketWrapper.create(0x2A, null, connection);
				particles.write(Type.STRING, "fireworksSpark");
				particles.write(Type.FLOAT, (float) (side.modX != 0 ? worldBorderTracker.getX() + (radius * side.modX) : centerH));
				particles.write(Type.FLOAT, (float) centerV);
				particles.write(Type.FLOAT, (float) (side.modX == 0 ? worldBorderTracker.getZ() + (radius * side.modZ) : centerH));
				particles.write(Type.FLOAT, (float) (side.modX != 0 ? 0f : (maxH - minH) / b));
				particles.write(Type.FLOAT, (float) ((maxV - minV) / b));
				particles.write(Type.FLOAT, (float) (side.modX == 0 ? 0f : (maxH - minH) / b));
				particles.write(Type.FLOAT, 0f);
				particles.write(Type.INT, a);

				PacketUtil.sendPacket(particles, Protocol1_7_6_10To1_8.class, true, true);
			}
		}
	}
}
