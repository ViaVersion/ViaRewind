package de.gerrygames.viarewind.utils;

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
