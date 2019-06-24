package de.gerrygames.viarewind.fabric.protocol.protocol1_8to1_7_6_10.metadata;

import de.gerrygames.viarewind.protocol.protocol1_8to1_7_6_10.metadata.MetaIndex1_8to1_7_6_10;
import us.myles.ViaVersion.api.Via;
import us.myles.ViaVersion.api.entities.Entity1_10Types;
import us.myles.ViaVersion.api.minecraft.metadata.Metadata;
import us.myles.ViaVersion.api.minecraft.metadata.types.MetaType1_8;

import java.util.ArrayList;
import java.util.List;

public class MetadataRewriter {
    public static void transform(Entity1_10Types.EntityType type, List<Metadata> list) {
        for (Metadata entry : new ArrayList<>(list)) {
            MetaIndex1_8to1_7_6_10 metaIndex = MetaIndex1_8to1_7_6_10.searchIndex(type, entry.getId());
            try {
                if (metaIndex == null) throw new Exception("Could not find valid metadata");
                if (metaIndex.getNewType() == MetaType1_8.NonExistent) {
                    list.remove(entry);
                    return;
                }
                Object value = entry.getValue();
                if (!value.getClass().isAssignableFrom(metaIndex.getOldType().getType().getOutputClass())) {
                    list.remove(entry);
                    return;
                }
                entry.setMetaType(metaIndex.getNewType());
                entry.setId(metaIndex.getNewIndex());
                switch (metaIndex.getNewType()) {
                    case Int:
                        entry.setValue(((Number) value).intValue());
                        break;
                    case Byte:
                        byte byteValue = ((Number) value).byteValue();
                        entry.setValue(byteValue);
                        if (metaIndex == MetaIndex1_8to1_7_6_10.HUMAN_SKIN_FLAGS) {
                            boolean cape = byteValue == 2;
                            byteValue = (byte) (cape ? 127 : 125);
                            entry.setValue(byteValue);
                        }
                        break;
                    case Short:
                        entry.setValue(((Number) value).shortValue());
                        break;
                    case String:
                        entry.setValue(value.toString());
                        break;
                    case Float:
                        entry.setValue(((Number) value).floatValue());
                        break;
                    case Slot:
                    case Position:
                    case Rotation:
                        entry.setValue(value);
                        break;
                    default:
                        Via.getPlatform().getLogger().warning("[Out] Unhandled MetaDataType: " + metaIndex.getNewType());
                        list.remove(entry);
                        break;
                }
            } catch (Exception e) {
                list.remove(entry);
            }
        }
    }
}
