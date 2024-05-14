/*
 * This file is part of ViaRewind - https://github.com/ViaVersion/ViaRewind
 * Copyright (C) 2018-2024 ViaVersion and contributors
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

import java.util.*;

public class ScoreboardTracker extends StoredObject {
	private final HashMap<String, List<String>> teams = new HashMap<>();
	private final HashSet<String> objectives = new HashSet<>();
	private final HashMap<String, ScoreTeam> scoreTeams = new HashMap<>();
	private final HashMap<String, Byte> teamColors = new HashMap<>();
	private final HashSet<String> scoreTeamNames = new HashSet<>();
	private String colorIndependentSidebar;
	private final HashMap<Byte, String> colorDependentSidebar = new HashMap<>();

	public ScoreboardTracker(UserConnection user) {
		super(user);
	}

	public void addPlayerToTeam(String player, String team) {
		teams.computeIfAbsent(team, key -> new ArrayList<>()).add(player);
	}

	public void setTeamColor(String team, Byte color) {
		teamColors.put(team, color);
	}

	public Optional<Byte> getTeamColor(String team) {
		return Optional.ofNullable(teamColors.get(team));
	}

	public void addTeam(String team) {
		teams.computeIfAbsent(team, key -> new ArrayList<>());
	}

	public void removeTeam(String team) {
		teams.remove(team);
		scoreTeams.remove(team);
		teamColors.remove(team);
	}

	public boolean teamExists(String team) {
		return teams.containsKey(team);
	}

	public void removePlayerFromTeam(String player, String team) {
		List<String> teamPlayers = teams.get(team);
		if (teamPlayers != null) teamPlayers.remove(player);
	}

	public boolean isPlayerInTeam(String player, String team) {
		List<String> teamPlayers = teams.get(team);
		return teamPlayers != null && teamPlayers.contains(player);
	}

	public boolean isPlayerInTeam(String player) {
		for (List<String> teamPlayers : teams.values()) {
			if (teamPlayers.contains(player)) return true;
		}
		return false;
	}

	public Optional<Byte> getPlayerTeamColor(String player) {
		Optional<String> team = getTeam(player);
		return team.isPresent() ? getTeamColor(team.get()) : Optional.empty();
	}

	public Optional<String> getTeam(String player) {
		for (Map.Entry<String, List<String>> entry : teams.entrySet())
			if (entry.getValue().contains(player))
				return Optional.of(entry.getKey());
		return Optional.empty();
	}

	public void addObjective(String name) {
		objectives.add(name);
	}

	public void removeObjective(String name) {
		objectives.remove(name);
		colorDependentSidebar.values().remove(name);
		if (name.equals(colorIndependentSidebar)) {
			colorIndependentSidebar = null;
		}
	}

	public boolean objectiveExists(String name) {
		return objectives.contains(name);
	}

	public String sendTeamForScore(String score) {
		if (score.length() <= 16) return score;
		if (scoreTeams.containsKey(score)) return scoreTeams.get(score).name;
		int l = 16;
		int i = Math.min(16, score.length() - 16);
		String name = score.substring(i, i + l);
		while (scoreTeamNames.contains(name) || teams.containsKey(name)) {
			i--;
			while (score.length() - l - i > 16) {
				l--;
				if (l < 1) return score;
				i = Math.min(16, score.length() - l);
			}
			name = score.substring(i, i + l);
		}
		String prefix = score.substring(0, i);
		String suffix = i + l >= score.length() ? "" : score.substring(i + l);

		ScoreTeam scoreTeam = new ScoreTeam(name, prefix, suffix);
		scoreTeams.put(score, scoreTeam);
		scoreTeamNames.add(name);

		PacketWrapper teamPacket = PacketWrapper.create(ClientboundPackets1_7_2_5.SET_PLAYER_TEAM, getUser());
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

	public String removeTeamForScore(String score) {
		ScoreTeam scoreTeam = scoreTeams.remove(score);
		if (scoreTeam == null) return score;
		scoreTeamNames.remove(scoreTeam.name);

		PacketWrapper teamPacket = PacketWrapper.create(ClientboundPackets1_7_2_5.SET_PLAYER_TEAM, getUser());
		teamPacket.write(Types.STRING, scoreTeam.name);
		teamPacket.write(Types.BYTE, (byte) 1);
		teamPacket.send(Protocol1_8To1_7_6_10.class);

		return scoreTeam.name;
	}

	public String getColorIndependentSidebar() {
		return this.colorIndependentSidebar;
	}

	public HashMap<Byte, String> getColorDependentSidebar() {
		return this.colorDependentSidebar;
	}

	public void setColorIndependentSidebar(String colorIndependentSidebar) {
		this.colorIndependentSidebar = colorIndependentSidebar;
	}

	private static class ScoreTeam {
		private final String prefix;
		private final String suffix;
		private final String name;

		public ScoreTeam(String name, String prefix, String suffix) {
			this.prefix = prefix;
			this.suffix = suffix;
			this.name = name;
		}
	}
}
