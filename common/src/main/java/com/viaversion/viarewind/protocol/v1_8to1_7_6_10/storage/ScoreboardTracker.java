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
package com.viaversion.viarewind.protocol.v1_8to1_7_6_10.storage;

import com.viaversion.viarewind.protocol.v1_7_6_10to1_7_2_5.packet.ClientboundPackets1_7_2_5;
import com.viaversion.viarewind.protocol.v1_8to1_7_6_10.Protocol1_8To1_7_6_10;
import com.viaversion.viaversion.api.connection.StoredObject;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.type.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class ScoreboardTracker extends StoredObject {

    private final HashMap<String, List<String>> teams = new HashMap<>();
    private final HashSet<String> objectives = new HashSet<>();
    private final HashMap<String, ScoreTeam> scoreTeams = new HashMap<>();
    private final HashMap<String, Byte> teamColors = new HashMap<>();
    private final HashSet<String> scoreTeamNames = new HashSet<>();
    private final HashMap<Byte, String> colorDependentSidebar = new HashMap<>();
    private final HashMap<String, String> teamNameTagVisibilities = new HashMap<>();
    private String colorIndependentSidebar;

    public ScoreboardTracker(final UserConnection user) {
        super(user);
    }

    public void addPlayerToTeam(final String player, final String team) {
        teams.computeIfAbsent(team, key -> new ArrayList<>()).add(player);
    }

    public void setTeamColor(final String team, final Byte color) {
        teamColors.put(team, color);
    }

    public Optional<Byte> getTeamColor(final String team) {
        return Optional.ofNullable(teamColors.get(team));
    }

    public void addTeam(final String team) {
        teams.computeIfAbsent(team, key -> new ArrayList<>());
    }

    public void removeTeam(final String team) {
        teams.remove(team);
        scoreTeams.remove(team);
        teamColors.remove(team);
        teamNameTagVisibilities.remove(team);
    }

    public boolean teamExists(final String team) {
        return teams.containsKey(team);
    }

    public void removePlayerFromTeam(String player, String team) {
        final List<String> teamPlayers = teams.get(team);
        if (teamPlayers != null) {
            teamPlayers.remove(player);
        }
    }

    public boolean isPlayerInTeam(final String player, final String team) {
        final List<String> teamPlayers = teams.get(team);
        return teamPlayers != null && teamPlayers.contains(player);
    }


    public Optional<Byte> getPlayerTeamColor(final String player) {
        final Optional<String> team = getTeam(player);
        return team.flatMap(this::getTeamColor);
    }

    public Optional<String> getTeam(final String player) {
        for (Map.Entry<String, List<String>> entry : teams.entrySet()) {
            if (entry.getValue().contains(player)) {
                return Optional.of(entry.getKey());
            }
        }
        return Optional.empty();
    }

    public void setTeamNameTagVisibility(final String team, final String visibility) {
        teamNameTagVisibilities.put(team, visibility);
    }

    public String getTeamNameTagVisibility(final String team) {
        return teamNameTagVisibilities.getOrDefault(team, "always");
    }

    public boolean isNametagHidden(final String username) {
        for (final Map.Entry<String, List<String>> entry : teams.entrySet()) {
            if (entry.getValue().contains(username)) {
                return "never".equalsIgnoreCase(teamNameTagVisibilities.getOrDefault(entry.getKey(), "always"));
            }
        }
        return false;
    }

    public List<String> getTeamMembers(final String team) {
        return teams.getOrDefault(team, new ArrayList<>());
    }

    public void addObjective(final String name) {
        objectives.add(name);
    }

    public void removeObjective(final String name) {
        objectives.remove(name);
        colorDependentSidebar.values().remove(name);
        if (name.equals(colorIndependentSidebar)) {
            colorIndependentSidebar = null;
        }
    }

    public boolean objectiveExists(final String name) {
        return objectives.contains(name);
    }

    public String sendTeamForScore(final String score) {
        if (score.length() <= 16) {
            return score;
        }

        if (scoreTeams.containsKey(score)) {
            return scoreTeams.get(score).name;
        }

        int l = 16;
        int i = Math.min(16, score.length() - 16);
        String name = score.substring(i, i + l);
        while (scoreTeamNames.contains(name) || teams.containsKey(name)) {
            i--;
            while (score.length() - l - i > 16) {
                l--;
                if (l < 1) {
                    return score;
                }
                i = Math.min(16, score.length() - l);
            }
            name = score.substring(i, i + l);
        }
        final String prefix = score.substring(0, i);
        final String suffix = i + l >= score.length() ? "" : score.substring(i + l);

        final ScoreTeam scoreTeam = new ScoreTeam(name, prefix, suffix);
        scoreTeams.put(score, scoreTeam);
        scoreTeamNames.add(name);

        PacketWrapper teamPacket = PacketWrapper.create(ClientboundPackets1_7_2_5.SET_PLAYER_TEAM, user());
        teamPacket.write(Types.STRING, name);
        teamPacket.write(Types.BYTE, (byte) 0);
        teamPacket.write(Types.STRING, "ViaRewind");
        teamPacket.write(Types.STRING, prefix);
        teamPacket.write(Types.STRING, suffix);
        teamPacket.write(Types.BYTE, (byte) 0);
        teamPacket.write(Types.SHORT, (short) 1);
        teamPacket.write(Types.STRING, name);
        teamPacket.send(Protocol1_8To1_7_6_10.class);

        return name;
    }

    public String removeTeamForScore(final String score) {
        final ScoreTeam scoreTeam = scoreTeams.remove(score);
        if (scoreTeam == null) {
            return score;
        }

        scoreTeamNames.remove(scoreTeam.name);

        final PacketWrapper teamPacket = PacketWrapper.create(ClientboundPackets1_7_2_5.SET_PLAYER_TEAM, user());
        teamPacket.write(Types.STRING, scoreTeam.name);
        teamPacket.write(Types.BYTE, (byte) 1);
        teamPacket.send(Protocol1_8To1_7_6_10.class);

        return scoreTeam.name;
    }

    public String getColorIndependentSidebar() {
        return this.colorIndependentSidebar;
    }

    public void setColorIndependentSidebar(final String colorIndependentSidebar) {
        this.colorIndependentSidebar = colorIndependentSidebar;
    }

    public HashMap<Byte, String> getColorDependentSidebar() {
        return this.colorDependentSidebar;
    }

    private record ScoreTeam(String name, String prefix, String suffix) {
    }
}
