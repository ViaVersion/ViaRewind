package de.gerrygames.viarewind.protocol.protocol1_8to1_9.storage;

import com.viaversion.viaversion.api.connection.StoredObject;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.minecraft.item.DataItem;
import com.viaversion.viaversion.api.minecraft.item.Item;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.libs.kyori.adventure.text.Component;
import com.viaversion.viaversion.libs.kyori.adventure.text.format.NamedTextColor;
import com.viaversion.viaversion.libs.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import com.viaversion.viaversion.protocols.protocol1_8.ClientboundPackets1_8;
import de.gerrygames.viarewind.protocol.protocol1_8to1_9.Protocol1_8TO1_9;
import de.gerrygames.viarewind.utils.PacketUtil;

import java.util.HashMap;

public class Windows extends StoredObject {
	private final HashMap<Short, String> types = new HashMap<>();
	private final HashMap<Short, Item[]> brewingItems = new HashMap<>();

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
				new DataItem(),
				new DataItem(),
				new DataItem(),
				new DataItem()
		});
	}

	public static void updateBrewingStand(UserConnection user, Item blazePowder, short windowId) {
		if (blazePowder != null && blazePowder.identifier() != 377) return;
		int amount = blazePowder == null ? 0 : blazePowder.amount();
		PacketWrapper openWindow = PacketWrapper.create(ClientboundPackets1_8.OPEN_WINDOW, user);
		openWindow.write(Type.UNSIGNED_BYTE, windowId);
		openWindow.write(Type.STRING, "minecraft:brewing_stand");
		Component title = Component.empty()
				.append(Component.translatable("container.brewing"))
				.append(Component.text(": ", NamedTextColor.DARK_GRAY))
				.append(Component.text(amount + " ", NamedTextColor.DARK_RED))
				.append(Component.translatable("item.blazePowder.name", NamedTextColor.DARK_RED));
		openWindow.write(Type.COMPONENT, GsonComponentSerializer.colorDownsamplingGson().serializeToTree(title));
		openWindow.write(Type.UNSIGNED_BYTE, (short) 420);
		PacketUtil.sendPacket(openWindow, Protocol1_8TO1_9.class);

		Item[] items = user.get(Windows.class).getBrewingItems(windowId);
		for (int i = 0; i < items.length; i++) {
			PacketWrapper setSlot = PacketWrapper.create(ClientboundPackets1_8.SET_SLOT, user);
			setSlot.write(Type.UNSIGNED_BYTE, windowId);
			setSlot.write(Type.SHORT, (short) i);
			setSlot.write(Type.ITEM, items[i]);
			PacketUtil.sendPacket(setSlot, Protocol1_8TO1_9.class);
		}
	}
}
