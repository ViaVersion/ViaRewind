package de.gerrygames.viarewind.protocol.protocol1_7_6_10to1_8.storage;

import de.gerrygames.viarewind.protocol.protocol1_7_6_10to1_8.Protocol1_7_6_10TO1_8;
import de.gerrygames.viarewind.utils.PacketUtil;
import lombok.Getter;
import lombok.Setter;
import us.myles.ViaVersion.api.PacketWrapper;
import us.myles.ViaVersion.api.data.StoredObject;
import us.myles.ViaVersion.api.data.UserConnection;
import us.myles.ViaVersion.api.type.Type;

import java.util.*;

public class Scoreboard extends StoredObject {
	private HashMap<String, List<String>> teams = new HashMap<>();
	private HashSet<String> objectives = new HashSet<>();
	private HashMap<String, ScoreTeam> scoreTeams = new HashMap<>();
	private HashMap<String, Byte> teamColors = new HashMap<>();
	private HashSet<String> scoreTeamNames = new HashSet<>();
	@Getter
    @Setter
	private String colorIndependentSidebar;
	@Getter
	private HashMap<Byte, String> colorDependentSidebar = new HashMap<>();

    public Scoreboard(UserConnection user) {
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
		if (score.length()<=16) return score;
		if (scoreTeams.containsKey(score)) return scoreTeams.get(score).name;
		int l = 16;
		int i = Math.min(16, score.length()-16);
		String name = score.substring(i, i+l);
		while (scoreTeamNames.contains(name) || teams.containsKey(name)) {
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
		scoreTeamNames.add(name);

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
		scoreTeamNames.remove(scoreTeam.name);

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
