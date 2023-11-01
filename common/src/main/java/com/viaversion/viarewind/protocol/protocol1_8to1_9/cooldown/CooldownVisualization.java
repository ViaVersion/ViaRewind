package com.viaversion.viarewind.protocol.protocol1_8to1_9.cooldown;

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
			switch (indicator) {
				case TITLE:
					return TitleCooldownVisualization::new;
				case BOSS_BAR:
					return BossBarVisualization::new;
				case ACTION_BAR:
					return ActionBarVisualization::new;
				case DISABLED:
					return DISABLED;
				default:
					throw new IllegalArgumentException("Unexpected: " + indicator);
			}
		}

		Factory DISABLED = user -> new DisabledCooldownVisualization();
	}
}
