package de.gerrygames.viarewind.protocol.protocol1_8to1_9.storage;

import de.gerrygames.viarewind.protocol.protocol1_8to1_9.Protocol1_8TO1_9;
import de.gerrygames.viarewind.utils.PacketUtil;
import us.myles.ViaVersion.api.PacketWrapper;
import us.myles.ViaVersion.api.data.StoredObject;
import us.myles.ViaVersion.api.data.UserConnection;
import us.myles.ViaVersion.api.minecraft.item.Item;
import us.myles.ViaVersion.api.type.Type;

import java.util.HashMap;

public class Windows extends StoredObject {
	private HashMap<Short, String> types = new HashMap<>();
	private HashMap<Short, Item[]> brewingItems = new HashMap<>();

	public Windows(UserConnection user) {
		super(user);
	}

	public String get(short windowId) {
		return types.get(windowId);
	}

	public void put(short windowId, String type) {
		types.put(windowId, type);
	}

	public void remove(short windowId) {
		types.remove(windowId);
		brewingItems.remove(windowId);
	}

	public Item[] getBrewingItems(short windowId) {
		return brewingItems.computeIfAbsent(windowId, key -> new Item[] {
				new Item(),
				new Item(),
				new Item(),
				new Item()
		});
	}

	public static void updateBrewingStand(UserConnection user, Item blazePowder, short windowId) {
		if (blazePowder != null && blazePowder.getIdentifier() != 377) return;
		int amount = blazePowder == null ? 0 : blazePowder.getAmount();
		PacketWrapper openWindow = new PacketWrapper(0x2D, null, user);
		openWindow.write(Type.UNSIGNED_BYTE, windowId);
		openWindow.write(Type.STRING, "minecraft:brewing_stand");
		openWindow.write(Type.STRING, "[{\"translate\":\"container.brewing\"},{\"text\":\": \",\"color\":\"dark_gray\"},{\"text\":\"§4" + amount + " \",\"color\":\"dark_red\"},{\"translate\":\"item.blazePowder.name\",\"color\":\"dark_red\"}]");
		openWindow.write(Type.UNSIGNED_BYTE, (short) 420);
		PacketUtil.sendPacket(openWindow, Protocol1_8TO1_9.class);

		Item[] items = user.get(Windows.class).getBrewingItems(windowId);
		for (int i = 0; i < items.length; i++) {
			PacketWrapper setSlot = new PacketWrapper(0x2F, null, user);
			setSlot.write(Type.BYTE, (byte) windowId);
			setSlot.write(Type.SHORT, (short) i);
			setSlot.write(Type.ITEM, items[i]);
			PacketUtil.sendPacket(setSlot, Protocol1_8TO1_9.class);
		}
	}
}
