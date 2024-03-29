package com.viaversion.viarewind.api.rewriter;

import com.viaversion.viabackwards.api.BackwardsProtocol;
import com.viaversion.viabackwards.api.rewriters.LegacyEntityRewriter;
import com.viaversion.viarewind.ViaRewind;
import com.viaversion.viaversion.api.protocol.packet.ClientboundPacketType;

public abstract class VREntityRewriter<C extends ClientboundPacketType, T extends BackwardsProtocol<C, ?, ?, ?>> extends LegacyEntityRewriter<C, T> {

	public VREntityRewriter(T protocol) {
		super(protocol);
	}

	@Override
	protected boolean alwaysShowOriginalMobName() {
		return ViaRewind.getConfig().alwaysShowOriginalMobName();
	}
}
