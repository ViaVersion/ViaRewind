package de.gerrygames.viarewind.utils.math;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Objects;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class Vector3d {
	double x, y, z;

	public void set(Vector3d vec) {
		this.x = vec.x;
		this.y = vec.y;
		this.z = vec.z;
	}

	public Vector3d set(double x, double y, double z) {
		this.x = x;
		this.y = y;
		this.z = z;
		return this;
	}

	public Vector3d multiply(double a) {
		this.x *= a;
		this.y *= a;
		this.z *= a;
		return this;
	}

	public Vector3d add(Vector3d vec) {
		this.x += vec.x;
		this.y += vec.y;
		this.z += vec.z;
		return this;
	}

	public Vector3d substract(Vector3d vec) {
		this.x -= vec.x;
		this.y -= vec.y;
		this.z -= vec.z;
		return this;
	}

	public double length() {
		return Math.sqrt(lengthSquared());
	}

	public double lengthSquared() {
		return this.x * this.x + this.y * this.y + this.z * this.z;
	}

	public Vector3d normalize() {
		double length = length();
		multiply(1.0 / length);
		return this;
	}

	public Vector3d clone() {
		return new Vector3d(this.x, this.y, this.z);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Vector3d vector3d = (Vector3d) o;
		return Double.compare(vector3d.x, x) == 0 && Double.compare(vector3d.y, y) == 0 && Double.compare(vector3d.z, z) == 0;
	}

	@Override
	public int hashCode() {
		return Objects.hash(x, y, z);
	}

	@Override
	public String toString() {
		return "Vector3d{" + "x=" + x + ", y=" + y + ", z=" + z + '}';
	}
}
