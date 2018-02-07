package de.gerrygames.viarewind.protocol.protocol1_7_6_10to1_8.entityreplacements;

import de.gerrygames.viarewind.protocol.protocol1_7_6_10to1_8.Protocol1_7_6_10TO1_8;
import de.gerrygames.viarewind.protocol.protocol1_7_6_10to1_8.metadata.MetadataRewriter;
import de.gerrygames.viarewind.protocol.protocol1_7_6_10to1_8.types.MetaType1_7_6_10;
import de.gerrygames.viarewind.protocol.protocol1_7_6_10to1_8.types.Types1_7_6_10;
import lombok.Getter;
import us.myles.ViaVersion.api.PacketWrapper;
import us.myles.ViaVersion.api.data.UserConnection;
import us.myles.ViaVersion.api.entities.Entity1_10Types;
import us.myles.ViaVersion.api.minecraft.metadata.Metadata;
import us.myles.ViaVersion.api.minecraft.metadata.types.MetaType1_8;
import us.myles.ViaVersion.api.type.Type;

import java.util.ArrayList;
import java.util.List;

public class ArmorStandReplacement {
	@Getter
	private int entityId;
	private List<Metadata> datawatcher = new ArrayList<>();
	private int[] entityIds = null;
	private double locX, locY, locZ;
	private State currentState = null;
	private boolean invisible = false;
	private boolean nameTagVisible = false;
	private String name = null;
	private UserConnection user;
	private float yaw, pitch;
	private boolean small = false;
	private static int ENTITY_ID = Integer.MAX_VALUE - 16000;

	private enum State {
		HOLOGRAM, ZOMBIE;
	}

	public ArmorStandReplacement(int entityId, UserConnection user) {
		this.entityId = entityId;
		this.user = user;
	}

	public void setLocation(double x, double y, double z) {
		if (x!=this.locX || y!=this.locY || z!=this.locZ) {
			this.locX = x;
			this.locY = y;
			this.locZ = z;
			updateLocation();
		}
	}

	public void relMove(double x, double y, double z) {
		if (x==0.0 && y==0.0 && z==0.0) return;
		this.locX += x;
		this.locY += y;
		this.locZ += z;
		updateLocation();
	}

	public void setYawPitch(float yaw, float pitch) {
		this.yaw = yaw;
		this.pitch = pitch;
	}

	public void setYaw(float yaw) {
		this.yaw = yaw;
	}

	public void updateMetadata(List<Metadata> metadataList) {
		for (Metadata metadata : metadataList) {
			datawatcher.removeIf(m -> m.getId()==metadata.getId());
			datawatcher.add(metadata);
		}
		updateState();
	}

	public void updateState() {
		byte flags = 0;
		byte armorStandFlags = 0;
		for (Metadata metadata : datawatcher) {
			if (metadata.getId()==0 && metadata.getMetaType()==MetaType1_8.Byte) {
				flags = (byte) metadata.getValue();
			} else if (metadata.getId()==2 && metadata.getMetaType()==MetaType1_8.String) {
				name = (String) metadata.getValue();
				if (name!=null && name.equals("")) name = null;
			} else if (metadata.getId()==10 && metadata.getMetaType()==MetaType1_8.Byte) {
				armorStandFlags = (byte) metadata.getValue();
			} else if (metadata.getId()==3 && metadata.getMetaType()==MetaType1_8.Byte) {
				nameTagVisible = (byte) metadata.getId()!=0;
			}
		}
		invisible = (flags & 0x20) == 0x20;
		small = (armorStandFlags & 0x01) == 0x01;

		State prevState = currentState;
		if (invisible && name!=null) {
			currentState = State.HOLOGRAM;
		} else {
			currentState = State.ZOMBIE;
		}

		if (currentState!=prevState) {
			despawn();
			spawn();
		} else {
			updateMetadata();
			updateLocation();
		}
	}

