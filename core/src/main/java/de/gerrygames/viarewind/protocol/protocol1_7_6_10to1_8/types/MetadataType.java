package de.gerrygames.viarewind.protocol.protocol1_7_6_10to1_8.types;

import io.netty.buffer.ByteBuf;
import us.myles.ViaVersion.api.minecraft.metadata.Metadata;
import us.myles.ViaVersion.api.type.types.minecraft.MetaTypeTemplate;

public class MetadataType extends MetaTypeTemplate {
	@Override
	public Metadata read(ByteBuf buffer) throws Exception {
		byte item = buffer.readByte();
		if (item == 127) {
			return null;
		} else {
			int typeID = (item & 224) >> 5;
			MetaType1_7_6_10 type = MetaType1_7_6_10.byId(typeID);
			int id = item & 31;
			return new Metadata(id, type, type.getType().read(buffer));
		}
	}

	@Override
	public void write(ByteBuf buffer, Metadata meta) throws Exception {
		int item = (meta.getMetaType().getTypeID() << 5 | meta.getId() & 31) & 255;
		buffer.writeByte(item);
		meta.getMetaType().getType().write(buffer, meta.getValue());
	}
}
