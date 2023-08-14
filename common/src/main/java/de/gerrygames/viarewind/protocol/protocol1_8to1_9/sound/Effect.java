package de.gerrygames.viarewind.protocol.protocol1_8to1_9.sound;

import java.util.HashMap;

public class Effect {
	private static final HashMap<Integer, Integer> effects = new HashMap<>();

	public static int getOldId(int id) {
		return effects.getOrDefault(id, id);
	}

	static {
		effects.put(1003, 1002);
		effects.put(1005, 1003);
		effects.put(1006, 1003);
		effects.put(1007, 1003);
		effects.put(1008, 1003);
		effects.put(1009, 1004);
		effects.put(1010, 1005);
		effects.put(1011, 1006);
		effects.put(1012, 1006);
		effects.put(1013, 1006);
		effects.put(1014, 1006);
		effects.put(1015, 1007);
		effects.put(1016, 1008);
		effects.put(1017, 1008);
		effects.put(1018, 1009);
		effects.put(1019, 1010);
		effects.put(1020, 1011);
		effects.put(1021, 1012);
		effects.put(1022, 1012);
		effects.put(1023, 1013);
		effects.put(1024, 1014);
		effects.put(1025, 1015);
		effects.put(1026, 1016);
		effects.put(1027, 1017);
		effects.put(1028, 1018);
		effects.put(1029, 1020);
		effects.put(1030, 1021);
		effects.put(1031, 1022);
		effects.put(1032, -1);
		effects.put(1033, -1);
		effects.put(1034, -1);
		effects.put(1035, -1);
		effects.put(1036, 1003);
		effects.put(1037, 1006);

		effects.put(3000, -1);
		effects.put(3001, -1);
	}
}
