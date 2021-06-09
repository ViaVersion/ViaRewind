package de.gerrygames.viarewind.protocol.protocol1_8to1_9.items;

import com.viaversion.viaversion.api.minecraft.item.Item;
import de.gerrygames.viarewind.replacement.Replacement;
import de.gerrygames.viarewind.replacement.ReplacementRegistry;
import de.gerrygames.viarewind.storage.BlockState;

public class ReplacementRegistry1_8to1_9 {
	private static final ReplacementRegistry registry = new ReplacementRegistry();

	static {
		registry.registerItem(198, new Replacement(50, 0, "End Rod"));
		registry.registerItem(434, new Replacement(391, "Beetroot"));
		registry.registerItem(435, new Replacement(361, "Beetroot Seeds"));
		registry.registerItem(436, new Replacement(282, "Beetroot Soup"));
		registry.registerItem(432, new Replacement(322, "Chorus Fruit"));
		registry.registerItem(433, new Replacement(393, "Popped Chorus Fruit"));
		registry.registerItem(437, new Replacement(373, "Dragons Breath"));
		registry.registerItem(443, new Replacement(299, "Elytra"));
		registry.registerItem(426, new Replacement(410, "End Crystal"));
		registry.registerItem(442, new Replacement(425, "Shield"));
		registry.registerItem(439, new Replacement(262, "Spectral Arrow"));
		registry.registerItem(440, new Replacement(262, "Tipped Arrow"));
		registry.registerItem(444, new Replacement(333, "Spruce Boat"));
		registry.registerItem(445, new Replacement(333, "Birch Boat"));
		registry.registerItem(446, new Replacement(333, "Jungle Boat"));
		registry.registerItem(447, new Replacement(333, "Acacia Boat"));
		registry.registerItem(448, new Replacement(333, "Dark Oak Boat"));
		registry.registerItem(204, new Replacement(43, 7, "Purpur Double Slab"));
		registry.registerItem(205, new Replacement(44, 7, "Purpur Slab"));

		registry.registerBlock(209, new Replacement(119));
		registry.registerBlock(198, 0, new Replacement(50, 5));
		registry.registerBlock(198, 1, new Replacement(50, 5));
		registry.registerBlock(198, 2, new Replacement(50, 4));
		registry.registerBlock(198, 3, new Replacement(50, 3));
		registry.registerBlock(198, 4, new Replacement(50, 2));
		registry.registerBlock(198, 5, new Replacement(50, 1));
		registry.registerBlock(204, new Replacement(43, 7));
		registry.registerBlock(205, 0, new Replacement(44, 7));
		registry.registerBlock(205, 8, new Replacement(44, 15));
		registry.registerBlock(207, new Replacement(141));
		registry.registerBlock(137, new Replacement(137, 0));

		registry.registerItemBlock(199, new Replacement(35, 10, "Chorus Plant"));
		registry.registerItemBlock(200, new Replacement(35, 2, "Chorus Flower"));
		registry.registerItemBlock(201, new Replacement(155, 0, "Purpur Block"));
		registry.registerItemBlock(202, new Replacement(155, 2, "Purpur Pillar"));
		registry.registerItemBlock(203, 0, new Replacement(156, 0, "Purpur Stairs"));
		registry.registerItemBlock(203, 1, new Replacement(156, 1, "Purpur Stairs"));
		registry.registerItemBlock(203, 2, new Replacement(156, 2, "Purpur Stairs"));
		registry.registerItemBlock(203, 3, new Replacement(156, 3, "Purpur Stairs"));
		registry.registerItemBlock(203, 4, new Replacement(156, 4, "Purpur Stairs"));
		registry.registerItemBlock(203, 5, new Replacement(156, 5, "Purpur Stairs"));
		registry.registerItemBlock(203, 6, new Replacement(156, 6, "Purpur Stairs"));
		registry.registerItemBlock(203, 7, new Replacement(156, 7, "Purpur Stairs"));
		registry.registerItemBlock(203, 8, new Replacement(156, 8, "Purpur Stairs"));
		registry.registerItemBlock(206, new Replacement(121, 0, "Endstone Bricks"));
		registry.registerItemBlock(207, new Replacement(141, "Beetroot Block"));
		registry.registerItemBlock(208, new Replacement(2, 0, "Grass Path"));
		registry.registerItemBlock(209, new Replacement(90, "End Gateway"));
		registry.registerItemBlock(210, new Replacement(137, 0, "Repeating Command Block"));
		registry.registerItemBlock(211, new Replacement(137, 0, "Chain Command Block"));
		registry.registerItemBlock(212, new Replacement(79, 0, "Frosted Ice"));
		registry.registerItemBlock(214, new Replacement(87, 0, "Nether Wart Block"));
		registry.registerItemBlock(215, new Replacement(112, 0, "Red Nether Brick"));
		registry.registerItemBlock(217, new Replacement(166, 0, "Structure Void"));
		registry.registerItemBlock(255, new Replacement(137, 0, "Structure Block"));
		registry.registerItemBlock(397, 5, new Replacement(397, 0, "Dragon Head"));
	}

	public static Item replace(Item item) {
		return registry.replace(item);
	}

	public static int replace(int raw) {
		int data = BlockState.extractData(raw);
		Replacement replace = registry.replace(BlockState.extractId(raw), data);
		if (replace == null) return raw;
		return BlockState.stateToRaw(replace.getId(), replace.replaceData(data));
	}

	public static Replacement getReplacement(int id, int data) {
		return registry.replace(id, data);
	}

}
