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
package com.viaversion.viarewind.api.type;

import com.viaversion.viaversion.api.minecraft.BlockPosition;
import com.viaversion.viaversion.api.type.Type;
import io.netty.buffer.ByteBuf;

import java.util.function.IntFunction;

public class PositionVarYType<T extends Number> extends Type<BlockPosition> {

	private final Type<T> yType;
	private final IntFunction<T> toY;

	public PositionVarYType(final Type<T> yType, final IntFunction<T> toY) {
		super(BlockPosition.class);

		this.yType = yType;
		this.toY = toY;
	}

	@Override
	public BlockPosition read(ByteBuf buffer) {
		final int x = buffer.readInt();
		final int y = yType.read(buffer).intValue();
		final int z = buffer.readInt();

		return new BlockPosition(x, y, z);
	}

	@Override
	public void write(ByteBuf buffer, BlockPosition value) {
		buffer.writeInt(value.x());
		yType.write(buffer, this.toY.apply(value.y()));
		buffer.writeInt(value.z());
	}
}
