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
package com.viaversion.viarewind.protocol.v1_8to1_7_6_10.rewriter;

import com.viaversion.viarewind.protocol.v1_7_6_10to1_7_2_5.packet.ClientboundPackets1_7_2_5;
import com.viaversion.viarewind.protocol.v1_8to1_7_6_10.Protocol1_8To1_7_6_10;
import com.viaversion.viarewind.protocol.v1_8to1_7_6_10.storage.ScoreboardTracker;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.protocol.remapper.PacketHandlers;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.protocols.v1_8to1_9.packet.ClientboundPackets1_8;
import com.viaversion.viaversion.util.ChatColorUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ScoreboardPacketRewriter1_8 {

	public static void register(Protocol1_8To1_7_6_10 protocol) {
		protocol.registerClientbound(ClientboundPackets1_8.SET_OBJECTIVE, wrapper -> {
			String name = wrapper.passthrough(Types.STRING);
			if (name.length() > 16) {
				wrapper.set(Types.STRING, 0, name = name.substring(0, 16));
			}

			final byte mode = wrapper.read(Types.BYTE);
			ScoreboardTracker scoreboard = wrapper.user().get(ScoreboardTracker.class);
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
							final PacketWrapper sidebarPacket = PacketWrapper.create(ClientboundPackets1_7_2_5.SET_DISPLAY_OBJECTIVE, wrapper.user());
							sidebarPacket.write(Types.BYTE, (byte) 1);
							sidebarPacket.write(Types.STRING, scoreboard.getColorIndependentSidebar());
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
				String displayName = wrapper.passthrough(Types.STRING);
				if (displayName.length() > 32) {
					wrapper.set(Types.STRING, 1, displayName.substring(0, 32));
				}
				wrapper.read(Types.STRING);
			} else {
				wrapper.write(Types.STRING, "");
			}
			wrapper.write(Types.BYTE, mode);
		});

		protocol.registerClientbound(ClientboundPackets1_8.SET_SCORE, new PacketHandlers() {
			@Override
			public void register() {
				map(Types.STRING); // Name
				map(Types.VAR_INT, Types.BYTE); // Mode
				handler(wrapper -> {
					ScoreboardTracker scoreboard = wrapper.user().get(ScoreboardTracker.class);
					String name = wrapper.get(Types.STRING, 0);
					byte mode = wrapper.get(Types.BYTE, 0);

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
					wrapper.set(Types.STRING, 0, name);

					String objective = wrapper.read(Types.STRING);
					if (objective.length() > 16) {
						objective = objective.substring(0, 16);
					}

					if (mode != 1) {
						int score = wrapper.read(Types.VAR_INT);
						wrapper.write(Types.STRING, objective);
						wrapper.write(Types.INT, score);
					}
				});
			}
		});

		protocol.registerClientbound(ClientboundPackets1_8.SET_DISPLAY_OBJECTIVE, new PacketHandlers() {
			@Override
			public void register() {
				map(Types.BYTE); // Position
				map(Types.STRING); // Score name
				handler(wrapper -> {
					byte position = wrapper.get(Types.BYTE, 0);
					String name = wrapper.get(Types.STRING, 0);
					ScoreboardTracker scoreboard = wrapper.user().get(ScoreboardTracker.class);
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
					wrapper.set(Types.BYTE, 0, position);
				});
			}
		});

		protocol.registerClientbound(ClientboundPackets1_8.SET_PLAYER_TEAM, wrapper -> {
			final String team = wrapper.passthrough(Types.STRING);
			if (team == null) {
				wrapper.cancel();
				return;
			}

			final ScoreboardTracker scoreboard = wrapper.user().get(ScoreboardTracker.class);

			final byte mode = wrapper.passthrough(Types.BYTE);
			if (mode != 0 && !scoreboard.teamExists(team)) {
				wrapper.cancel();
				return;
			} else if (mode == 0 && scoreboard.teamExists(team)) {
				scoreboard.removeTeam(team);

				final PacketWrapper removeTeam = PacketWrapper.create(ClientboundPackets1_7_2_5.SET_PLAYER_TEAM, wrapper.user());
				removeTeam.write(Types.STRING, team);
				removeTeam.write(Types.BYTE, (byte) 1);
				removeTeam.send(Protocol1_8To1_7_6_10.class);
			}

			if (mode == 0) {
				scoreboard.addTeam(team);
			} else if (mode == 1) {
				scoreboard.removeTeam(team);
			}

			if (mode == 0 || mode == 2) {
				wrapper.passthrough(Types.STRING); // Display name
				wrapper.passthrough(Types.STRING); // Prefix
				wrapper.passthrough(Types.STRING); // Suffix
				wrapper.passthrough(Types.BYTE); // Friendly fire
				wrapper.read(Types.STRING); // Name tag visibility
				byte color = wrapper.read(Types.BYTE);
				if (mode == 2 && scoreboard.getTeamColor(team).get() != color) {
					final String sidebar = scoreboard.getColorDependentSidebar().get(color);

					final PacketWrapper sidebarPacket = PacketWrapper.create(ClientboundPackets1_7_2_5.SET_DISPLAY_OBJECTIVE, wrapper.user());
					sidebarPacket.write(Types.BYTE, (byte) 1);
					sidebarPacket.write(Types.STRING, sidebar == null ? "" : sidebar);
					sidebarPacket.scheduleSend(Protocol1_8To1_7_6_10.class);
				}
				scoreboard.setTeamColor(team, color);
			}
			if (mode == 0 || mode == 3 || mode == 4) {
				byte color = scoreboard.getTeamColor(team).get();
				String[] entries = wrapper.read(Types.STRING_ARRAY);
				List<String> entryList = new ArrayList<>();

				for (String entry : entries) {
					String username = wrapper.user().getProtocolInfo().getUsername();

					if (mode == 4) {
						if (!scoreboard.isPlayerInTeam(entry, team)) {
							continue;
						}
						scoreboard.removePlayerFromTeam(entry, team);
						if (entry.equals(username)) {
							final PacketWrapper sidebarPacket = PacketWrapper.create(ClientboundPackets1_7_2_5.SET_DISPLAY_OBJECTIVE, wrapper.user());
							sidebarPacket.write(Types.BYTE, (byte) 1);
							sidebarPacket.write(Types.STRING, scoreboard.getColorIndependentSidebar() == null ? "" : scoreboard.getColorIndependentSidebar());
							sidebarPacket.scheduleSend(Protocol1_8To1_7_6_10.class);
						}
					} else {
						scoreboard.addPlayerToTeam(entry, team);
						if (entry.equals(username) && scoreboard.getColorDependentSidebar().containsKey(color)) {
							final PacketWrapper displayObjective = PacketWrapper.create(ClientboundPackets1_7_2_5.SET_DISPLAY_OBJECTIVE, wrapper.user());
							displayObjective.write(Types.BYTE, (byte) 1);
							displayObjective.write(Types.STRING, scoreboard.getColorDependentSidebar().get(color));
							displayObjective.scheduleSend(Protocol1_8To1_7_6_10.class);
						}
					}
					entryList.add(entry);
				}

				wrapper.write(Types.SHORT, (short) entryList.size());
				for (String entry : entryList) {
					wrapper.write(Types.STRING, entry);
				}
			}
		});
	}
}
