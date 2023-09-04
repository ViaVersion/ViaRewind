/*
 * This file is part of ViaRewind - https://github.com/ViaVersion/ViaRewind
 * Copyright (C) 2016-2023 ViaVersion and contributors
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

package com.viaversion.viarewind.protocol.protocol1_8to1_9.storage;

import com.viaversion.viaversion.api.connection.StoredObject;
import com.viaversion.viaversion.api.connection.UserConnection;

public class BlockPlaceDestroyTracker extends StoredObject {
	private long blockPlaced, lastMining;
	private boolean mining;

	public BlockPlaceDestroyTracker(UserConnection user) {
		super(user);
	}

	public long getBlockPlaced() {
		return blockPlaced;
	}

	public void place() {
		blockPlaced = System.currentTimeMillis();
	}

	public boolean isMining() {
		long time = System.currentTimeMillis()-lastMining;
		return time < 75;
	}

	public void setMining(boolean mining) {
		this.mining = mining && getUser().get(EntityTracker.class).getPlayerGamemode()!=1;
		lastMining = System.currentTimeMillis();
	}

	public long getLastMining() {
		return lastMining;
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
