package de.gerrygames.viarewind.replacement;

import us.myles.ViaVersion.api.minecraft.metadata.Metadata;

import java.util.List;

public interface EntityReplacement {

	public int getEntityId();

	public void setLocation(double x, double y, double z);

	public void relMove(double x, double y, double z);

	public void setYawPitch(float yaw, float pitch);

	public void setHeadYaw(float yaw);

	public void spawn();

	public void despawn();

	public void updateMetadata(List<Metadata> metadataList);
}
