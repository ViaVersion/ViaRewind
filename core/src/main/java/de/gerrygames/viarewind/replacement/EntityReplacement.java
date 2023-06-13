package de.gerrygames.viarewind.replacement;

import com.viaversion.viaversion.api.minecraft.metadata.Metadata;

import java.util.List;

public interface EntityReplacement {

    int getEntityId();

    void setLocation(double x, double y, double z);

    void relMove(double x, double y, double z);

    void setYawPitch(float yaw, float pitch);

    void setHeadYaw(float yaw);

    void spawn();

    void despawn();

    void updateMetadata(List<Metadata> metadataList);
}
