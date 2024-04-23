package com.viaversion.viarewind.api.data;

import com.viaversion.viabackwards.api.data.BackwardsMappings;
import com.viaversion.viarewind.ViaRewind;
import com.viaversion.viaversion.libs.opennbt.tag.builtin.CompoundTag;

import java.util.logging.Logger;

public class RewindMappings extends BackwardsMappings {

	public RewindMappings(String unmappedVersion, String mappedVersion) {
		super(unmappedVersion, mappedVersion);
	}

	@Override
	protected Logger getLogger() {
		return ViaRewind.getPlatform().getLogger();
	}

	@Override
	protected CompoundTag readMappingsFile(String name) {
		return VRMappingDataLoader.INSTANCE.loadNBTFromDir(name);
	}
}
