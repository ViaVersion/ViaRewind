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

import com.viaversion.viarewind.protocol.v1_8to1_7_6_10.storage.CompressionStatusTracker;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.platform.providers.Provider;
import io.netty.channel.ChannelHandler;

public abstract class CompressionHandlerProvider implements Provider {

	public abstract void onHandleLoginCompressionPacket(UserConnection user, int threshold);
	public abstract void onTransformPacket(UserConnection user);

	public abstract ChannelHandler getEncoder(int threshold);
	public abstract ChannelHandler getDecoder(int threshold);

	public boolean isCompressionEnabled(UserConnection user) {
		return user.get(CompressionStatusTracker.class).removeCompression;
	}

	public void setCompressionEnabled(UserConnection user, boolean enabled) {
		user.get(CompressionStatusTracker.class).removeCompression = enabled;
	}
}
