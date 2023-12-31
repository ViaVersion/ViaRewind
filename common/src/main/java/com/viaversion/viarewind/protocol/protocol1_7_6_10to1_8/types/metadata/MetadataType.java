/*
 * This file is part of ViaRewind - https://github.com/ViaVersion/ViaRewind
 * Copyright (C) 2018-2024 ViaVersion and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.viaversion.viarewind.protocol.protocol1_7_6_10to1_8.types.metadata;

import com.viaversion.viaversion.api.minecraft.metadata.Metadata;
import com.viaversion.viaversion.api.type.types.metadata.MetaTypeTemplate;
import io.netty.buffer.ByteBuf;

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
			return new Metadata(id, type, type.type().read(buffer));
		}
	}

	@Override
	public void write(ByteBuf buffer, Metadata meta) throws Exception {
		int item = (meta.metaType().typeId() << 5 | meta.id() & 31) & 255;
		buffer.writeByte(item);
		meta.metaType().type().write(buffer, meta.getValue());
	}
}
