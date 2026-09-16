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
package com.viaversion.viarewind.protocol.v1_9to1_8.cooldown;

import com.viaversion.viarewind.protocol.v1_9to1_8.Protocol1_9To1_8;
import com.viaversion.viarewind.protocol.v1_9to1_8.storage.LastTitle;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.libs.gson.JsonPrimitive;
import com.viaversion.viaversion.protocols.v1_8to1_9.packet.ClientboundPackets1_8;

public class TitleCooldownVisualization implements CooldownVisualization {

    public static final int ACTION_SET_TITLE = 0;
    public static final int ACTION_SET_SUBTITLE = 1;
    public static final int ACTION_SET_TIMES_AND_DISPLAY = 2;
    public static final int ACTION_HIDE = 3;
    public static final int ACTION_RESET = 4;

    private final UserConnection user;
    private boolean visible;

    public TitleCooldownVisualization(UserConnection user) {
        this.user = user;
    }

    @Override
    public void show(double progress) throws Exception {
        final LastTitle title = this.user.get(LastTitle.class);
        if (this.visible && title.isVisible()) {
            this.restoreTitle(title);
            return;
        }

        final PacketWrapper setTitle = PacketWrapper.create(ClientboundPackets1_8.SET_TITLES, user);
        setTitle.write(Types.VAR_INT, ACTION_SET_TITLE);
        setTitle.write(Types.COMPONENT, new JsonPrimitive(""));
        setTitle.send(Protocol1_9To1_8.class);

        final PacketWrapper setSubtitle = PacketWrapper.create(ClientboundPackets1_8.SET_TITLES, user);
        setSubtitle.write(Types.VAR_INT, ACTION_SET_SUBTITLE);
        setSubtitle.write(Types.COMPONENT, new JsonPrimitive(CooldownVisualization.buildProgressText("˙", progress)));
        setSubtitle.send(Protocol1_9To1_8.class);

        final PacketWrapper setTitleTimes = PacketWrapper.create(ClientboundPackets1_8.SET_TITLES, user);
        setTitleTimes.write(Types.VAR_INT, ACTION_SET_TIMES_AND_DISPLAY);
        setTitleTimes.write(Types.INT, 0);
        setTitleTimes.write(Types.INT, 2);
        setTitleTimes.write(Types.INT, 5);
        setTitleTimes.send(Protocol1_9To1_8.class);
        this.visible = true;
    }

    @Override
    public void hide() throws Exception {
        if (!this.visible) {
            return;
        }

        final LastTitle title = this.user.get(LastTitle.class);
        if (title.isVisible()) {
            this.restoreTitle(title);
            return;
        }

        final PacketWrapper hideTitle = PacketWrapper.create(ClientboundPackets1_8.SET_TITLES, user);
        hideTitle.write(Types.VAR_INT, ACTION_HIDE);
        hideTitle.send(Protocol1_9To1_8.class);
        this.visible = false;
    }

    private void restoreTitle(final LastTitle title) {
        final PacketWrapper setTitleTimes = PacketWrapper.create(ClientboundPackets1_8.SET_TITLES, user);
        setTitleTimes.write(Types.VAR_INT, ACTION_SET_TIMES_AND_DISPLAY);
        final int[] times = title.remainingTimes();
        setTitleTimes.write(Types.INT, times[0]);
        setTitleTimes.write(Types.INT, times[1]);
        setTitleTimes.write(Types.INT, times[2]);
        setTitleTimes.send(Protocol1_9To1_8.class);

        if (title.subtitle() != null) {
            final PacketWrapper setSubtitle = PacketWrapper.create(ClientboundPackets1_8.SET_TITLES, user);
            setSubtitle.write(Types.VAR_INT, ACTION_SET_SUBTITLE);
            setSubtitle.write(Types.COMPONENT, title.subtitle());
            setSubtitle.send(Protocol1_9To1_8.class);
        }

        final PacketWrapper setTitle = PacketWrapper.create(ClientboundPackets1_8.SET_TITLES, user);
        setTitle.write(Types.VAR_INT, ACTION_SET_TITLE);
        setTitle.write(Types.COMPONENT, title.title());
        setTitle.send(Protocol1_9To1_8.class);

        this.visible = false;
    }

}
