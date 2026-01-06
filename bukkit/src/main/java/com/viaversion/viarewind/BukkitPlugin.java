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

import com.viaversion.viarewind.api.ViaRewindPlatform;
import com.viaversion.viarewind.protocol.v1_9to1_8.provider.InventoryProvider;
import com.viaversion.viarewind.provider.BukkitInventoryProvider;
import com.viaversion.viaversion.api.Via;
import java.io.File;
import com.viaversion.viaversion.api.platform.providers.ViaProviders;
import org.bukkit.plugin.java.JavaPlugin;

public class BukkitPlugin extends JavaPlugin implements ViaRewindPlatform {

    public BukkitPlugin() {
        Via.getManager().addEnableListener(() -> this.init(new File(getDataFolder(), "config.yml")));
    }

    @Override
    public void onEnable() {
        final ViaProviders providers = Via.getManager().getProviders();
        providers.use(InventoryProvider.class, new BukkitInventoryProvider());
    }
}
