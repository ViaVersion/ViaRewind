package de.gerrygames.viarewind.fabric.protocol.protocol1_8to1_7_6_10.storage;

import us.myles.ViaVersion.api.data.StoredObject;
import us.myles.ViaVersion.api.data.UserConnection;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Tablist extends StoredObject {

    private ArrayList<TabListEntry> tablist = new ArrayList<>();

    public Tablist(UserConnection user) {
        super(user);
    }

    public static boolean shouldUpdateDisplayName(String oldName, String newName) {
        return oldName == null && newName != null || oldName != null && newName == null || oldName != null && !oldName.equals(newName);
    }

    public TabListEntry getTabListEntry(String name) {
        for (TabListEntry entry : tablist) if (name.equals(entry.name)) return entry;
        return null;
    }

    public TabListEntry getTabListEntry(UUID uuid) {
        for (TabListEntry entry : tablist) if (uuid.equals(entry.uuid)) return entry;
        return null;
    }

    public void remove(TabListEntry entry) {
        tablist.remove(entry);
    }

    public void add(TabListEntry entry) {
        tablist.add(entry);
    }

    public static class TabListEntry {
        public String name;
        public String displayName;
        public UUID uuid;
        public int ping;
        public List<Property> properties = new ArrayList<>();

        public TabListEntry(String name, UUID uuid) {
            this.name = name;
            this.uuid = uuid;
        }
    }

    public static class Property {
        public String name;
        public String value;
        public String signature;

        public Property(String name, String value, String signature) {
            this.name = name;
            this.value = value;
            this.signature = signature;
        }
    }
}
