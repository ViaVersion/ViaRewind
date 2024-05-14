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
package com.viaversion.viarewind.api;

import com.viaversion.viarewind.ViaRewind;
import com.viaversion.viarewind.ViaRewindConfig;
import com.viaversion.viarewind.protocol.v1_7_6_10to1_7_2_5.Protocol1_7_6_10To1_7_2_5;
import com.viaversion.viarewind.protocol.v1_8to1_7_6_10.Protocol1_8To1_7_6_10;
import com.viaversion.viarewind.protocol.v1_9to1_8.Protocol1_9To1_8;
import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.protocol.ProtocolManager;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;

import java.io.File;
import java.util.logging.Logger;

/**
 * The platform API for ViaRewind
 */
public interface ViaRewindPlatform {

	String VERSION = "${version}";
	String IMPL_VERSION = "${impl_version}";

	/**
	 * Initialize ViaRewind
	 */
	default void init(final File configFile) {
		ViaRewindConfig config = new ViaRewindConfig(configFile, getLogger());
		config.reload();
		Via.getManager().getConfigurationProvider().register(config);

		ViaRewind.init(this, config);

		Via.getManager().getSubPlatforms().add(IMPL_VERSION);

		getLogger().info("Registering protocols...");
		final ProtocolManager protocolManager = Via.getManager().getProtocolManager();
		protocolManager.registerProtocol(new Protocol1_7_6_10To1_7_2_5(), ProtocolVersion.v1_7_2, ProtocolVersion.v1_7_6);
		protocolManager.registerProtocol(new Protocol1_8To1_7_6_10(), ProtocolVersion.v1_7_6, ProtocolVersion.v1_8);

		protocolManager.registerProtocol(new Protocol1_9To1_8(), ProtocolVersion.v1_8, ProtocolVersion.v1_9);
	}

	/**
	 * Get the platform logger
	 *
	 * @return the logger
	 */
	Logger getLogger();

	/**
	 * Returns ViaRewind's data folder.
	 *
	 * @return data folder
	 */
	File getDataFolder();
}
