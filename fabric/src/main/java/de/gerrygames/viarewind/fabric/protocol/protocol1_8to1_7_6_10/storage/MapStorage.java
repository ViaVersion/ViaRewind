package de.gerrygames.viarewind.fabric.protocol.protocol1_8to1_7_6_10.storage;

import us.myles.ViaVersion.api.data.StoredObject;
import us.myles.ViaVersion.api.data.UserConnection;

import java.util.HashMap;
import java.util.Map;

public class MapStorage extends StoredObject {
    private Map<Integer, MapData> maps = new HashMap<>();

    public MapStorage(UserConnection user) {
        super(user);
    }

    public MapData getMapData(int id) {
        return maps.get(id);
    }

    public void putMapData(int id, MapData mapData) {
        maps.put(id, mapData);
    }

    public static class MapData {
        public byte scale = 0;
        public MapIcon[] mapIcons = {};
    }

    public static class MapIcon {
        public byte direction;
        public byte type;
        public byte x;
        public byte z;

        public MapIcon(byte direction, byte type, byte x, byte z) {
            this.direction = direction;
            this.type = type;
            this.x = x;
            this.z = z;
        }
    }
}
