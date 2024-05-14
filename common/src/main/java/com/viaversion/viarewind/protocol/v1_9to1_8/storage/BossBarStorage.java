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
package com.viaversion.viarewind.protocol.v1_9to1_8.storage;

import com.viaversion.viarewind.protocol.v1_9to1_8.data.WitherBossBar;
import com.viaversion.viaversion.api.connection.StoredObject;
import com.viaversion.viaversion.api.connection.UserConnection;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class BossBarStorage extends StoredObject {

	private final Map<UUID, WitherBossBar> bossBars = new HashMap<>();

	public BossBarStorage(UserConnection user) {
		super(user);
	}

	public void reset() {
		updateLocation();
		changeWorld();
	}

	public void add(UUID uuid, String title, float health) {
		WitherBossBar bossBar = new WitherBossBar(this.getUser(), uuid, title, health);
		PlayerPositionTracker playerPositionTracker = this.getUser().get(PlayerPositionTracker.class);
		bossBar.setPlayerLocation(playerPositionTracker.getPosX(), playerPositionTracker.getPosY(), playerPositionTracker.getPosZ(), playerPositionTracker.getYaw(), playerPositionTracker.getPitch());
		bossBar.show();
		bossBars.put(uuid, bossBar);
	}

	public void remove(UUID uuid) {
		WitherBossBar bossBar = bossBars.remove(uuid);
		if (bossBar == null) return;
		bossBar.hide();
	}

	public void updateLocation() {
		PlayerPositionTracker playerPositionTracker = this.getUser().get(PlayerPositionTracker.class);
		bossBars.values().forEach(bossBar -> {
			try {
				bossBar.setPlayerLocation(playerPositionTracker.getPosX(), playerPositionTracker.getPosY(), playerPositionTracker.getPosZ(), playerPositionTracker.getYaw(), playerPositionTracker.getPitch());
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		});
	}

	public void changeWorld() {
		bossBars.values().forEach(bossBar -> {
			bossBar.hide();
			bossBar.show();
		});
	}

	public void updateHealth(UUID uuid, float health) {
		WitherBossBar bossBar = bossBars.get(uuid);
		if (bossBar == null) return;
		bossBar.setHealth(health);
	}

	public void updateTitle(UUID uuid, String title) {
		WitherBossBar bossBar = bossBars.get(uuid);
		if (bossBar == null) return;
		bossBar.setTitle(title);
	}
}
