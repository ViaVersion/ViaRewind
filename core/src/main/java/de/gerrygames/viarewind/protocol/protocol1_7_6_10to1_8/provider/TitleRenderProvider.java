package de.gerrygames.viarewind.protocol.protocol1_7_6_10to1_8.provider;

import us.myles.ViaVersion.api.platform.providers.Provider;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class TitleRenderProvider implements Provider {
	protected Map<UUID, Integer> fadeIn = new HashMap<>();
	protected Map<UUID, Integer> stay = new HashMap<>();
	protected Map<UUID, Integer> fadeOut = new HashMap<>();
	protected Map<UUID, String> titles = new HashMap<>();
	protected Map<UUID, String> subTitles = new HashMap<>();
	protected Map<UUID, AtomicInteger> times = new HashMap<>();

	public void setTimings(UUID uuid, int fadeIn, int stay, int fadeOut) {
		setFadeIn(uuid, fadeIn);
		setStay(uuid, stay);
		setFadeOut(uuid, fadeOut);

		AtomicInteger time = getTime(uuid);
		if (time.get()>0) time.set(getFadeIn(uuid) + getStay(uuid) + getFadeOut(uuid));
	}

	public void reset(UUID uuid) {
		this.titles.remove(uuid);
		this.subTitles.remove(uuid);
		getTime(uuid).set(0);
		fadeIn.remove(uuid);
		stay.remove(uuid);
		fadeOut.remove(uuid);
	}

	public void setTitle(UUID uuid, String title) {
		this.titles.put(uuid, title);
		getTime(uuid).set(getFadeIn(uuid) + getStay(uuid) + getFadeOut(uuid));
	}

	public void setSubTitle(UUID uuid, String subTitle) {
		this.subTitles.put(uuid, subTitle);
	}

	public void clear(UUID uuid) {
		this.titles.remove(uuid);
		this.subTitles.remove(uuid);
		getTime(uuid).set(0);
	}

	public AtomicInteger getTime(UUID uuid) {
		return times.computeIfAbsent(uuid, key -> new AtomicInteger(0));
	}

	public int getFadeIn(UUID uuid) {
		return fadeIn.getOrDefault(uuid, 10);
	}

	public int getStay(UUID uuid) {
		return stay.getOrDefault(uuid, 70);
	}

	public int getFadeOut(UUID uuid) {
		return fadeOut.getOrDefault(uuid, 20);
	}

	public void setFadeIn(UUID uuid, int fadeIn) {
		if (fadeIn>=0) this.fadeIn.put(uuid, fadeIn);
		else this.fadeIn.remove(uuid);
	}

	public void setStay(UUID uuid, int stay) {
		if (stay>=0) this.stay.put(uuid, stay);
		else this.stay.remove(uuid);
	}

	public void setFadeOut(UUID uuid, int fadeOut) {
		if (fadeOut>=0) this.fadeOut.put(uuid, fadeOut);
		else this.fadeOut.remove(uuid);
	}
}
