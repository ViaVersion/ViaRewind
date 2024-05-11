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
package com.viaversion.viarewind.protocol.v1_8to1_7_6_10.packets;

import com.viaversion.viarewind.protocol.v1_8to1_7_6_10.Protocol1_8To1_7_6_10;
import com.viaversion.viarewind.protocol.v1_8to1_7_6_10.storage.Scoreboard;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.protocol.remapper.PacketHandlers;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.protocols.protocol1_8.ClientboundPackets1_8;
import com.viaversion.viaversion.util.ChatColorUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ScoreboardPackets1_8 {

	public static void register(Protocol1_8To1_7_6_10 protocol) {
		protocol.registerClientbound(ClientboundPackets1_8.SCOREBOARD_OBJECTIVE, new PacketHandlers() {
			@Override
			public void register() {
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
							String username = wrapper.user().getProtocolInfo().getUsername();
							Optional<Byte> color = scoreboard.getPlayerTeamColor(username);
							if (color.isPresent()) {
								String sidebar = scoreboard.getColorDependentSidebar().get(color.get());
								if (name.equals(sidebar)) {
									PacketWrapper sidebarPacket = PacketWrapper.create(0x3D, null, wrapper.user());
									sidebarPacket.write(Type.BYTE, (byte) 1);
									sidebarPacket.write(Type.STRING, scoreboard.getColorIndependentSidebar());
									sidebarPacket.scheduleSend(Protocol1_8To1_7_6_10.class);
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

		protocol.registerClientbound(ClientboundPackets1_8.UPDATE_SCORE, new PacketHandlers() {
			@Override
			public void register() {
				map(Type.STRING); // Name
				map(Type.VAR_INT, Type.BYTE); // Mode
				handler(wrapper -> {
					Scoreboard scoreboard = wrapper.user().get(Scoreboard.class);
					String name = wrapper.get(Type.STRING, 0);
					byte mode = wrapper.get(Type.BYTE, 0);

					if (mode == 1) {
						name = scoreboard.removeTeamForScore(name);
					} else {
						name = scoreboard.sendTeamForScore(name);
					}

					if (name.length() > 16) {
						name = ChatColorUtil.stripColor(name);
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

		protocol.registerClientbound(ClientboundPackets1_8.DISPLAY_SCOREBOARD, new PacketHandlers() {
			@Override
			public void register() {
				map(Type.BYTE); // Position
				map(Type.STRING); // Score name
				handler(wrapper -> {
					byte position = wrapper.get(Type.BYTE, 0);
					String name = wrapper.get(Type.STRING, 0);
					Scoreboard scoreboard = wrapper.user().get(Scoreboard.class);
					if (position > 2) { // team specific sidebar
						byte receiverTeamColor = (byte) (position - 3);
						scoreboard.getColorDependentSidebar().put(receiverTeamColor, name);

						String username = wrapper.user().getProtocolInfo().getUsername();
						Optional<Byte> color = scoreboard.getPlayerTeamColor(username);
						if (color.isPresent() && color.get() == receiverTeamColor) {
							position = 1;
						} else {
							position = -1;
						}
					} else if (position == 1) { // team independent sidebar
						scoreboard.setColorIndependentSidebar(name);
						String username = wrapper.user().getProtocolInfo().getUsername();
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

		protocol.registerClientbound(ClientboundPackets1_8.TEAMS, new PacketHandlers() {
			@Override
			public void register() {
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

						PacketWrapper remove = PacketWrapper.create(0x3E, null, wrapper.user());
						remove.write(Type.STRING, team);
						remove.write(Type.BYTE, (byte) 1);
						remove.send(Protocol1_8To1_7_6_10.class);
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
							String sidebar = scoreboard.getColorDependentSidebar().get(color);
							PacketWrapper sidebarPacket = wrapper.create(0x3D);
							sidebarPacket.write(Type.BYTE, (byte) 1);
							sidebarPacket.write(Type.STRING, sidebar == null ? "" : sidebar);
							sidebarPacket.scheduleSend(Protocol1_8To1_7_6_10.class);
						}
						scoreboard.setTeamColor(team, color);
					}
					if (mode == 0 || mode == 3 || mode == 4) {
						byte color = scoreboard.getTeamColor(team).get();
						String[] entries = wrapper.read(Type.STRING_ARRAY);
						List<String> entryList = new ArrayList<>();

                        for (String entry : entries) {
                            String username = wrapper.user().getProtocolInfo().getUsername();

                            if (mode == 4) {
                                if (!scoreboard.isPlayerInTeam(entry, team)) continue;
                                scoreboard.removePlayerFromTeam(entry, team);
                                if (entry.equals(username)) {
                                    PacketWrapper sidebarPacket = wrapper.create(0x3D);
                                    sidebarPacket.write(Type.BYTE, (byte) 1);
                                    sidebarPacket.write(Type.STRING, scoreboard.getColorIndependentSidebar() == null ? "" : scoreboard.getColorIndependentSidebar());
									sidebarPacket.scheduleSend(Protocol1_8To1_7_6_10.class);
                                }
                            } else {
                                scoreboard.addPlayerToTeam(entry, team);
                                if (entry.equals(username) && scoreboard.getColorDependentSidebar().containsKey(color)) {
                                    PacketWrapper displayObjective = wrapper.create(0x3D);
                                    displayObjective.write(Type.BYTE, (byte) 1);
                                    displayObjective.write(Type.STRING, scoreboard.getColorDependentSidebar().get(color));
									displayObjective.scheduleSend(Protocol1_8To1_7_6_10.class);
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
