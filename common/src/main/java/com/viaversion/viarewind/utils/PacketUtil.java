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

package com.viaversion.viarewind.utils;

import com.viaversion.viaversion.api.protocol.Protocol;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.exception.CancelException;

public class PacketUtil {

	public static void sendToServer(PacketWrapper packet, Class<? extends Protocol> packetProtocol) {
		sendToServer(packet, packetProtocol, true);
	}

	public static void sendToServer(PacketWrapper packet, Class<? extends Protocol> packetProtocol, boolean skipCurrentPipeline) {
		sendToServer(packet, packetProtocol, skipCurrentPipeline, false);
	}

	public static void sendToServer(PacketWrapper packet, Class<? extends Protocol> packetProtocol, boolean skipCurrentPipeline, boolean currentThread) {
		try {
		    if (currentThread) {
                packet.sendToServer(packetProtocol, skipCurrentPipeline);
            } else {
                packet.scheduleSendToServer(packetProtocol, skipCurrentPipeline);
            }
		} catch (CancelException ignored) {
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public static boolean sendPacket(PacketWrapper packet, Class<? extends Protocol> packetProtocol) {
		return sendPacket(packet, packetProtocol, true);
	}

	public static boolean sendPacket(PacketWrapper packet, Class<? extends Protocol> packetProtocol, boolean skipCurrentPipeline) {
		return sendPacket(packet, packetProtocol, true, false);
	}

	public static boolean sendPacket(PacketWrapper packet, Class<? extends Protocol> packetProtocol, boolean skipCurrentPipeline, boolean currentThread) {
		try {
            if (currentThread) {
                packet.send(packetProtocol, skipCurrentPipeline);
            } else {
                packet.scheduleSend(packetProtocol, skipCurrentPipeline);
            }
			return true;
		} catch (CancelException ignored) {
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return false;
	}
}
