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

import com.viaversion.viaversion.api.connection.StorableObject;
import com.viaversion.viaversion.libs.gson.JsonElement;

public final class LastTitle implements StorableObject {

    private JsonElement title;
    private JsonElement subtitle;

    private long visibleSince;
    private long visibleUntil;

    private int fadeIn;
    private int stay;
    private int fadeOut;

    public LastTitle() {
        this.reset();
    }

    public void setTitle(final JsonElement title) {
        this.title = title;

        final long now = System.currentTimeMillis();
        this.visibleSince = now;
        this.visibleUntil = now + titleLength();
    }

    public void setSubtitle(final JsonElement subtitle) {
        this.subtitle = subtitle;
    }

    public void setTimes(final int fadeIn, final int stay, final int fadeOut) {
        this.fadeIn = fadeIn;
        this.stay = stay;
        this.fadeOut = fadeOut;

        if (this.visibleUntil > System.currentTimeMillis()) {
            final long now = System.currentTimeMillis();
            this.visibleSince = now;
            this.visibleUntil = now + titleLength();
        }
    }

    public void hide() {
        this.visibleSince = 0;
        this.visibleUntil = 0;
    }

    public void reset() {
        this.hide();

        this.fadeIn = 10;
        this.stay = 70;
        this.fadeOut = 20;
    }

    public boolean isVisible() {
        return this.title != null && this.visibleUntil > System.currentTimeMillis();
    }

    public JsonElement title() {
        return this.title;
    }

    public JsonElement subtitle() {
        return this.subtitle;
    }

    public int[] remainingTimes() {
        final int fullLength = Math.max(0, this.fadeIn + this.stay + this.fadeOut);
        final int elapsed = (int) Math.min(fullLength, Math.max(0, (System.currentTimeMillis() - this.visibleSince) / 50L));
        if (elapsed < this.fadeIn) {
            return new int[]{this.fadeIn - elapsed, this.stay, this.fadeOut};
        }

        final int titleEnd = this.fadeIn + this.stay;
        if (elapsed < titleEnd) {
            return new int[]{0, titleEnd - elapsed, this.fadeOut};
        }

        return new int[]{0, 0, fullLength - elapsed};
    }

    private long titleLength() {
        return Math.max(0, this.fadeIn + this.stay + this.fadeOut) * 50L;
    }

}
