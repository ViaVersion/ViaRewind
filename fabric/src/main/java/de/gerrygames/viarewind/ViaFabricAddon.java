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

import de.gerrygames.viarewind.api.ViaRewindPlatform;
import de.gerrygames.viarewind.fabric.util.LoggerWrapper;
import net.fabricmc.loader.api.FabricLoader;
import org.apache.logging.log4j.LogManager;

import java.util.logging.Logger;

public class ViaFabricAddon implements ViaRewindPlatform, Runnable {
    private final Logger logger = new LoggerWrapper(LogManager.getLogger("ViaRewind"));

    @Override
    public void run() {
        ViaRewindConfig conf = new ViaRewindConfig(FabricLoader.getInstance().getConfigDirectory().toPath().resolve("ViaRewind").resolve("config.yml").toFile());
        conf.reloadConfig();
        this.init(conf);
    }

	public Logger getLogger() {
		return this.logger;
	}
}