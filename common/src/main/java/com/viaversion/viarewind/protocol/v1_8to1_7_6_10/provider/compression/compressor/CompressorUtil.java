/*
 * This file is part of ViaRewind - https://github.com/ViaVersion/ViaRewind
 * Copyright (C) 2018-2025 ViaVersion and contributors
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
package com.viaversion.viarewind.protocol.v1_8to1_7_6_10.provider.compression.compressor;

public class CompressorUtil {
    public static boolean VELOCITY_COMPRESSION_PRESENT;

    static {
        try {
            Class.forName("com.velocitypowered.natives.compression.VelocityCompressor");
            VELOCITY_COMPRESSION_PRESENT = true;
        } catch (ClassNotFoundException e) {

        }
    }

    public static ThreadLocal<Compressor> COMPRESSOR = ThreadLocal.withInitial(() -> VELOCITY_COMPRESSION_PRESENT ? new VelocityCompressor() : new JavaCompressor());

    public static Compressor getCompressor() {
        return COMPRESSOR.get();
    }
}
