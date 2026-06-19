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
import com.viaversion.viaversion.api.connection.StorableObject;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.libs.gson.JsonElement;
import com.viaversion.viaversion.libs.gson.JsonPrimitive;
import com.viaversion.viaversion.protocols.v1_8to1_9.packet.ClientboundPackets1_8;
import java.util.function.Consumer;

public class TitleCooldownVisualization implements CooldownVisualization {

    private static final int ACTION_SET_TITLE = 0;
    private static final int ACTION_SET_SUBTITLE = 1;
    private static final int ACTION_SET_TIMES_AND_DISPLAY = 2;
    private static final int ACTION_HIDE = 3;
    private static final int ACTION_RESET = 4;

    private final UserConnection user;
    private boolean visible;

    public TitleCooldownVisualization(UserConnection user) {
        this.user = user;
    }

    public static void trackTitle(PacketWrapper wrapper) {
        final StoredTitle title = wrapper.user().get(StoredTitle.class);
        final int action = wrapper.passthrough(Types.VAR_INT);
        if (action == ACTION_SET_TITLE) {
            title.setTitle(wrapper.passthrough(Types.COMPONENT));
        } else if (action == ACTION_SET_SUBTITLE) {
            title.setSubtitle(wrapper.passthrough(Types.COMPONENT));
        } else if (action == ACTION_SET_TIMES_AND_DISPLAY) {
            final int fadeIn = wrapper.passthrough(Types.INT);
            final int stay = wrapper.passthrough(Types.INT);
            final int fadeOut = wrapper.passthrough(Types.INT);
            title.setTimes(fadeIn, stay, fadeOut);
        } else if (action == ACTION_HIDE) {
            title.clear();
        } else if (action == ACTION_RESET) {
            title.reset();
        }
    }

    @Override
    public void show(double progress) throws Exception {
        final StoredTitle title = user.get(StoredTitle.class);
        if (title.isVisible() && title.title() != null) {
            if (visible) {
                restoreTitle(title);
            }
            return;
        }

        final String text = CooldownVisualization.buildProgressText("˙", progress);

        sendTitlePacket(ACTION_SET_TITLE, wrapper -> wrapper.write(Types.COMPONENT, new JsonPrimitive("")));
        sendTitlePacket(ACTION_SET_SUBTITLE, wrapper -> wrapper.write(Types.COMPONENT, new JsonPrimitive(text)));
        sendTitlePacket(ACTION_SET_TIMES_AND_DISPLAY, wrapper -> {
            wrapper.write(Types.INT, 0);
            wrapper.write(Types.INT, 2);
            wrapper.write(Types.INT, 5);
        });
        visible = true;
    }

    @Override
    public void hide() throws Exception {
        if (!visible) {
            return;
        }

        final StoredTitle title = user.get(StoredTitle.class);
        if (title.isVisible() && title.title() != null) {
            restoreTitle(title);
            return;
        }

        sendTitlePacket(ACTION_HIDE, wrapper -> {
        });
        visible = false;
    }

    private void restoreTitle(StoredTitle title) {
        final int[] times = title.remainingTimes();
        sendTitlePacket(ACTION_SET_TIMES_AND_DISPLAY, wrapper -> {
            wrapper.write(Types.INT, times[0]);
            wrapper.write(Types.INT, times[1]);
            wrapper.write(Types.INT, times[2]);
        });
        if (title.subtitle() != null) {
            sendTitlePacket(ACTION_SET_SUBTITLE, wrapper -> wrapper.write(Types.COMPONENT, title.subtitle()));
        }
        sendTitlePacket(ACTION_SET_TITLE, wrapper -> wrapper.write(Types.COMPONENT, title.title()));
        visible = false;
    }

    private void sendTitlePacket(int action, Consumer<PacketWrapper> writer) {
        final PacketWrapper title = PacketWrapper.create(ClientboundPackets1_8.SET_TITLES, user);
        title.write(Types.VAR_INT, action);
        writer.accept(title);
        title.scheduleSend(Protocol1_9To1_8.class);
    }

    public static final class StoredTitle implements StorableObject {
        private JsonElement title;
        private JsonElement subtitle;
        private int fadeIn = 10;
        private int stay = 70;
        private int fadeOut = 20;
        private long visibleSince;
        private long visibleUntil;

        public void setTitle(JsonElement title) {
            final long now = System.currentTimeMillis();
            this.title = title;
            visibleSince = now;
            visibleUntil = now + titleLength();
        }

        public void setSubtitle(JsonElement subtitle) {
            this.subtitle = subtitle;
        }

        public void setTimes(int fadeIn, int stay, int fadeOut) {
            final long now = System.currentTimeMillis();
            this.fadeIn = fadeIn;
            this.stay = stay;
            this.fadeOut = fadeOut;
            if (isVisible()) {
                visibleSince = now;
                visibleUntil = now + titleLength();
            }
        }

        public void clear() {
            visibleSince = 0;
            visibleUntil = 0;
        }

        public void reset() {
            clear();
            title = null;
            subtitle = null;
            fadeIn = 10;
            stay = 70;
            fadeOut = 20;
        }

        public boolean isVisible() {
            return visibleUntil > System.currentTimeMillis();
        }

        public JsonElement title() {
            return title;
        }

        public JsonElement subtitle() {
            return subtitle;
        }

        public int[] remainingTimes() {
            final int fullLength = Math.max(0, fadeIn + stay + fadeOut);
            final int elapsed = (int) Math.min(fullLength, Math.max(0, (System.currentTimeMillis() - visibleSince) / 50L));
            if (elapsed < fadeIn) {
                return new int[]{fadeIn - elapsed, stay, fadeOut};
            }

            final int titleEnd = fadeIn + stay;
            if (elapsed < titleEnd) {
                return new int[]{0, titleEnd - elapsed, fadeOut};
            }

            return new int[]{0, 0, fullLength - elapsed};
        }

        private long titleLength() {
            return Math.max(0, fadeIn + stay + fadeOut) * 50L;
        }
    }

}
