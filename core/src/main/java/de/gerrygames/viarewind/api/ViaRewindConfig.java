package de.gerrygames.viarewind.api;

public interface ViaRewindConfig {

	enum CooldownIndicator {
		TITLE, ACTION_BAR, BOSS_BAR, DISABLED
	}

	CooldownIndicator getCooldownIndicator();

	boolean isReplaceAdventureMode();

	boolean isReplaceParticles();

	int getMaxBookPages();

	int getMaxBookPageSize();
}
