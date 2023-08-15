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

package de.gerrygames.viarewind.replacement;

import com.viaversion.viaversion.api.minecraft.metadata.Metadata;

import java.util.List;

public interface EntityReplacement {

    int getEntityId();

    void setLocation(double x, double y, double z);

    void relMove(double x, double y, double z);

    void setYawPitch(float yaw, float pitch);

    void setHeadYaw(float yaw);

    void spawn();

    void despawn();

    void updateMetadata(List<Metadata> metadataList);
}
