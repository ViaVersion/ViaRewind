package de.gerrygames.viarewind.protocol.protocol1_8to1_7_6_10.storage;

import us.myles.ViaVersion.api.data.StoredObject;
import us.myles.ViaVersion.api.data.UserConnection;

import java.util.HashMap;

public class Scoreboard extends StoredObject {

    private HashMap<String, String> objectives = new HashMap<>();

    public Scoreboard(UserConnection user) {
        super(user);
    }

    public void put(String name, String objective) {
        objectives.put(name, objective);
    }

    public String get(String name) {
        return objectives.getOrDefault(name, "null");
    }
}
