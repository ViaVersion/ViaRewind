package de.gerrygames.viarewind.protocol.protocol1_7_6_10to1_8.metadata;

import de.gerrygames.viarewind.protocol.protocol1_8to1_7_6_10.metadata.MetaIndex1_8to1_7_6_10;
import us.myles.ViaVersion.api.Pair;
import us.myles.ViaVersion.api.entities.Entity1_10Types;

import java.util.HashMap;
import java.util.Optional;

public class MetaIndex1_7_6_10to1_8 {

	private static final HashMap<Pair<Entity1_10Types.EntityType, Integer>, MetaIndex1_8to1_7_6_10> metadataRewrites = new HashMap<>();

	static {
		for (MetaIndex1_8to1_7_6_10 index : MetaIndex1_8to1_7_6_10.values())
			metadataRewrites.put(new Pair<>(index.getClazz(), index.getNewIndex()), index);
	}

	private static Optional<MetaIndex1_8to1_7_6_10> getIndex(Entity1_10Types.EntityType type, int index) {
		Pair pair = new Pair<>(type, index);
		if (metadataRewrites.containsKey(pair)) {
			return Optional.of(metadataRewrites.get(pair));
		}

		return Optional.empty();
	}

	public static MetaIndex1_8to1_7_6_10 searchIndex(Entity1_10Types.EntityType type, int index) {
		Entity1_10Types.EntityType currentType = type;
		do {
			Optional<MetaIndex1_8to1_7_6_10> optMeta = getIndex(currentType, index);

			if (optMeta.isPresent()) {
				return optMeta.get();
			}

			currentType = currentType.getParent();
		} while (currentType != null);

		return null;
	}
}
