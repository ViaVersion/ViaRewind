package de.gerrygames.viarewind.utils.math;

import lombok.Getter;

public class AABB {
	@Getter
	Vector3d min;
	@Getter
	Vector3d max;

	public AABB(Vector3d min, Vector3d max) {
		this.min = min;
		this.max = max;
	}
}
