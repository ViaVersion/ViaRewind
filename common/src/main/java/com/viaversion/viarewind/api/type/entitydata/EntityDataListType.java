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
package com.viaversion.viarewind.api.type.entitydata;

import com.viaversion.viarewind.api.minecraft.entitydata.EntityDataTypes1_7_6_10;
import com.viaversion.viaversion.api.minecraft.entitydata.EntityData;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.api.type.types.entitydata.EntityDataListTypeTemplate;
import io.netty.buffer.ByteBuf;

import java.util.ArrayList;
import java.util.List;

public class EntityDataListType extends EntityDataListTypeTemplate {

	private final Type<EntityData> type;

	public EntityDataListType(Type<EntityData> type) {
		this.type = type;
	}

	@Override
	public List<EntityData> read(ByteBuf buffer) {
		ArrayList<EntityData> entityData = new ArrayList<>();

		EntityData data;
		do {
			data = type.read(buffer);
			if (data != null) {
				entityData.add(data);
			}
		} while (data != null);

		return entityData;
	}

	@Override
	public void write(ByteBuf buffer, List<EntityData> entityData) {
		for (EntityData meta : entityData) {
			type.write(buffer, meta);
		}
		if (entityData.isEmpty()) {
			type.write(buffer, new EntityData(0, EntityDataTypes1_7_6_10.BYTE, (byte) 0));
		}
		buffer.writeByte(127);
	}
}