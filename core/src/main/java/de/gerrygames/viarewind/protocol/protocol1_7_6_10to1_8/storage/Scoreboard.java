package de.gerrygames.viarewind.protocol.protocol1_7_6_10to1_8.storage;

import us.myles.ViaVersion.api.data.StoredObject;
import us.myles.ViaVersion.api.data.UserConnection;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Scoreboard extends StoredObject {
	private HashMap<String, List<String>> teams = new HashMap<>();

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
}
