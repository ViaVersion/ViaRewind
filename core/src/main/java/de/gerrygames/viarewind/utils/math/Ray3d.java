package de.gerrygames.viarewind.utils.math;

import lombok.Getter;

public class Ray3d {
	@Getter
	Vector3d start;
	@Getter
	Vector3d dir;

	public Ray3d(Vector3d start, Vector3d dir) {
		this.start = start;
		this.dir = dir;
	}
}
