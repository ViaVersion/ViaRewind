package com.viaversion.viarewind.api.data;

import com.viaversion.viabackwards.api.data.BackwardsMappingDataLoader;

public class VRMappingDataLoader extends BackwardsMappingDataLoader {

	public static final VRMappingDataLoader INSTANCE = new VRMappingDataLoader();

	public VRMappingDataLoader() {
		super(VRMappingDataLoader.class, "assets/viarewind/data/");
	}

}
