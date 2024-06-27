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
package com.viaversion.viarewind;

import com.viaversion.viaversion.util.Config;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class ViaRewindConfig extends Config implements com.viaversion.viarewind.api.ViaRewindConfig {

	private CooldownIndicator cooldownIndicator;
	private boolean replaceAdventureMode;
	private boolean replaceParticles;
	private int maxBookPages;
	private int maxBookPageSize;
	private boolean emulateWorldBorder;
	private boolean alwaysShowOriginalMobName;
	private String worldBorderParticle;
	private boolean enableOffhand;
	private String offhandCommand;
	private boolean emulateLevitationEffect;

    public ViaRewindConfig(File configFile, Logger logger) {
        super(configFile, logger);
    }

	@Override
	public void reload() {
		super.reload();
		loadFields();
	}

	private void loadFields() {
		cooldownIndicator = CooldownIndicator.valueOf(getString("cooldown-indicator", "TITLE").toUpperCase());
		replaceAdventureMode = getBoolean("replace-adventure", false);
		replaceParticles = getBoolean("replace-particles", false);
		maxBookPages = getInt("max-book-pages", 100);
		maxBookPageSize = getInt("max-book-page-length", 5000);
		emulateWorldBorder = getBoolean("emulate-world-border", true);
		alwaysShowOriginalMobName = getBoolean("always-show-original-mob-name", true);
		worldBorderParticle = getString("world-border-particle", "fireworksSpark");
		enableOffhand = getBoolean("enable-offhand", true);
		offhandCommand = getString("offhand-command", "/offhand");
		emulateLevitationEffect = getBoolean("emulate-levitation-effect", true);
	}

	@Override
    public CooldownIndicator getCooldownIndicator() {
		return cooldownIndicator;
    }

    @Override
    public boolean isReplaceAdventureMode() {
		return replaceAdventureMode;
    }

    @Override
    public boolean isReplaceParticles() {
		return replaceParticles;
    }

    @Override
    public int getMaxBookPages() {
		return maxBookPages;
    }

    @Override
    public int getMaxBookPageSize() {
		return maxBookPageSize;
    }

	@Override
	public boolean isEmulateWorldBorder() {
		return emulateWorldBorder;
	}

	@Override
	public boolean alwaysShowOriginalMobName() {
		return alwaysShowOriginalMobName;
	}

	@Override
	public String getWorldBorderParticle() {
		return worldBorderParticle;
	}

	@Override
	public boolean isEnableOffhand() {
		return enableOffhand;
	}

	@Override
	public String getOffhandCommand() {
		return offhandCommand;
	}

	@Override
	public boolean emulateLevitationEffect() {
		return emulateLevitationEffect;
	}

	@Override
    public URL getDefaultConfigURL() {
        return getClass().getClassLoader().getResource("assets/viarewind/config.yml");
    }

    @Override
    public InputStream getDefaultConfigInputStream() {
        return getClass().getClassLoader().getResourceAsStream("assets/viarewind/config.yml");
    }

    @Override
    protected void handleConfig(Map<String, Object> map) {
    }

    @Override
    public List<String> getUnsupportedOptions() {
        return Collections.emptyList();
    }
}
