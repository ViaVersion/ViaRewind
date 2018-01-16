package de.gerrygames.viarewind.protocol.protocol1_7_6_10to1_8.storage;

import de.gerrygames.viarewind.protocol.protocol1_7_6_10to1_8.Protocol1_7_6_10TO1_8;
import de.gerrygames.viarewind.protocol.protocol1_7_6_10to1_8.chunks.ChunkPacketTransformer;
import us.myles.ViaVersion.api.Via;
import us.myles.ViaVersion.api.data.StoredObject;
import us.myles.ViaVersion.api.data.UserConnection;
import us.myles.ViaVersion.exception.CancelException;

import java.util.ArrayList;

public class LoadedChunks extends StoredObject {
	private ArrayList<String> loaded = new ArrayList<>();
	private int playerViewDistance = 0;

	public LoadedChunks(UserConnection user) {
		super(user);

		Via.getPlatform().runRepeatingSync(new Runnable() {
			@Override
			public void run() {
				for (int i = loaded.size() - 1; i >= 0; i--) {
					String chunk = loaded.get(i);
					int chunkX = Integer.parseInt(chunk.split("/")[0]);
					int chunkZ = Integer.parseInt(chunk.split("/")[1]);

					if (!isInViewDistance(chunkX, chunkZ)) {
						loaded.remove(i);
						try {
							ChunkPacketTransformer.getUnloadPacket(chunkX, chunkZ, getUser())
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
		String chunk = x + "/" + z;
		if (!loaded.contains(chunk)) loaded.add(chunk);
	}

	public void unload(int x, int z) {
		String chunk = x + "/" + z;
		loaded.remove(chunk);
	}

	public boolean isLoaded(int x, int z) {
		String chunk = x + "/" + z;
		return loaded.contains(chunk);
	}

	public void clear() {
		loaded.clear();
	}
}
