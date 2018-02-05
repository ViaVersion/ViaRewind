package de.gerrygames.viarewind.protocol.protocol1_8to1_9.storage;

import us.myles.ViaVersion.api.data.StoredObject;
import us.myles.ViaVersion.api.data.UserConnection;

import java.util.HashMap;

public class Windows extends StoredObject {
	public HashMap<Short, String> types = new HashMap<>();

	public Windows(UserConnection user) {
		super(user);
	}

	public String get(short windowId) {
		return types.get(windowId);
	}
}
