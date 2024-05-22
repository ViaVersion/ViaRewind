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
package com.viaversion.viarewind.protocol.v1_8to1_7_6_10.provider;

import com.viaversion.viaversion.api.platform.providers.Provider;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

// TODO I believe this should be a storage?
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
		if (time.get() > 0) time.set(getFadeIn(uuid) + getStay(uuid) + getFadeOut(uuid));
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
		if (fadeIn >= 0) this.fadeIn.put(uuid, fadeIn);
		else this.fadeIn.remove(uuid);
	}

	public void setStay(UUID uuid, int stay) {
		if (stay >= 0) this.stay.put(uuid, stay);
		else this.stay.remove(uuid);
	}

	public void setFadeOut(UUID uuid, int fadeOut) {
		if (fadeOut >= 0) this.fadeOut.put(uuid, fadeOut);
		else this.fadeOut.remove(uuid);
	}
}
