package de.gerrygames.viarewind.replacement;

import com.viaversion.viaversion.api.minecraft.item.Item;
import com.viaversion.viaversion.libs.opennbt.tag.builtin.CompoundTag;
import com.viaversion.viaversion.libs.opennbt.tag.builtin.StringTag;

public class Replacement {
	private final int id;
	private final int data;
	private final String name;
	private String resetName;
	private String bracketName;

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
			CompoundTag compoundTag = item.tag()==null ? new CompoundTag() : item.tag();
			if (!compoundTag.contains("display")) compoundTag.put("display", new CompoundTag());
			CompoundTag display = compoundTag.get("display");
			if (display.contains("Name")) {
				StringTag name = display.get("Name");
				if (!name.getValue().equals(resetName) && !name.getValue().endsWith(bracketName))
					name.setValue(name.getValue() + bracketName);
			} else {
				display.put("Name", new StringTag(resetName));
			}
			item.setTag(compoundTag);
		}
		return item;
	}

	public int replaceData(int data) {
		return this.data == -1 ? data : this.data;
	}
}
