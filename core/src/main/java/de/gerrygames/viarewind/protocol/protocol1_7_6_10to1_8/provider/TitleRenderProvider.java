package de.gerrygames.viarewind.protocol.protocol1_7_6_10to1_8.provider;

import us.myles.ViaVersion.api.data.UserConnection;
import us.myles.ViaVersion.api.platform.providers.Provider;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class TitleRenderProvider implements Provider {
	protected Map<UserConnection, Integer> fadeIn = new HashMap<>();
	protected Map<UserConnection, Integer> stay = new HashMap<>();
	protected Map<UserConnection, Integer> fadeOut = new HashMap<>();
	protected Map<UserConnection, String> titles = new HashMap<>();
	protected Map<UserConnection, String> subTitles = new HashMap<>();
	protected Map<UserConnection, AtomicInteger> times = new HashMap<>();

	public void setTimings(UserConnection user, int fadeIn, int stay, int fadeOut) {
		setFadeIn(user, fadeIn);
		setStay(user, stay);
		setFadeOut(user, fadeOut);

		AtomicInteger time = getTime(user);
		if (time.get()>0) time.set(getFadeIn(user) + getStay(user) + getFadeOut(user));
	}

	public void reset(UserConnection user) {
		this.titles.remove(user);
		this.subTitles.remove(user);
		getTime(user).set(0);
		fadeIn.remove(user);
		stay.remove(user);
		fadeOut.remove(user);
	}

	public void setTitle(UserConnection user, String title) {
		this.titles.put(user, title);
		getTime(user).set(getFadeIn(user) + getStay(user) + getFadeOut(user));
	}

	public void setSubTitle(UserConnection user, String subTitle) {
		this.subTitles.put(user, subTitle);
	}

	public void clear(UserConnection user) {
		this.titles.remove(user);
		this.subTitles.remove(user);
		getTime(user).set(0);
	}

	public AtomicInteger getTime(UserConnection user) {
		return times.computeIfAbsent(user, key -> new AtomicInteger());
	}

	public int getFadeIn(UserConnection user) {
		return fadeIn.getOrDefault(user, 10);
	}

	public int getStay(UserConnection user) {
		return stay.getOrDefault(user, 70);
	}

	public int getFadeOut(UserConnection user) {
		return fadeOut.getOrDefault(user, 20);
	}

	public void setFadeIn(UserConnection user, int fadeIn) {
		if (fadeIn>0) this.fadeIn.put(user, fadeIn);
		else this.fadeIn.remove(user);
	}

	public void setStay(UserConnection user, int stay) {
		if (stay>0) this.stay.put(user, stay);
		else this.stay.remove(user);
	}

	public void setFadeOut(UserConnection user, int fadeOut) {
		if (fadeOut>0) this.fadeOut.put(user, fadeOut);
		else this.fadeOut.remove(user);
	}
}
