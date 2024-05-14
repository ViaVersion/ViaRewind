package com.viaversion.viarewind.api.type.version;

import com.viaversion.viarewind.api.type.entitydata.EntityDataListType;
import com.viaversion.viarewind.api.type.entitydata.EntityDataType;
import com.viaversion.viaversion.api.minecraft.entitydata.EntityData;
import com.viaversion.viaversion.api.type.Type;

import java.util.List;

public class Types1_7_6_10 {

	public static final Type<EntityData> ENTITY_DATA = new EntityDataType();
	public static final Type<List<EntityData>> ENTITY_DATA_LIST = new EntityDataListType(ENTITY_DATA);

}
