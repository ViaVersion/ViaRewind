/*
 * This file is part of ViaRewind - https://github.com/ViaVersion/ViaRewind
 * Copyright (C) 2018-2026 ViaVersion and contributors
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
package com.viaversion.viarewind.protocol.v1_9to1_8.storage;

import com.viaversion.viarewind.protocol.v1_9to1_8.data.CommandBlockState;
import com.viaversion.viaversion.api.connection.StorableObject;
import com.viaversion.viaversion.api.minecraft.BlockPosition;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public final class CommandBlockStateStorage implements StorableObject {

    private final Map<BlockPosition, Integer> commandBlockStates = new HashMap<>();

    public void storeOrRemove(final BlockPosition position, final int blockState) {
        if (CommandBlockState.isCommandBlock(blockState)) {
            commandBlockStates.put(position, blockState);
        } else {
            commandBlockStates.remove(position);
        }
    }

    public int state(final BlockPosition position) {
        return commandBlockStates.getOrDefault(position, -1);
    }

    public void unloadChunk(final int chunkX, final int chunkZ) {
        final Iterator<BlockPosition> iterator = commandBlockStates.keySet().iterator();
        while (iterator.hasNext()) {
            final BlockPosition position = iterator.next();
            if (position.x() >> 4 == chunkX && position.z() >> 4 == chunkZ) {
                iterator.remove();
            }
        }
    }
}
