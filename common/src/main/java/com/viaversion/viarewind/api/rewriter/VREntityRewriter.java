package com.viaversion.viarewind.api.rewriter;

import com.viaversion.viabackwards.api.BackwardsProtocol;
import com.viaversion.viabackwards.api.rewriters.LegacyEntityRewriter;
import com.viaversion.viarewind.ViaRewind;
import com.viaversion.viaversion.api.minecraft.ClientWorld;
import com.viaversion.viaversion.api.minecraft.entities.EntityType;
import com.viaversion.viaversion.api.protocol.packet.ClientboundPacketType;
import com.viaversion.viaversion.api.type.Type;

public abstract class VREntityRewriter<C extends ClientboundPacketType, T extends BackwardsProtocol<C, ?, ?, ?>> extends LegacyEntityRewriter<C, T> {

	public VREntityRewriter(T protocol) {
		super(protocol);
	}

	protected void registerJoinGame1_8(final C packetType, final EntityType playerType) {
		protocol.registerClientbound(packetType, wrapper -> {
			final int entityId = wrapper.passthrough(Type.INT);
			wrapper.passthrough(Type.UNSIGNED_BYTE); // Game mode
			final byte dimension = wrapper.passthrough(Type.BYTE);

			addTrackedEntity(wrapper, entityId, playerType);
			wrapper.user().get(ClientWorld.class).setEnvironment(dimension);
		});
	}

	@Override
	protected boolean alwaysShowOriginalMobName() {
		return ViaRewind.getConfig().alwaysShowOriginalMobName();
	}
}
