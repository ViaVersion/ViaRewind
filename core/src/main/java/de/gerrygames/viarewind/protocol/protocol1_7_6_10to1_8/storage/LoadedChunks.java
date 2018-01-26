package de.gerrygames.viarewind.protocol.protocol1_7_6_10to1_8.storage;

import de.gerrygames.viarewind.protocol.protocol1_7_6_10to1_8.Protocol1_7_6_10TO1_8;
import de.gerrygames.viarewind.protocol.protocol1_7_6_10to1_8.chunks.ChunkPacketTransformer;
import us.myles.ViaVersion.api.Via;
import us.myles.ViaVersion.api.data.StoredObject;
import us.myles.ViaVersion.api.data.UserConnection;
import us.myles.ViaVersion.exception.CancelException;

import java.util.ArrayList;

public class LoadedChunks extends StoredObject {
	private ArrayList<ChunkCoords> loaded = new ArrayList<>();
	private int playerViewDistance = 0;

	public class ChunkCoords {
		private int chunkX, chunkZ;

		public ChunkCoords(int chunkX, int chunkZ) {
			this.chunkX = chunkX;
			this.chunkZ = chunkZ;
		}

		public int getChunkX() {
			return chunkX;
		}

		public int getChunkZ() {
			return chunkZ;
		}
	}

	public LoadedChunks(UserConnection user) {
		super(user);

		Via.getPlatform().runRepeatingSync(new Runnable() {
			@Override
			public void run() {
				for (int i = loaded.size() - 1; i >= 0; i--) {
					ChunkCoords chunk = loaded.get(i);

					if (!isInViewDistance(chunk.getChunkX(), chunk.getChunkZ())) {
						loaded.remove(i);
						try {
							ChunkPacketTransformer.getUnloadPacket(chunk.getChunkX(), chunk.getChunkZ(), getUser())
									.send(Protocol1_7_6_10TO1_8.class, true, false);
						} catch (CancelException ignored) {
							;
						} catch (Exception ex) {
							ex.printStackTrace();
						}
					}
				}
			}
		}, 20L*30);
	}

	public boolean isInViewDistance(int chunkX, int chunkZ) {
		PlayerPosition playerPosition = getUser().get(PlayerPosition.class);
		int pChunkX = ((int)playerPosition.getPosX()) >> 4;
		int pChunkZ = ((int)playerPosition.getPosZ()) >> 4;

		return Math.abs(pChunkX-chunkX)<=playerViewDistance && Math.abs(pChunkZ-chunkZ)<=playerViewDistance;
	}

	public int getPlayerViewDistance() {
		return playerViewDistance;
	}

	public void setPlayerViewDistance(int playerViewDistance) {
		this.playerViewDistance = playerViewDistance;
	}

	public void load(int x, int z) {
		ChunkCoords chunk = new ChunkCoords(x, z);
		if (!loaded.contains(chunk)) loaded.add(chunk);
	}

	public void unload(final int x, final int z) {
		loaded.removeIf(chunk -> chunk.getChunkX()==x && chunk.getChunkZ()==z);
	}

	public boolean isLoaded(int x, int z) {
		for (ChunkCoords chunkCoords : loaded) {
			if (chunkCoords.getChunkX()==x && chunkCoords.getChunkZ()==z) return true;
		}
		return false;
	}

	public void clear() {
		loaded.clear();
	}
}
