package de.gerrygames.viarewind.protocol.protocol1_8to1_9.metadata;

import com.viaversion.viaversion.api.minecraft.EulerAngle;
import com.viaversion.viaversion.api.minecraft.Vector;
import com.viaversion.viaversion.api.minecraft.entities.Entity1_10Types;
import com.viaversion.viaversion.api.minecraft.item.Item;
import com.viaversion.viaversion.api.minecraft.metadata.Metadata;
import com.viaversion.viaversion.api.minecraft.metadata.types.MetaType1_8;
import com.viaversion.viaversion.api.minecraft.metadata.types.MetaType1_9;
import com.viaversion.viaversion.protocols.protocol1_9to1_8.metadata.MetaIndex;
import de.gerrygames.viarewind.ViaRewind;
import de.gerrygames.viarewind.protocol.protocol1_8to1_9.items.ItemRewriter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MetadataRewriter {
	public static void transform(Entity1_10Types.EntityType type, List<Metadata> list) {
		for (Metadata entry : new ArrayList<>(list)) {
			MetaIndex metaIndex = MetaIndex1_8to1_9.searchIndex(type, entry.id());
			try {
				if (metaIndex != null) {
					if (metaIndex.getOldType() == MetaType1_8.NonExistent || metaIndex.getNewType()==null) {
						list.remove(entry);
						continue;
					}
					Object value = entry.getValue();
					entry.setMetaTypeUnsafe(metaIndex.getOldType());
					entry.setId(metaIndex.getIndex());
					switch (metaIndex.getNewType()) {
						case Byte:
							if (metaIndex.getOldType() == MetaType1_8.Byte) {
								entry.setValue(value);
							}
							if (metaIndex.getOldType() == MetaType1_8.Int) {
								entry.setValue(((Byte) value).intValue());
							}
							break;
						case OptUUID:
							if (metaIndex.getOldType()!=MetaType1_8.String) {
								list.remove(entry);
								break;
							}
							UUID owner = (UUID) value;
							if (owner == null) entry.setValue("");
							else entry.setValue(owner.toString());
							break;
						case BlockID:
							list.remove(entry);
							list.add(new Metadata(metaIndex.getIndex(), MetaType1_8.Short, ((Integer) value).shortValue()));
							break;
						case VarInt:
							if (metaIndex.getOldType() == MetaType1_8.Byte) {
								entry.setValue(((Integer) value).byteValue());
							}
							if (metaIndex.getOldType() == MetaType1_8.Short) {
								entry.setValue(((Integer) value).shortValue());
							}
							if (metaIndex.getOldType() == MetaType1_8.Int) {
								entry.setValue(value);
							}
							break;
						case Float:
							entry.setValue(value);
							break;
						case String:
							entry.setValue(value);
							break;
						case Boolean:
							if (metaIndex == MetaIndex.AGEABLE_AGE) entry.setValue((byte)((Boolean) value ? -1 : 0));
							else entry.setValue((byte)((Boolean) value ? 1 : 0));
							break;
						case Slot:
							entry.setValue(ItemRewriter.toClient((Item) value));
							break;
						case Position:
							Vector vector = (Vector) value;
							entry.setValue(vector);
							break;
						case Vector3F:
							EulerAngle angle = (EulerAngle) value;
							entry.setValue(angle);
							break;
						case Chat:
							entry.setValue(value);
							break;
						default:
							ViaRewind.getPlatform().getLogger().warning("[Out] Unhandled MetaDataType: " + metaIndex.getNewType());
							list.remove(entry);
							break;
					}

					if (!metaIndex.getOldType().type().getOutputClass().isAssignableFrom(entry.getValue().getClass())) {
						list.remove(entry);
					}

				} else {
					throw new Exception("Could not find valid metadata");
				}
			} catch (Exception e) {
				list.remove(entry);
			}
		}
	}

}
