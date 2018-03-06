package de.gerrygames.viarewind.protocol.protocol1_7_6_10to1_8.storage;

import de.gerrygames.viarewind.protocol.protocol1_7_6_10to1_8.Protocol1_7_6_10TO1_8;
import de.gerrygames.viarewind.utils.PacketUtil;
import us.myles.ViaVersion.api.PacketWrapper;
import us.myles.ViaVersion.api.data.StoredObject;
import us.myles.ViaVersion.api.data.UserConnection;
import us.myles.ViaVersion.api.type.Type;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class Scoreboard extends StoredObject {
	private HashMap<String, List<String>> teams = new HashMap<>();
	private HashSet<String> objectives = new HashSet<>();
	private HashMap<String, ScoreTeam> scoreTeams = new HashMap<>();

	public Scoreboard(UserConnection user) {
		super(user);
	}

	public void addPlayerToTeam(String player, String team) {
		teams.computeIfAbsent(team, key -> new ArrayList<>()).add(player);
	}

	public void addTeam(String team) {
		teams.computeIfAbsent(team, key -> new ArrayList<>());
	}

	public void removeTeam(String team) {
		teams.remove(team);
	}

	public boolean teamExists(String team) {
		return teams.containsKey(team);
	}

	public void removePlayerFromTeam(String player, String team) {
		List<String> teamPlayers = teams.get(team);
		if (teamPlayers!=null) teamPlayers.remove(player);
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

	public void addObjective(String name) {
		objectives.add(name);
	}

	public void removeObjective(String name) {
		objectives.remove(name);
	}

	public boolean objectiveExists(String name) {
		return objectives.contains(name);
	}

	public String sendTeamForScore(String score) {
		if (score.length()<=16) return score;
		int l = 16;
		int i = Math.min(16, score.length()-16);
		String name = score.substring(i, i+l);
		while (scoreTeams.containsKey(name) || teams.containsKey(name)) {
			i--;
			while (score.length()-l-i>16) {
				l--;
				if (l<1) return score;
				i = Math.min(16, score.length()-l);
			}
			name = score.substring(i, i+l);
		}
		String prefix = score.substring(0, i);
		String suffix = i+l>=score.length() ? "" : score.substring(i+l, score.length());

		ScoreTeam scoreTeam = new ScoreTeam(name, prefix, suffix);
		scoreTeams.put(score, scoreTeam);

		PacketWrapper teamPacket = new PacketWrapper(0x3E, null, getUser());
		teamPacket.write(Type.STRING, name);
		teamPacket.write(Type.BYTE, (byte) 0);
		teamPacket.write(Type.STRING, "ViaRewind");
		teamPacket.write(Type.STRING, prefix);
		teamPacket.write(Type.STRING, suffix);
		teamPacket.write(Type.BYTE, (byte) 0);
		teamPacket.write(Type.SHORT, (short) 1);
		teamPacket.write(Type.STRING, name);
		PacketUtil.sendPacket(teamPacket, Protocol1_7_6_10TO1_8.class, true, true);

		return name;
	}

	public String removeTeamForScore(String score) {
		ScoreTeam scoreTeam = scoreTeams.remove(score);
		if (scoreTeam==null) return score;

		PacketWrapper teamPacket = new PacketWrapper(0x3E, null, getUser());
		teamPacket.write(Type.STRING, scoreTeam.name);
		teamPacket.write(Type.BYTE, (byte) 1);
		PacketUtil.sendPacket(teamPacket, Protocol1_7_6_10TO1_8.class, true, true);

		return scoreTeam.name;
	}

	private class ScoreTeam {
		private String prefix;
		private String suffix;
		private String name;

		public ScoreTeam(String name, String prefix, String suffix) {
			this.prefix = prefix;
			this.suffix = suffix;
			this.name = name;
		}
	}
}
