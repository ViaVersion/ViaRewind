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

package de.gerrygames.viarewind;

import com.viaversion.viaversion.util.Config;

import java.io.File;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class ViaRewindConfig extends Config implements de.gerrygames.viarewind.api.ViaRewindConfig {
    public ViaRewindConfig(File configFile) {
        super(configFile);
        reloadConfig();
    }

    @Override
    public CooldownIndicator getCooldownIndicator() {
        return CooldownIndicator.valueOf(getString("cooldown-indicator", "TITLE").toUpperCase());
    }

    @Override
    public boolean isReplaceAdventureMode() {
        return getBoolean("replace-adventure", false);
    }

    @Override
    public boolean isReplaceParticles() {
        return getBoolean("replace-particles", false);
    }

    @Override
    public int getMaxBookPages() {
        return getInt("max-book-pages", 100);
    }

    @Override
    public int getMaxBookPageSize() {
        return getInt("max-book-page-length", 5000);
    }

    @Override
    public URL getDefaultConfigURL() {
        return getClass().getClassLoader().getResource("assets/viarewind/config.yml");
    }

    @Override
    protected void handleConfig(Map<String, Object> map) {

    }

    @Override
    public List<String> getUnsupportedOptions() {
        return Collections.emptyList();
    }
}
