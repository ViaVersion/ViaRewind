/*
 * This file is part of ViaRewind - https://github.com/ViaVersion/ViaRewind
 * Copyright (C) 2018-2026 ViaVersion and contributors
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

import com.google.common.base.Preconditions;
import com.viaversion.viarewind.api.ViaRewindConfig;
import com.viaversion.viarewind.api.ViaRewindPlatform;

public class ViaRewind {

    private static ViaRewindPlatform platform;
    private static ViaRewindConfig config;

    public static void init(ViaRewindPlatform platform, ViaRewindConfig config) {
        Preconditions.checkArgument(ViaRewind.platform == null, "ViaRewind is already initialized");

        ViaRewind.platform = platform;
        ViaRewind.config = config;
    }

    public static ViaRewindPlatform getPlatform() {
        return ViaRewind.platform;
    }

    public static ViaRewindConfig getConfig() {
        return ViaRewind.config;
    }
}
