package de.gerrygames.viarewind.utils;

import us.myles.ViaVersion.api.Via;

public class Ticker {
	private static boolean init = false;

	public static void init() {
		if (init) return;
		synchronized (Ticker.class) {
			if (init) return;
			init = true;
		}
		Via.getPlatform().runRepeatingSync(() -> {
			Via.getManager().getPortedPlayers().values().forEach(user -> {
				user.getStoredObjects().values().stream().filter(Tickable.class::isInstance).map(Tickable.class::cast).forEach(Tickable::tick);
			});
		}, 1L);
	}
}
