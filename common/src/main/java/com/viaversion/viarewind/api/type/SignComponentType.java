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
package com.viaversion.viarewind.api.type;

import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.api.type.types.ComponentType;
import com.viaversion.viaversion.api.type.types.StringType;
import com.viaversion.viaversion.libs.gson.JsonElement;
import com.viaversion.viaversion.libs.gson.JsonParser;
import com.viaversion.viaversion.libs.gson.JsonSyntaxException;
import io.netty.buffer.ByteBuf;

public class SignComponentType extends ComponentType {

    public static final Type<String> STRING = new StringType(384);

    @Override
    public JsonElement read(final ByteBuf buffer) {
        final String string = STRING.read(buffer);
        try {
            return JsonParser.parseString(string);
        } catch (final JsonSyntaxException e) {
            if (Via.getManager().isDebug()) {
                Via.getPlatform().getLogger().severe("Error when trying to parse json: " + string);
            }
            throw e;
        }
    }

    @Override
    public void write(final ByteBuf buffer, final JsonElement value) {
        STRING.write(buffer, value.toString());
    }

}
