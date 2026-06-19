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
package com.viaversion.viarewind.protocol.v1_9to1_8.data;

import com.viaversion.nbt.tag.CompoundTag;
import com.viaversion.nbt.tag.NumberTag;
import com.viaversion.nbt.tag.StringTag;

public final class CommandBlockState {

    public static final int COMMAND_BLOCK_COMMAND_LIMIT = 32500;

    private static final String IMPULSE = "IMPULSE";
    private static final String REPEAT = "REPEAT";
    private static final String CHAIN = "CHAIN";
    private static final String CONDITIONAL = "CONDITIONAL";
    private static final String UNCONDITIONAL = "UNCONDITIONAL";
    private static final String ALWAYS_ACTIVE = "ALWAYSACTIVE";
    private static final String REDSTONE = "REDSTONE";

    private CommandBlockState() {
    }

    public static boolean isCommandBlock(final int blockState) {
        return isImpulse(blockState) || isRepeat(blockState) || isChain(blockState);
    }

    public static void decorateCommand(final CompoundTag tag, final int blockState) {
        if (tag == null || !isCommandBlock(blockState)) {
            return;
        }

        final StringTag commandTag = tag.getStringTag("Command");
        if (commandTag == null) {
            return;
        }

        final Mode mode = mode(blockState);
        final boolean conditional = (blockState & 8) != 0;
        final boolean automatic = booleanTag(tag, "auto");
        if (mode == Mode.REDSTONE && !conditional && !automatic) {
            return;
        }

        final String prefix = mode.prefix + ' ' + (conditional ? CONDITIONAL : UNCONDITIONAL) + ' ' + (automatic ? ALWAYS_ACTIVE : REDSTONE) + ' ';
        final String command = commandTag.getValue();
        if (!parseDecoratedCommand(command).prefixed() && prefix.length() + command.length() <= COMMAND_BLOCK_COMMAND_LIMIT) {
            commandTag.setValue(prefix + command);
        }
    }

    public static ParsedCommand parseDecoratedCommand(final String command) {
        final int first = command.indexOf(' ');
        if (first == -1) {
            return ParsedCommand.defaultState(command);
        }
        final int second = command.indexOf(' ', first + 1);
        if (second == -1) {
            return ParsedCommand.defaultState(command);
        }
        final int third = command.indexOf(' ', second + 1);
        if (third == -1) {
            return ParsedCommand.defaultState(command);
        }

        final Mode mode = Mode.fromPrefix(command.substring(0, first));
        if (mode == null) {
            return ParsedCommand.defaultState(command);
        }

        final String conditionalToken = command.substring(first + 1, second);
        final boolean conditional;
        if (CONDITIONAL.equalsIgnoreCase(conditionalToken)) {
            conditional = true;
        } else if (UNCONDITIONAL.equalsIgnoreCase(conditionalToken)) {
            conditional = false;
        } else {
            return ParsedCommand.defaultState(command);
        }

        final String automaticToken = command.substring(second + 1, third);
        final boolean automatic;
        if (ALWAYS_ACTIVE.equalsIgnoreCase(automaticToken)) {
            automatic = true;
        } else if (REDSTONE.equalsIgnoreCase(automaticToken)) {
            automatic = false;
        } else {
            return ParsedCommand.defaultState(command);
        }

        return new ParsedCommand(command.substring(third + 1), mode.serverName, conditional, automatic, true);
    }

    private static boolean booleanTag(final CompoundTag tag, final String key) {
        final NumberTag numberTag = tag.getNumberTag(key);
        return numberTag != null && numberTag.asByte() != 0;
    }

    private static Mode mode(final int blockState) {
        if (isRepeat(blockState)) {
            return Mode.AUTO;
        }
        if (isChain(blockState)) {
            return Mode.SEQUENCE;
        }
        return Mode.REDSTONE;
    }

    private static boolean isImpulse(final int blockState) {
        return blockState >= 2192 && blockState <= 2207;
    }

    private static boolean isRepeat(final int blockState) {
        return blockState >= 3360 && blockState <= 3375;
    }

    private static boolean isChain(final int blockState) {
        return blockState >= 3376 && blockState <= 3391;
    }

    public record ParsedCommand(String command, String mode, boolean conditional, boolean automatic, boolean prefixed) {

        private static ParsedCommand defaultState(final String command) {
            return new ParsedCommand(command, Mode.REDSTONE.serverName, false, false, false);
        }
    }

    private enum Mode {
        REDSTONE(IMPULSE, "REDSTONE"),
        AUTO(REPEAT, "AUTO"),
        SEQUENCE(CHAIN, "SEQUENCE");

        private final String prefix;
        private final String serverName;

        Mode(final String prefix, final String serverName) {
            this.prefix = prefix;
            this.serverName = serverName;
        }

        private static Mode fromPrefix(final String prefix) {
            for (final Mode mode : values()) {
                if (mode.prefix.equalsIgnoreCase(prefix)) {
                    return mode;
                }
            }
            return null;
        }
    }
}
