package de.gerrygames.viarewind.protocol.protocol1_8to1_9.storage;

import com.viaversion.viaversion.api.connection.StoredObject;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.libs.gson.JsonPrimitive;
import com.viaversion.viaversion.protocols.protocol1_9to1_8.ClientboundPackets1_9;
import com.viaversion.viaversion.util.Pair;
import de.gerrygames.viarewind.ViaRewind;
import de.gerrygames.viarewind.api.ViaRewindConfig;
import de.gerrygames.viarewind.protocol.protocol1_8to1_9.Protocol1_8TO1_9;
import de.gerrygames.viarewind.utils.PacketUtil;
import de.gerrygames.viarewind.utils.Tickable;

import java.util.ArrayList;
import java.util.UUID;

public class Cooldown extends StoredObject implements Tickable {

	private double attackSpeed = 4.0;
	private long lastHit = 0;
	private final ViaRewindConfig.CooldownIndicator cooldownIndicator;
	private UUID bossUUID;
	private boolean lastSend;

	public Cooldown(final UserConnection user) {
		super(user);

		ViaRewindConfig.CooldownIndicator indicator;
		try {
			indicator = ViaRewind.getConfig().getCooldownIndicator();
		} catch (IllegalArgumentException e) {
			ViaRewind.getPlatform().getLogger().warning("Invalid cooldown-indicator setting");
			indicator = ViaRewindConfig.CooldownIndicator.DISABLED;
		}

		this.cooldownIndicator = indicator;
	}

	@Override
	public void tick() {
		if (!hasCooldown()) {
			if (lastSend) {
				hide();
				lastSend = false;
			}
			return;
		}

		BlockPlaceDestroyTracker tracker = getUser().get(BlockPlaceDestroyTracker.class);
		if (tracker.isMining()) {
			lastHit = 0;
			if (lastSend) {
				hide();
				lastSend = false;
			}
			return;
		}

		showCooldown();
		lastSend = true;
	}

	private void showCooldown() {
		if (cooldownIndicator == ViaRewindConfig.CooldownIndicator.TITLE) {
			sendTitle("", getTitle(), 0, 2, 5);
		} else if (cooldownIndicator == ViaRewindConfig.CooldownIndicator.ACTION_BAR) {
			sendActionBar(getTitle());
		} else if (cooldownIndicator == ViaRewindConfig.CooldownIndicator.BOSS_BAR) {
			sendBossBar((float) getCooldown());
		}
	}

	private void hide() {
		if (cooldownIndicator == ViaRewindConfig.CooldownIndicator.ACTION_BAR) {
			sendActionBar("§r");
		} else if (cooldownIndicator == ViaRewindConfig.CooldownIndicator.TITLE) {
			hideTitle();
		} else if (cooldownIndicator == ViaRewindConfig.CooldownIndicator.BOSS_BAR) {
			hideBossBar();
		}
	}

	private void hideBossBar() {
		if (bossUUID == null) return;
		PacketWrapper wrapper = PacketWrapper.create(0x0C, null, getUser());
		wrapper.write(Type.UUID, bossUUID);
		wrapper.write(Type.VAR_INT, 1);
		PacketUtil.sendPacket(wrapper, Protocol1_8TO1_9.class, false, true);
		bossUUID = null;
	}

	private void sendBossBar(float cooldown) {
		PacketWrapper wrapper = PacketWrapper.create(ClientboundPackets1_9.BOSSBAR, getUser());
		if (bossUUID == null) {
			bossUUID = UUID.randomUUID();
			wrapper.write(Type.UUID, bossUUID);
			wrapper.write(Type.VAR_INT, 0);
			wrapper.write(Type.COMPONENT, new JsonPrimitive(" "));
			wrapper.write(Type.FLOAT, cooldown);
			wrapper.write(Type.VAR_INT, 0);
			wrapper.write(Type.VAR_INT, 0);
			wrapper.write(Type.UNSIGNED_BYTE, (short) 0);
		} else {
			wrapper.write(Type.UUID, bossUUID);
			wrapper.write(Type.VAR_INT, 2);
			wrapper.write(Type.FLOAT, cooldown);
		}
		PacketUtil.sendPacket(wrapper, Protocol1_8TO1_9.class, false, true);
	}

