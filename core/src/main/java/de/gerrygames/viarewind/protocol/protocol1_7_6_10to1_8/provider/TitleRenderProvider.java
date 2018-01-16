package de.gerrygames.viarewind.protocol.protocol1_7_6_10to1_8.provider;

import us.myles.ViaVersion.api.data.UserConnection;
import us.myles.ViaVersion.api.platform.providers.Provider;

import java.util.HashMap;
import java.util.Map;

public abstract class TitleRenderProvider implements Provider {
	protected Map<UserConnection, Integer> fadeIn = new HashMap<>();
	protected Map<UserConnection, Integer> stay = new HashMap<>();
	protected Map<UserConnection, Integer> fadeOut = new HashMap<>();
	protected Map<UserConnection, String> titles = new HashMap<>();
	protected Map<UserConnection, String> subTitles = new HashMap<>();

	public void setTimings(UserConnection user, int fadeIn, int stay, int fadeOut) {
		this.fadeIn.put(user, fadeIn);
		this.stay.put(user, stay);
		this.fadeOut.put(user, fadeOut);
	}

	public void reset(UserConnection user) {
		hide(user);
		this.titles.remove(user);
		this.subTitles.remove(user);
	}

	public void setTitle(UserConnection user, String title) {
		this.titles.put(user, title);
		display(user);
	}

	public void setSubTitle(UserConnection user, String subTitle) {
		this.subTitles.put(user, subTitle);
	}

	public void hide(UserConnection user) {
		this.fadeIn.remove(user);
		this.stay.remove(user);
		this.fadeOut.remove(user);
	}

	public abstract void display(UserConnection user);
}
