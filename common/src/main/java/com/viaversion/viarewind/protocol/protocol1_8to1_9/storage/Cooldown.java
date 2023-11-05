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

import com.viaversion.viarewind.ViaRewind;
import com.viaversion.viarewind.protocol.protocol1_8to1_9.cooldown.CooldownVisualization;
import com.viaversion.viarewind.protocol.protocol1_8to1_9.cooldown.CooldownVisualization.Factory;
import com.viaversion.viarewind.utils.Tickable;
import com.viaversion.viaversion.api.connection.StoredObject;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.util.Pair;

import java.util.ArrayList;
import java.util.Objects;
import java.util.logging.Level;

public class Cooldown extends StoredObject implements Tickable {

	private double attackSpeed = 4.0;
	private long lastHit = 0;
	private CooldownVisualization.Factory visualizationFactory = CooldownVisualization.Factory.fromConfiguration();
	private CooldownVisualization current;

	public Cooldown(final UserConnection user) {
		super(user);
	}

	@Override
	public void tick() {
		if (!hasCooldown()) {
			endCurrentVisualization();
			return;
		}
		BlockPlaceDestroyTracker tracker = getUser().get(BlockPlaceDestroyTracker.class);
		if (tracker.isMining()) {
			lastHit = 0;
			endCurrentVisualization();
			return;
		}
		if (current == null) {
			current = visualizationFactory.create(getUser());
		}
		try {
			current.show(getCooldown());
		} catch (Exception exception) {
			ViaRewind.getPlatform().getLogger().log(Level.WARNING, "Unable to show cooldown visualization", exception);
		}
	}

	private void endCurrentVisualization() {
		if (current != null) {
			try {
				current.hide();
			} catch (Exception exception) {
				ViaRewind.getPlatform().getLogger().log(Level.WARNING, "Unable to hide cooldown visualization", exception);
			}
			current = null;
		}
	}

	public boolean hasCooldown() {
		long time = System.currentTimeMillis() - lastHit;
		double cooldown = restrain(((double) time) * attackSpeed / 1000d, 0, 1.5);
		return cooldown > 0.1 && cooldown < 1.1;
	}

	public double getCooldown() {
		long time = System.currentTimeMillis() - lastHit;
		return restrain(((double) time) * attackSpeed / 1000d, 0, 1);
	}

	private double restrain(double x, double a, double b) {
		if (x < a) return a;
		return Math.min(x, b);
	}

	public double getAttackSpeed() {
		return attackSpeed;
	}

	public void setAttackSpeed(double attackSpeed) {
		this.attackSpeed = attackSpeed;
	}

	public void setAttackSpeed(double base, ArrayList<Pair<Byte, Double>> modifiers) {
		attackSpeed = base;
		for (int j = 0; j < modifiers.size(); j++) {
			if (modifiers.get(j).key() == 0) {
				attackSpeed += modifiers.get(j).value();
				modifiers.remove(j--);
			}
		}
		for (int j = 0; j < modifiers.size(); j++) {
			if (modifiers.get(j).key() == 1) {
				attackSpeed += base * modifiers.get(j).value();
				modifiers.remove(j--);
			}
		}
		for (int j = 0; j < modifiers.size(); j++) {
			if (modifiers.get(j).key() == 2) {
				attackSpeed *= (1.0 + modifiers.get(j).value());
				modifiers.remove(j--);
			}
		}
	}

	public void hit() {
		lastHit = System.currentTimeMillis();
	}

	public void setLastHit(long lastHit) {
		this.lastHit = lastHit;
	}

	public void setVisualizationFactory(Factory visualizationFactory) {
		this.visualizationFactory = Objects.requireNonNull(visualizationFactory, "visualizationFactory");
	}
}
