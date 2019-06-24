package de.gerrygames.viarewind.fabric.protocol.protocol1_8to1_7_6_10.storage;

import de.gerrygames.viarewind.fabric.protocol.protocol1_8to1_7_6_10.Protocol1_8To1_7_6_10;
import de.gerrygames.viarewind.fabric.protocol.protocol1_8to1_7_6_10.metadata.MetadataRewriter;
import us.myles.ViaVersion.api.PacketWrapper;
import us.myles.ViaVersion.api.data.StoredObject;
import us.myles.ViaVersion.api.data.UserConnection;
import us.myles.ViaVersion.api.entities.Entity1_10Types;
import us.myles.ViaVersion.api.minecraft.metadata.Metadata;
import us.myles.ViaVersion.api.type.Type;
import us.myles.ViaVersion.api.type.types.version.Types1_8;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class EntityTracker extends StoredObject {
    private final Map<Integer, Entity1_10Types.EntityType> clientEntityTypes = new ConcurrentHashMap<>();
    private final Map<Integer, List<Metadata>> metadataBuffer = new ConcurrentHashMap<>();

    public EntityTracker(UserConnection user) {
        super(user);
    }

    public void removeEntity(int entityId) {
        clientEntityTypes.remove(entityId);
    }

    public Map<Integer, Entity1_10Types.EntityType> getClientEntityTypes() {
        return this.clientEntityTypes;
    }

    public void addMetadataToBuffer(int entityID, List<Metadata> metadataList) {
        if (this.metadataBuffer.containsKey(entityID)) {
            this.metadataBuffer.get(entityID).addAll(metadataList);
        } else if (!metadataList.isEmpty()) {
            this.metadataBuffer.put(entityID, metadataList);
        }
    }

    public List<Metadata> getBufferedMetadata(int entityId) {
        return metadataBuffer.get(entityId);
    }

    public void sendMetadataBuffer(int entityId) {
        if (!this.metadataBuffer.containsKey(entityId)) return;
        PacketWrapper wrapper = new PacketWrapper(0x1C, null, this.getUser());
        wrapper.write(Type.VAR_INT, entityId);
        wrapper.write(Types1_8.METADATA_LIST, this.metadataBuffer.get(entityId));
        MetadataRewriter.transform(this.getClientEntityTypes().get(entityId), this.metadataBuffer.get(entityId));
        if (!this.metadataBuffer.get(entityId).isEmpty()) {
            try {
                wrapper.send(Protocol1_8To1_7_6_10.class);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        this.metadataBuffer.remove(entityId);
    }

}
