package de.gerrygames.viarewind;

import de.gerrygames.viarewind.api.ViaRewindPlatform;
import lombok.Getter;

public class ViaRewind {
	@Getter
	private static ViaRewindPlatform platform;

	public static void init(ViaRewindPlatform platform) {
		ViaRewind.platform = platform;
	}
}
