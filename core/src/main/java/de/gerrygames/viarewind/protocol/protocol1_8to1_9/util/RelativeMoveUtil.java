package de.gerrygames.viarewind.protocol.protocol1_8to1_9.util;

import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.minecraft.Vector;
import de.gerrygames.viarewind.protocol.protocol1_8to1_9.storage.EntityTracker;

public class RelativeMoveUtil {

	public static Vector[] calculateRelativeMoves(UserConnection user, int entityId, int relX, int relY, int relZ) {
		EntityTracker tracker = user.get(EntityTracker.class);

		int x;
		int y;
		int z;
		Vector offset = tracker.getEntityOffset(entityId);
		if (offset != null) {
			relX += offset.blockX();
			relY += offset.blockY();
			relZ += offset.blockZ();
		}

		if (relX > Short.MAX_VALUE) {
			x = relX - Short.MAX_VALUE;
			relX = Short.MAX_VALUE;
		} else if (relX < Short.MIN_VALUE) {
			x = relX - Short.MIN_VALUE;
			relX = Short.MIN_VALUE;
		} else {
			x = 0;
		}

		if (relY > Short.MAX_VALUE) {
			y = relY - Short.MAX_VALUE;
			relY = Short.MAX_VALUE;
		} else if (relY < Short.MIN_VALUE) {
			y = relY - Short.MIN_VALUE;
			relY = Short.MIN_VALUE;
		} else {
			y = 0;
		}

		if (relZ > Short.MAX_VALUE) {
			z = relZ - Short.MAX_VALUE;
			relZ = Short.MAX_VALUE;
		} else if (relZ < Short.MIN_VALUE) {
			z = relZ - Short.MIN_VALUE;
			relZ = Short.MIN_VALUE;
		} else {
			z = 0;
		}

		int sentRelX, sentRelY, sentRelZ;
		Vector[] moves;

		if (relX > Byte.MAX_VALUE * 128 || relX < Byte.MIN_VALUE * 128 || relY > Byte.MAX_VALUE * 128 || relY < Byte.MIN_VALUE * 128 || relZ > Byte.MAX_VALUE * 128 || relZ < Byte.MIN_VALUE * 128) {
			byte relX1 = (byte) (relX / 256);
			byte relX2 = (byte) (Math.round((relX - relX1 * 128) / 128f));
			byte relY1 = (byte) (relY / 256);
			byte relY2 = (byte) (Math.round((relY - relY1 * 128) / 128f));
			byte relZ1 = (byte) (relZ / 256);
			byte relZ2 = (byte) (Math.round((relZ - relZ1 * 128) / 128f));

			sentRelX = relX1 + relX2;
			sentRelY = relY1 + relY2;
			sentRelZ = relZ1 + relZ2;

			moves = new Vector[] {new Vector(relX1, relY1, relZ1), new Vector(relX2, relY2, relZ2)};
		} else {
			sentRelX = Math.round(relX / 128f);
			sentRelY = Math.round(relY / 128f);
			sentRelZ = Math.round(relZ / 128f);

			moves = new Vector[] {new Vector(sentRelX, sentRelY, sentRelZ)};
		}

		x = x + relX - sentRelX * 128;
		y = y + relY - sentRelY * 128;
		z = z + relZ - sentRelZ * 128;

		tracker.setEntityOffset(entityId, new Vector(x, y, z));

		return moves;
	}
}
