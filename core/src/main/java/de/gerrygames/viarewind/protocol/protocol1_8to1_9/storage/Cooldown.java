package de.gerrygames.viarewind.protocol.protocol1_8to1_9.storage;

import de.gerrygames.viarewind.ViaRewind;
import de.gerrygames.viarewind.api.ViaRewindConfig;
import de.gerrygames.viarewind.protocol.protocol1_8to1_9.Protocol1_8TO1_9;
import de.gerrygames.viarewind.utils.PacketUtil;
import us.myles.ViaVersion.api.PacketWrapper;
import us.myles.ViaVersion.api.Pair;
import us.myles.ViaVersion.api.Via;
import us.myles.ViaVersion.api.data.StoredObject;
import us.myles.ViaVersion.api.data.UserConnection;
import us.myles.ViaVersion.api.platform.TaskId;
import us.myles.ViaVersion.api.type.Type;

import java.util.ArrayList;

public class Cooldown extends StoredObject {
	private double attackSpeed = 4.0;
	private long lastHit = 0;
	private TaskId taskId;
	private final ViaRewindConfig.CooldownIndicator cooldownIndicator;

	public Cooldown(final UserConnection user) {
		super(user);

		this.cooldownIndicator = ViaRewind.getConfig().getCooldownIndicator();

		if (cooldownIndicator==ViaRewindConfig.CooldownIndicator.DISABLED) return;

		taskId = Via.getPlatform().runRepeatingSync(new Runnable() {
			private boolean lastSend = false;
			@Override
			public void run() {
				if (!user.getChannel().isOpen()) {
					Via.getPlatform().cancelTask(taskId);
					return;
				}

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
					hide();
					lastSend = false;
					return;
				}

				String title = getTitle();
				if (cooldownIndicator==ViaRewindConfig.CooldownIndicator.TITLE) {
					sendTitle("", title, 0, 2, 5);
				} else if (cooldownIndicator==ViaRewindConfig.CooldownIndicator.ACTION_BAR) {
					sendActionBar(title);
				}
				lastSend = true;
			}
		}, 1L);
	}

	private void hide() {
		if (cooldownIndicator==ViaRewindConfig.CooldownIndicator.ACTION_BAR) {
			sendActionBar("§r");
		} if (cooldownIndicator==ViaRewindConfig.CooldownIndicator.TITLE) {
			hideTitle();
		}
	}

	private void hideTitle() {
		PacketWrapper hide = new PacketWrapper(0x45, null, getUser());
		hide.write(Type.VAR_INT, 3);
		PacketUtil.sendPacket(hide, Protocol1_8TO1_9.class);
	}

	private void sendTitle(String title, String subTitle, int fadeIn, int stay, int fadeOut) {
		PacketWrapper timePacket = new PacketWrapper(0x45, null, getUser());
		timePacket.write(Type.VAR_INT, 2);
		timePacket.write(Type.INT, fadeIn);
		timePacket.write(Type.INT, stay);
		timePacket.write(Type.INT, fadeOut);
		PacketWrapper titlePacket = new PacketWrapper(0x45, null, getUser());
		titlePacket.write(Type.VAR_INT, 0);
		titlePacket.write(Type.STRING, title);
		PacketWrapper subtitlePacket = new PacketWrapper(0x45, null, getUser());
		subtitlePacket.write(Type.VAR_INT, 1);
		subtitlePacket.write(Type.STRING, subTitle);

		PacketUtil.sendPacket(titlePacket, Protocol1_8TO1_9.class);
		PacketUtil.sendPacket(subtitlePacket, Protocol1_8TO1_9.class);
		PacketUtil.sendPacket(timePacket, Protocol1_8TO1_9.class);
	}

	private void sendActionBar(String bar) {
		PacketWrapper actionBarPacket = new PacketWrapper(0x02, null, getUser());
		actionBarPacket.write(Type.STRING, bar);
		actionBarPacket.write(Type.BYTE, (byte) 2);

		PacketUtil.sendPacket(actionBarPacket, Protocol1_8TO1_9.class);
	}

	public boolean hasCooldown() {
		long time = System.currentTimeMillis()-lastHit;
		double cooldown = restrain(((double)time) * attackSpeed / 1000d, 0, 1.5);
		return cooldown>0.1 && cooldown<1.2;
	}

	public double getCooldown() {
		long time = System.currentTimeMillis()-lastHit;
		return restrain(((double)time) * attackSpeed / 1000d, 0, 1);
	}

	private double restrain(double x, double a, double b) {
		if (x<a) return a;
		if (x>b) return b;
		return x;
	}

	private static final int max = 10;
	private String getTitle() {
		String symbol = cooldownIndicator==ViaRewindConfig.CooldownIndicator.ACTION_BAR ? "■" : "˙";

		double cooldown = getCooldown();
		int green = (int) Math.floor(((double)max) * cooldown);
		int grey = max-green;
		StringBuilder builder = new StringBuilder("§8");
		while(green-->0) builder.append(symbol);
		builder.append("§7");
		while(grey-->0) builder.append(symbol);
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
		for (int j = 0; j<modifiers.size(); j++) {
			if (modifiers.get(j).getKey()==0) {
				attackSpeed += modifiers.get(j).getValue();
				modifiers.remove(j--);
			}
		}
		for (int j = 0; j<modifiers.size(); j++) {
			if (modifiers.get(j).getKey()==1) {
				attackSpeed += base * modifiers.get(j).getValue();
				modifiers.remove(j--);
			}
		}
		for (int j = 0; j<modifiers.size(); j++) {
			if (modifiers.get(j).getKey()==2) {
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
