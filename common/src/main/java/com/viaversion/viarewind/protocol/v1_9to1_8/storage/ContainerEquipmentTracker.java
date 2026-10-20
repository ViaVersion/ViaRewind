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
package com.viaversion.viarewind.protocol.v1_9to1_8.storage;

import com.viaversion.viarewind.protocol.v1_9to1_8.Protocol1_9To1_8;
import com.viaversion.viaversion.api.connection.StorableObject;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.minecraft.item.Item;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.protocols.v1_8to1_9.packet.ClientboundPackets1_8;

import java.util.Arrays;

/**
 * Tracks equipment changes while a foreign container is open.
 */
public class ContainerEquipmentTracker implements StorableObject {
  // 0: helmet, 3: boots
  private final Slot[] equipment = new Slot[4];

  public void setEquipment(final int slot, final Item item) {
    equipment[slot] = new Slot(item);
  }

  public void sendAsSlot(final UserConnection connection, final int slot) {
    if (equipment[slot] == null) {
      // needs no update
      return;
    }

    final PacketWrapper setSlot = PacketWrapper.create(ClientboundPackets1_8.CONTAINER_SET_SLOT, connection);
    setSlot.write(Types.BYTE, (byte) 0);
    setSlot.write(Types.SHORT, (short) (5 + slot));
    setSlot.write(Types.ITEM1_8, equipment[slot].item);
    setSlot.send(Protocol1_9To1_8.class);

    equipment[slot] = null;
  }

  public void sendAll(UserConnection connection) {
    for (int slot = 0; slot < equipment.length; slot++) {
      sendAsSlot(connection, slot);
    }
  }

  public void invalidate() {
    Arrays.fill(equipment, null);
  }

  private record Slot(Item item) {}
}