	public void updateLocation() {
		if (entityIds==null) return;

		if (currentState==State.ZOMBIE) {
			PacketWrapper teleport = new PacketWrapper(0x18, null, user);
			teleport.write(Type.INT, entityId);
			teleport.write(Type.INT, (int) (locX * 32.0));
			teleport.write(Type.INT, (int) (locY * 32.0));
			teleport.write(Type.INT, (int) (locZ * 32.0));
			teleport.write(Type.BYTE, (byte)((yaw / 360f) * 256));
			teleport.write(Type.BYTE, (byte)((pitch / 360f) * 256));

			try {
				teleport.send(Protocol1_7_6_10TO1_8.class, true, true);
			} catch (Exception ex) {ex.printStackTrace();}
		} else if (currentState==State.HOLOGRAM) {
			PacketWrapper detach = new PacketWrapper(0x1B, null, user);
			detach.write(Type.INT, entityIds[1]);
			detach.write(Type.INT, -1);
			detach.write(Type.BOOLEAN, false);

			PacketWrapper teleportSkull = new PacketWrapper(0x18, null, user);
			teleportSkull.write(Type.INT, entityIds[0]);
			teleportSkull.write(Type.INT, (int) (locX * 32.0));
			teleportSkull.write(Type.INT, (int) ((locY + 56.75) * 32.0));  //Don't ask me where this offset is coming from
			teleportSkull.write(Type.INT, (int) (locZ * 32.0));
			teleportSkull.write(Type.BYTE, (byte) 0);
			teleportSkull.write(Type.BYTE, (byte) 0);

			PacketWrapper teleportHorse = new PacketWrapper(0x18, null, user);
			teleportHorse.write(Type.INT, entityIds[1]);
			teleportHorse.write(Type.INT, (int) (locX * 32.0));
			teleportHorse.write(Type.INT, (int) ((locY + 56.75) * 32.0));
			teleportHorse.write(Type.INT, (int) (locZ * 32.0));
			teleportHorse.write(Type.BYTE, (byte) 0);
			teleportHorse.write(Type.BYTE, (byte) 0);

			PacketWrapper attach = new PacketWrapper(0x1B, null, user);
			attach.write(Type.INT, entityIds[1]);
			attach.write(Type.INT, entityIds[0]);
			attach.write(Type.BOOLEAN, false);

			try {
				detach.send(Protocol1_7_6_10TO1_8.class, true, true);
				teleportSkull.send(Protocol1_7_6_10TO1_8.class, true, true);
				teleportHorse.send(Protocol1_7_6_10TO1_8.class, true, true);
				attach.send(Protocol1_7_6_10TO1_8.class, true, true);
			} catch (Exception ex) {ex.printStackTrace();}
		}
	}

	public void updateMetadata() {
		if (entityIds==null) return;

		PacketWrapper metadataPacket = new PacketWrapper(0x1C, null, user);

		if (currentState==State.ZOMBIE) {
			metadataPacket.write(Type.INT, entityIds[0]);

			List<Metadata> metadataList = new ArrayList<>();
			for (Metadata metadata : datawatcher) {
				if (metadata.getId()<0 || metadata.getId()>9) continue;
				metadataList.add(new Metadata(metadata.getId(), metadata.getMetaType(), metadata.getValue()));
			}
			if (small) metadataList.add(new Metadata(12, MetaType1_8.Byte, (byte)1));
			MetadataRewriter.transform(Entity1_10Types.EntityType.ZOMBIE, metadataList);

			metadataPacket.write(Types1_7_6_10.METADATA_LIST, metadataList);
		} else if (currentState==State.HOLOGRAM) {
			metadataPacket.write(Type.INT, entityIds[1]);

			List<Metadata> metadataList = new ArrayList<>();
			metadataList.add(new Metadata(12, MetaType1_7_6_10.Int, -1700000));
			metadataList.add(new Metadata(10, MetaType1_7_6_10.String, name));
			metadataList.add(new Metadata(11, MetaType1_7_6_10.Byte, (byte) 1));

			metadataPacket.write(Types1_7_6_10.METADATA_LIST, metadataList);
		} else {
			return;
		}

		try {
			metadataPacket.send(Protocol1_7_6_10TO1_8.class, true, false);
		} catch (Exception ex) {ex.printStackTrace();}
	}

