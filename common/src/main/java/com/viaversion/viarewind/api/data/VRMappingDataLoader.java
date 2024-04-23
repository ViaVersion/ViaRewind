package com.viaversion.viarewind.api.data;

import com.viaversion.viabackwards.api.data.BackwardsMappingDataLoader;
import com.viaversion.viarewind.ViaRewind;

import java.io.File;
import java.util.logging.Logger;

public class VRMappingDataLoader extends BackwardsMappingDataLoader {

	public static final VRMappingDataLoader INSTANCE = new VRMappingDataLoader();

	public VRMappingDataLoader() {
		super(VRMappingDataLoader.class, "assets/viarewind/data/");
	}


	@Override
	public Logger getLogger() {
		return ViaRewind.getPlatform().getLogger();
	}

	@Override
	public File getDataFolder() {
		return ViaRewind.getPlatform().getDataFolder();
	}
}
