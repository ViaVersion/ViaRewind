package de.gerrygames.viarewind.api;

public interface ViaRewindConfig {

	public enum CooldownIndicator {
		TITLE, ACTION_BAR, DISABLED;
	}

	CooldownIndicator getCooldownIndicator();

	boolean isReplaceAdventureMode();

	boolean isReplaceParticles();
}
