package de.gerrygames.viarewind.protocol.protocol1_8to1_9.sound;

import java.util.HashMap;
import java.util.Map;

public class Effect {
	private static final Map<Integer, Integer> EFFECT_MAP = new HashMap<>();

	public static int getOldId(int id) {
		return EFFECT_MAP.getOrDefault(id, id);
	}

	static {
		EFFECT_MAP.put(1003, 1002);
		EFFECT_MAP.put(1005, 1003);
		EFFECT_MAP.put(1006, 1003);
		EFFECT_MAP.put(1007, 1003);
		EFFECT_MAP.put(1008, 1003);
		EFFECT_MAP.put(1009, 1004);
		EFFECT_MAP.put(1010, 1005);
		EFFECT_MAP.put(1011, 1006);
		EFFECT_MAP.put(1012, 1006);
		EFFECT_MAP.put(1013, 1006);
		EFFECT_MAP.put(1014, 1006);
		EFFECT_MAP.put(1015, 1007);
		EFFECT_MAP.put(1016, 1008);
		EFFECT_MAP.put(1017, 1008);
		EFFECT_MAP.put(1018, 1009);
		EFFECT_MAP.put(1019, 1010);
		EFFECT_MAP.put(1020, 1011);
		EFFECT_MAP.put(1021, 1012);
		EFFECT_MAP.put(1022, 1012);
		EFFECT_MAP.put(1023, 1013);
		EFFECT_MAP.put(1024, 1014);
		EFFECT_MAP.put(1025, 1015);
		EFFECT_MAP.put(1026, 1016);
		EFFECT_MAP.put(1027, 1017);
		EFFECT_MAP.put(1028, 1018);
		EFFECT_MAP.put(1029, 1020);
		EFFECT_MAP.put(1030, 1021);
		EFFECT_MAP.put(1031, 1022);
		EFFECT_MAP.put(1032, -1);
		EFFECT_MAP.put(1033, -1);
		EFFECT_MAP.put(1034, -1);
		EFFECT_MAP.put(1035, -1);
		EFFECT_MAP.put(1036, 1003);
		EFFECT_MAP.put(1037, 1006);

		EFFECT_MAP.put(3000, -1);
		EFFECT_MAP.put(3001, -1);
	}
}
