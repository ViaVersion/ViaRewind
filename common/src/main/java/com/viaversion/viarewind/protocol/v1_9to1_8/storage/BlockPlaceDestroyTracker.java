/*
 * This file is part of ViaRewind - https://github.com/ViaVersion/ViaRewind
 * Copyright (C) 2018-2025 ViaVersion and contributors
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

import com.viaversion.viaversion.api.connection.StorableObject;

public class BlockPlaceDestroyTracker implements StorableObject {
    private long lastMining;

    public boolean isMining() {
        long time = System.currentTimeMillis() - lastMining;
        return time < 75;
    }

    public void setMining() {
        lastMining = System.currentTimeMillis();
    }

    public void updateMining() {
        if (this.isMining()) {
            lastMining = System.currentTimeMillis();
        }
    }

    public void setLastMining(long lastMining) {
        this.lastMining = lastMining;
    }
}
