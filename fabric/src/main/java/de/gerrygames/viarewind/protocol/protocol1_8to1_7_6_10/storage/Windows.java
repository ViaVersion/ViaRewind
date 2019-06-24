package de.gerrygames.viarewind.protocol.protocol1_8to1_7_6_10.storage;

import us.myles.ViaVersion.api.data.StoredObject;
import us.myles.ViaVersion.api.data.UserConnection;

import java.util.HashMap;

public class Windows extends StoredObject {

    public HashMap<Short, Short> types = new HashMap<>();

    public Windows(UserConnection user) {
        super(user);
    }

    public short get(short windowId) {
        return types.getOrDefault(windowId, (short) -1);
    }
}
