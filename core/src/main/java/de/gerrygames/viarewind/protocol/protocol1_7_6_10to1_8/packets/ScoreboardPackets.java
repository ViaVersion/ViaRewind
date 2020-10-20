package de.gerrygames.viarewind.protocol.protocol1_7_6_10to1_8.packets;

import de.gerrygames.viarewind.protocol.protocol1_7_6_10to1_8.Protocol1_7_6_10TO1_8;
import de.gerrygames.viarewind.protocol.protocol1_7_6_10to1_8.storage.Scoreboard;
import de.gerrygames.viarewind.utils.PacketUtil;
import net.md_5.bungee.api.ChatColor;
import us.myles.ViaVersion.api.PacketWrapper;
import us.myles.ViaVersion.api.protocol.Protocol;
import us.myles.ViaVersion.api.remapper.PacketRemapper;
import us.myles.ViaVersion.api.type.Type;
import us.myles.ViaVersion.packets.State;
import us.myles.ViaVersion.protocols.base.ProtocolInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ScoreboardPackets {

	public static void register(Protocol protocol) {

		/*  OUTGOING  */

		//Scoreboard Objective
		protocol.registerOutgoing(State.PLAY, 0x3B, 0x3B, new PacketRemapper() {
			@Override
			public void registerMap() {
				handler(wrapper -> {
					String name = wrapper.passthrough(Type.STRING);
					if (name.length() > 16) {
						wrapper.set(Type.STRING, 0, name = name.substring(0, 16));
					}
					byte mode = wrapper.read(Type.BYTE);

					Scoreboard scoreboard = wrapper.user().get(Scoreboard.class);
					if (mode == 0) {
						if (scoreboard.objectiveExists(name)) {
							wrapper.cancel();
							return;
						}
						scoreboard.addObjective(name);
					} else if (mode == 1) {
						if (!scoreboard.objectiveExists(name)) {
							wrapper.cancel();
							return;
						}
						if (scoreboard.getColorIndependentSidebar() != null) {
							String username = wrapper.user().get(ProtocolInfo.class).getUsername();
							Optional<Byte> color = scoreboard.getPlayerTeamColor(username);
							if (color.isPresent()) {
								String sidebar = scoreboard.getColorDependentSidebar().get(color.get());
								if (name.equals(sidebar)) {
									PacketWrapper sidebarPacket = new PacketWrapper(0x3D, null, wrapper.user());
									sidebarPacket.write(Type.BYTE, (byte) 1);
									sidebarPacket.write(Type.STRING, scoreboard.getColorIndependentSidebar());
									PacketUtil.sendPacket(sidebarPacket, Protocol1_7_6_10TO1_8.class);
								}
							}
						}
						scoreboard.removeObjective(name);
					} else if (mode == 2) {
						if (!scoreboard.objectiveExists(name)) {
							wrapper.cancel();
							return;
						}
					}

					if (mode == 0 || mode == 2) {
						String displayName = wrapper.passthrough(Type.STRING);
						if (displayName.length() > 32) {
							wrapper.set(Type.STRING, 1, displayName.substring(0, 32));
						}
						wrapper.read(Type.STRING);
					} else {
						wrapper.write(Type.STRING, "");
					}
					wrapper.write(Type.BYTE, mode);
				});
			}
		});

		//Update Score
		protocol.registerOutgoing(State.PLAY, 0x3C, 0x3C, new PacketRemapper() {
			@Override
			public void registerMap() {
				handler(wrapper -> {
					Scoreboard scoreboard = wrapper.user().get(Scoreboard.class);
					String name = wrapper.passthrough(Type.STRING);
					byte mode = wrapper.passthrough(Type.BYTE);

					if (mode == 1) {
						name = scoreboard.removeTeamForScore(name);
					} else {
						name = scoreboard.sendTeamForScore(name);
					}

					if (name.length() > 16) {
						name = ChatColor.stripColor(name);
						if (name.length() > 16) {
							name = name.substring(0, 16);
						}
					}
					wrapper.set(Type.STRING, 0, name);

					String objective = wrapper.read(Type.STRING);
					if (objective.length() > 16) {
						objective = objective.substring(0, 16);
					}

					if (mode != 1) {
						int score = wrapper.read(Type.VAR_INT);
						wrapper.write(Type.STRING, objective);
						wrapper.write(Type.INT, score);
					}
				});
			}
		});

		//Display Scoreboard
		protocol.registerOutgoing(State.PLAY, 0x3D, 0x3D, new PacketRemapper() {
			@Override
			public void registerMap() {
				map(Type.BYTE); // Position
				map(Type.STRING); // Score name

				handler(wrapper -> {
					byte position = wrapper.get(Type.BYTE, 0);
					String name = wrapper.get(Type.STRING, 0);
					Scoreboard scoreboard = wrapper.user().get(Scoreboard.class);
					if (position > 2) { // team specific sidebar
						byte receiverTeamColor = (byte) (position - 3);
						scoreboard.getColorDependentSidebar().put(receiverTeamColor, name);

						String username = wrapper.user().get(ProtocolInfo.class).getUsername();
						Optional<Byte> color = scoreboard.getPlayerTeamColor(username);
						if (color.isPresent() && color.get() == receiverTeamColor) {
							position = 1;
						} else {
							position = -1;
						}
					} else if (position == 1) { // team independent sidebar
						scoreboard.setColorIndependentSidebar(name);
						String username = wrapper.user().get(ProtocolInfo.class).getUsername();
						Optional<Byte> color = scoreboard.getPlayerTeamColor(username);
						if (color.isPresent() && scoreboard.getColorDependentSidebar().containsKey(color.get())) {
							position = -1;
						}
					}
					if (position == -1) {
						wrapper.cancel();
						return;
					}
					wrapper.set(Type.BYTE, 0, position);
				});
			}
		});

		//Scoreboard Teams
		protocol.registerOutgoing(State.PLAY, 0x3E, 0x3E, new PacketRemapper() {
			@Override
			public void registerMap() {
				map(Type.STRING);

				handler(wrapper -> {
					String team = wrapper.get(Type.STRING, 0);
					if (team == null) {
						wrapper.cancel();
						return;
					}
					byte mode = wrapper.passthrough(Type.BYTE);

					Scoreboard scoreboard = wrapper.user().get(Scoreboard.class);

					if (mode != 0 && !scoreboard.teamExists(team)) {
						wrapper.cancel();
						return;
					} else if (mode == 0 && scoreboard.teamExists(team)) {
						scoreboard.removeTeam(team);

						PacketWrapper remove = new PacketWrapper(0x3E, null, wrapper.user());
						remove.write(Type.STRING, team);
						remove.write(Type.BYTE, (byte) 1);
						PacketUtil.sendPacket(remove, Protocol1_7_6_10TO1_8.class, true, true);
					}

					if (mode == 0) {
						scoreboard.addTeam(team);
					} else if (mode == 1) {
						scoreboard.removeTeam(team);
					}

					if (mode == 0 || mode == 2) {
						wrapper.passthrough(Type.STRING); // Display name
						wrapper.passthrough(Type.STRING); // prefix
						wrapper.passthrough(Type.STRING); // suffix
						wrapper.passthrough(Type.BYTE); // friendly fire
						wrapper.read(Type.STRING); // name tag visibility
						byte color = wrapper.read(Type.BYTE);
						if (mode == 2 && scoreboard.getTeamColor(team).get() != color) {
							String username = wrapper.user().get(ProtocolInfo.class).getUsername();
							String sidebar = scoreboard.getColorDependentSidebar().get(color);
							PacketWrapper sidebarPacket = wrapper.create(0x3D);
							sidebarPacket.write(Type.BYTE, (byte) 1);
							sidebarPacket.write(Type.STRING, sidebar == null ? "" : sidebar);
							PacketUtil.sendPacket(sidebarPacket, Protocol1_7_6_10TO1_8.class);
						}
						scoreboard.setTeamColor(team, color);
					}
					if (mode == 0 || mode == 3 || mode == 4) {
						byte color = scoreboard.getTeamColor(team).get();
						String[] entries = wrapper.read(Type.STRING_ARRAY);
						List<String> entryList = new ArrayList<>();

						for (String entry : entries) {
							String username = wrapper.user().get(ProtocolInfo.class).getUsername();

							if (mode == 4) {
								if (!scoreboard.isPlayerInTeam(entry, team)) continue;
								scoreboard.removePlayerFromTeam(entry, team);
								if (entry.equals(username)) {
									PacketWrapper sidebarPacket = wrapper.create(0x3D);
									sidebarPacket.write(Type.BYTE, (byte) 1);
									sidebarPacket.write(Type.STRING, scoreboard.getColorIndependentSidebar() == null ? "" : scoreboard.getColorIndependentSidebar());
									PacketUtil.sendPacket(sidebarPacket, Protocol1_7_6_10TO1_8.class);
								}
							} else {
								scoreboard.addPlayerToTeam(entry, team);
								if (entry.equals(username) && scoreboard.getColorDependentSidebar().containsKey(color)) {
									PacketWrapper displayObjective = wrapper.create(0x3D);
									displayObjective.write(Type.BYTE, (byte) 1);
									displayObjective.write(Type.STRING, scoreboard.getColorDependentSidebar().get(color));
									PacketUtil.sendPacket(displayObjective, Protocol1_7_6_10TO1_8.class);
								}
							}
							entryList.add(entry);
						}

						wrapper.write(Type.SHORT, (short) entryList.size());
						for (String entry : entryList) {
							wrapper.write(Type.STRING, entry);
						}
					}
				});
			}
		});
	}
}