	public void spawn() {
		if (entityIds!=null) despawn();

		if (currentState==State.ZOMBIE) {
			PacketWrapper spawn = new PacketWrapper(0x0F, null, user);
			spawn.write(Type.VAR_INT, entityId);
			spawn.write(Type.UNSIGNED_BYTE, (short) 54);
			spawn.write(Type.INT, 0);
			spawn.write(Type.INT, 0);
			spawn.write(Type.INT, 0);
			spawn.write(Type.BYTE, (byte) 0);
			spawn.write(Type.BYTE, (byte) 0);
			spawn.write(Type.BYTE, (byte) 0);
			spawn.write(Type.SHORT, (short) 0);
			spawn.write(Type.SHORT, (short) 0);
			spawn.write(Type.SHORT, (short) 0);
			spawn.write(Types1_7_6_10.METADATA_LIST, new ArrayList<>());

			try {
				spawn.send(Protocol1_7_6_10TO1_8.class, true, true);
			} catch (Exception ex) {ex.printStackTrace();}

			entityIds = new int[] {entityId};
			updateMetadata();
			updateLocation();
		} else if (currentState==State.HOLOGRAM) {
			int[] entityIds = new int[] {entityId, ENTITY_ID--};

			PacketWrapper spawnSkull = new PacketWrapper(0x0E, null, user);
			spawnSkull.write(Type.VAR_INT, entityIds[0]);
			spawnSkull.write(Type.BYTE, (byte) 66);
			spawnSkull.write(Type.INT, 0);
			spawnSkull.write(Type.INT, 0);
			spawnSkull.write(Type.INT, 0);
			spawnSkull.write(Type.BYTE, (byte) 0);
			spawnSkull.write(Type.BYTE, (byte) 0);
			spawnSkull.write(Type.INT, 0);

			PacketWrapper spawnHorse = new PacketWrapper(0x0F, null, user);
			spawnHorse.write(Type.VAR_INT, entityIds[1]);
			spawnHorse.write(Type.UNSIGNED_BYTE, (short) 100);
			spawnHorse.write(Type.INT, 0);
			spawnHorse.write(Type.INT, 0);
			spawnHorse.write(Type.INT, 0);
			spawnHorse.write(Type.BYTE, (byte) 0);
			spawnHorse.write(Type.BYTE, (byte) 0);
			spawnHorse.write(Type.BYTE, (byte) 0);
			spawnHorse.write(Type.SHORT, (short) 0);
			spawnHorse.write(Type.SHORT, (short) 0);
			spawnHorse.write(Type.SHORT, (short) 0);
			spawnHorse.write(Types1_7_6_10.METADATA_LIST, new ArrayList<>());

			try {
				spawnSkull.send(Protocol1_7_6_10TO1_8.class, true, true);
				spawnHorse.send(Protocol1_7_6_10TO1_8.class, true, true);
			} catch (Exception ex) {ex.printStackTrace();}

			this.entityIds = entityIds;
			updateMetadata();
			updateLocation();
		}
	}

	public void despawn() {
		if (entityIds==null) return;
		PacketWrapper despawn = new PacketWrapper(0x13, null, user);
		despawn.write(Type.BYTE, (byte)entityIds.length);
		for (int id : entityIds) {
			despawn.write(Type.INT, id);
		}
		entityIds = null;
		try {
			despawn.send(Protocol1_7_6_10TO1_8.class, true, true);
		} catch (Exception ex) {ex.printStackTrace();}
	}
}
