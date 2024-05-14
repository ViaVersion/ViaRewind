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
package com.viaversion.viarewind.api.data;

import com.viaversion.viabackwards.api.data.BackwardsMappingDataLoader;
import com.viaversion.viarewind.ViaRewind;

import java.io.File;
import java.util.logging.Logger;

public class RewindMappingDataLoader extends BackwardsMappingDataLoader {

	public static final RewindMappingDataLoader INSTANCE = new RewindMappingDataLoader();

	public RewindMappingDataLoader() {
		super(RewindMappingDataLoader.class, "assets/viarewind/data/");
	}


	@Override
	public Logger getLogger() {
		return ViaRewind.getPlatform().getLogger();
	}

	@Override
	public File getDataFolder() {
		return ViaRewind.getPlatform().getDataFolder();
	}
}
