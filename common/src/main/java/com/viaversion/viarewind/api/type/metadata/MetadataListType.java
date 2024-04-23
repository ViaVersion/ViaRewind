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
package com.viaversion.viarewind.api.type.metadata;

import com.viaversion.viarewind.api.type.Types1_7_6_10;
import com.viaversion.viaversion.api.minecraft.metadata.Metadata;
import com.viaversion.viaversion.api.type.types.metadata.MetaListTypeTemplate;
import io.netty.buffer.ByteBuf;

import java.util.ArrayList;
import java.util.List;

public class MetadataListType extends MetaListTypeTemplate {
	@Override
	public List<Metadata> read(ByteBuf buffer) throws Exception {
		ArrayList<Metadata> list = new ArrayList<>();

		Metadata m;
		do {
			m = Types1_7_6_10.METADATA.read(buffer);
			if (m != null) {
				list.add(m);
			}
		} while (m != null);

		return list;
	}

	@Override
	public void write(ByteBuf buffer, List<Metadata> metadata) throws Exception {
		for (Metadata meta : metadata) {
			Types1_7_6_10.METADATA.write(buffer, meta);
		}
		if (metadata.isEmpty()) {
			Types1_7_6_10.METADATA.write(buffer, new Metadata(0, MetaType1_7_6_10.Byte, (byte) 0));
		}
		buffer.writeByte(127);
	}
}