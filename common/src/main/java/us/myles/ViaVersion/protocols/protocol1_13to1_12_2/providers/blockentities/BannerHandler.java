package us.myles.ViaVersion.protocols.protocol1_13to1_12_2.providers.blockentities;

import com.github.steveice10.opennbt.tag.builtin.*;
import us.myles.ViaVersion.api.Via;
import us.myles.ViaVersion.api.data.UserConnection;
import us.myles.ViaVersion.api.minecraft.Position;
import us.myles.ViaVersion.protocols.protocol1_13to1_12_2.ChatRewriter;
import us.myles.ViaVersion.protocols.protocol1_13to1_12_2.providers.BlockEntityProvider;
import us.myles.ViaVersion.protocols.protocol1_13to1_12_2.storage.BlockStorage;

public class BannerHandler implements BlockEntityProvider.BlockEntityHandler {
    private final int WALL_BANNER_START = 7110; // 4 each
    private final int WALL_BANNER_STOP = 7173;

    private final int BANNER_START = 6854; // 16 each
    private final int BANNER_STOP = 7109;

    @Override
    public int transform(UserConnection user, CompoundTag tag) {
        BlockStorage storage = user.get(BlockStorage.class);
        Position position = new Position(getLong(tag.get("x")), getLong(tag.get("y")), getLong(tag.get("z")));

        if (!storage.contains(position)) {
            Via.getPlatform().getLogger().warning("Received an banner color update packet, but there is no banner! O_o " + tag);
            return -1;
        }

        int blockId = storage.get(position).getOriginal();

        Tag base = tag.get("Base");
        int color = 0;
        if (base != null) {
            color = ((Number) tag.get("Base").getValue()).intValue();
        }
        // Standing banner
        if (blockId >= BANNER_START && blockId <= BANNER_STOP) {
            blockId += ((15 - color) * 16);
            // Wall banner
        } else if (blockId >= WALL_BANNER_START && blockId <= WALL_BANNER_STOP) {
            blockId += ((15 - color) * 4);
        } else {
            Via.getPlatform().getLogger().warning("Why does this block have the banner block entity? :(" + tag);
        }

        if (tag.get("Patterns") instanceof ListTag) {
            for (Tag pattern : (ListTag) tag.get("Patterns")) {
                if (pattern instanceof CompoundTag) {
                    Tag c = ((CompoundTag) pattern).get("Color");
                    if (c instanceof IntTag) {
                        ((IntTag)c).setValue(15 - (int) c.getValue()); // Invert color id
                    }
                }
            }
        }

        Tag name = tag.get("CustomName");
        if (name instanceof StringTag) {
            ((StringTag) name).setValue(ChatRewriter.legacyTextToJson(((StringTag) name).getValue()));
        }

        return blockId;
    }

    private long getLong(Tag tag) {
        return ((Integer) tag.getValue()).longValue();
    }
}