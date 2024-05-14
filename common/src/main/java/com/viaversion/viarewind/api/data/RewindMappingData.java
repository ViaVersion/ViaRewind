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

import com.viaversion.viabackwards.api.data.BackwardsMappingData;
import com.viaversion.viarewind.ViaRewind;
import com.viaversion.nbt.tag.CompoundTag;

import java.util.logging.Logger;

public class RewindMappingData extends BackwardsMappingData {

	public RewindMappingData(String unmappedVersion, String mappedVersion) {
		super(unmappedVersion, mappedVersion);
	}

	@Override
	protected Logger getLogger() {
		return ViaRewind.getPlatform().getLogger();
	}

	@Override
	protected CompoundTag readMappingsFile(String name) {
		return RewindMappingDataLoader.INSTANCE.loadNBTFromDir(name);
	}
}
