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
package com.viaversion.viarewind.protocol.v1_9to1_8.cooldown;

import com.viaversion.viarewind.ViaRewind;
import com.viaversion.viarewind.api.ViaRewindConfig.CooldownIndicator;
import com.viaversion.viaversion.api.connection.UserConnection;

public interface CooldownVisualization {
	void show(double progress) throws Exception;

	void hide() throws Exception;


	int MAX_PROGRESS_TEXT_LENGTH = 10;

	static String buildProgressText(String symbol, double cooldown) {
		int green = (int) Math.floor(((double) MAX_PROGRESS_TEXT_LENGTH) * cooldown);
		int grey = MAX_PROGRESS_TEXT_LENGTH - green;
		StringBuilder builder = new StringBuilder("§8");
		while (green-- > 0) builder.append(symbol);
		builder.append("§7");
		while (grey-- > 0) builder.append(symbol);
		return builder.toString();
	}

	interface Factory {
		CooldownVisualization create(UserConnection user);

		static Factory fromConfiguration() {
			try {
				return fromIndicator(ViaRewind.getConfig().getCooldownIndicator());
			} catch (IllegalArgumentException e) {
				ViaRewind.getPlatform().getLogger().warning("Invalid cooldown-indicator setting");
				return DISABLED;
			}
		}

		static Factory fromIndicator(CooldownIndicator indicator) {
			return switch (indicator) {
				case TITLE -> TitleCooldownVisualization::new;
				case BOSS_BAR -> BossBarVisualization::new;
				case ACTION_BAR -> ActionBarVisualization::new;
				case DISABLED -> DISABLED;
			};
		}

		Factory DISABLED = user -> new DisabledCooldownVisualization();
	}
}
