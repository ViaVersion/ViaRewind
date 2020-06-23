package de.gerrygames.viarewind.replacement;

import de.gerrygames.viarewind.storage.BlockState;
import us.myles.ViaVersion.api.minecraft.item.Item;
import us.myles.viaversion.libs.opennbt.tag.builtin.CompoundTag;
import us.myles.viaversion.libs.opennbt.tag.builtin.StringTag;

public class Replacement {
	private int id, data;
	private String name, resetName, bracketName;

	public Replacement(int id) {
		this(id, -1);
	}

	public Replacement(int id, int data) {
		this(id, data, null);
	}

	public Replacement(int id, String name) {
		this(id, -1, name);
	}

	public Replacement(int id, int data, String name) {
		this.id = id;
		this.data = data;
		this.name = name;
		if (name!=null) {
			this.resetName = "§r" + name;
			this.bracketName = " §r§7(" + name + "§r§7)";
		}
	}

	public int getId() {
		return id;
	}

	public int getData() {
		return data;
	}

	public String getName() {
		return name;
	}

	public Item replace(Item item) {
		item.setIdentifier(id);
		if (data!=-1) item.setData((short)data);
		if (name!=null) {
			CompoundTag compoundTag = item.getTag()==null ? new CompoundTag("") : item.getTag();
			if (!compoundTag.contains("display")) compoundTag.put(new CompoundTag("display"));
			CompoundTag display = compoundTag.get("display");
			if (display.contains("Name")) {
				StringTag name = display.get("Name");
				if (!name.getValue().equals(resetName) && !name.getValue().endsWith(bracketName))
					name.setValue(name.getValue() + bracketName);
			} else {
				display.put(new StringTag("Name", resetName));
			}
			item.setTag(compoundTag);
		}
		return item;
	}

	public BlockState replace(BlockState block) {
		return new BlockState(id, data==-1 ? block.getData() : data);
	}
}