	private void hideTitle() {
		PacketWrapper hide = PacketWrapper.create(0x45, null, getUser());
		hide.write(Type.VAR_INT, 3);
		PacketUtil.sendPacket(hide, Protocol1_8TO1_9.class);
	}

	private void sendTitle(String title, String subTitle, int fadeIn, int stay, int fadeOut) {
		PacketWrapper timePacket = PacketWrapper.create(0x45, null, getUser());
		timePacket.write(Type.VAR_INT, 2);
		timePacket.write(Type.INT, fadeIn);
		timePacket.write(Type.INT, stay);
		timePacket.write(Type.INT, fadeOut);
		PacketWrapper titlePacket = PacketWrapper.create(0x45, null, getUser());
		titlePacket.write(Type.VAR_INT, 0);
		titlePacket.write(Type.STRING, title);
		PacketWrapper subtitlePacket = PacketWrapper.create(0x45, null, getUser());
		subtitlePacket.write(Type.VAR_INT, 1);
		subtitlePacket.write(Type.STRING, subTitle);

		PacketUtil.sendPacket(titlePacket, Protocol1_8TO1_9.class);
		PacketUtil.sendPacket(subtitlePacket, Protocol1_8TO1_9.class);
		PacketUtil.sendPacket(timePacket, Protocol1_8TO1_9.class);
	}

	private void sendActionBar(String bar) {
		PacketWrapper actionBarPacket = PacketWrapper.create(0x02, null, getUser());
		actionBarPacket.write(Type.STRING, bar);
		actionBarPacket.write(Type.BYTE, (byte) 2);

		PacketUtil.sendPacket(actionBarPacket, Protocol1_8TO1_9.class);
	}

	public boolean hasCooldown() {
		long time = System.currentTimeMillis() - lastHit;
		double cooldown = restrain(((double) time) * attackSpeed / 1000d, 0, 1.5);
		return cooldown > 0.1 && cooldown < 1.1;
	}

	public double getCooldown() {
		long time = System.currentTimeMillis() - lastHit;
		return restrain(((double) time) * attackSpeed / 1000d, 0, 1);
	}

	private double restrain(double x, double a, double b) {
		if (x < a) return a;
		return Math.min(x, b);
	}

	private static final int max = 10;

	private String getTitle() {
		String symbol = cooldownIndicator == ViaRewindConfig.CooldownIndicator.ACTION_BAR ? "■" : "˙";

		double cooldown = getCooldown();
		int green = (int) Math.floor(((double) max) * cooldown);
		int grey = max - green;
		StringBuilder builder = new StringBuilder("§8");
		while (green-- > 0) builder.append(symbol);
		builder.append("§7");
		while (grey-- > 0) builder.append(symbol);
		return builder.toString();
	}

	public double getAttackSpeed() {
		return attackSpeed;
	}

	public void setAttackSpeed(double attackSpeed) {
		this.attackSpeed = attackSpeed;
	}

	public void setAttackSpeed(double base, ArrayList<Pair<Byte, Double>> modifiers) {
		attackSpeed = base;
		for (int j = 0; j < modifiers.size(); j++) {
			if (modifiers.get(j).getKey() == 0) {
				attackSpeed += modifiers.get(j).getValue();
				modifiers.remove(j--);
			}
		}
		for (int j = 0; j < modifiers.size(); j++) {
			if (modifiers.get(j).getKey() == 1) {
				attackSpeed += base * modifiers.get(j).getValue();
				modifiers.remove(j--);
			}
		}
		for (int j = 0; j < modifiers.size(); j++) {
			if (modifiers.get(j).getKey() == 2) {
				attackSpeed *= (1.0 + modifiers.get(j).getValue());
				modifiers.remove(j--);
			}
		}
	}

	public void hit() {
		lastHit = System.currentTimeMillis();
	}

	public void setLastHit(long lastHit) {
		this.lastHit = lastHit;
	}
}
