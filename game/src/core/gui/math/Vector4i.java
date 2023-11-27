package core.gui.math;

public class Vector4i {

    private int x, y, z, w;

    public Vector4i(int x, int y, int z, int w) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.w = w;
    }

    public Vector4i(Vector4i other) {
        this.x = other.x;
        this.y = other.y;
        this.z = other.z;
        this.w = other.w;
    }

    public Vector4i(int[] array) {
        this.x = array[0];
        this.y = array[1];
        this.z = array[2];
        this.w = array[3];
    }

    public Vector4i(Vector3i vec3, int w) {
        this.x = vec3.x();
        this.y = vec3.y();
        this.z = vec3.z();
        this.w = w;
    }

    public Vector4i(Vector2i vec2, int z, int w) {
        this.x = vec2.x();
        this.y = vec2.y();
        this.z = z;
        this.w = w;
    }

    public Vector4i copy() {
        return new Vector4i(this);
    }

    public Vector4i add(Vector4i other) {
        this.x += other.x;
        this.y += other.y;
        this.z += other.z;
        this.w += other.w;
        return this;
    }

    public Vector4i add(int x, int y, int z, int w) {
        this.x += x;
        this.y += y;
        this.z += z;
        this.w += w;
        return this;
    }

    public Vector4i subtract(Vector4i other) {
        this.x -= other.x;
        this.y -= other.y;
        this.z -= other.z;
        this.w -= other.w;
        return this;
    }

    public Vector4i subtract(int x, int y, int z, int w) {
        this.x -= x;
        this.y -= y;
        this.z -= z;
        this.w -= w;
        return this;
    }

    public Vector4i multiply(Vector4i other) {
        this.x *= other.x;
        this.y *= other.y;
        this.z *= other.z;
        this.w *= other.w;
        return this;
    }

    public Vector4i multiply(int x, int y, int z, int w) {
        this.x *= x;
        this.y *= y;
        this.z *= z;
        this.w *= w;
        return this;
    }

    public Vector4i divide(Vector4i other) {
        this.x /= other.x;
        this.y /= other.y;
        this.z /= other.z;
        this.w /= other.w;
        return this;
    }

    public Vector4i divide(int x, int y, int z, int w) {
        this.x /= x;
        this.y /= y;
        this.z /= z;
        this.w /= w;
        return this;
    }

    public Vector4i normalize() {
        float length = this.length();
        this.x /= (int) length;
        this.y /= (int) length;
        this.z /= (int) length;
        this.w /= (int) length;
        return this;
    }

    public float length() {
        return (float)
                Math.sqrt(this.x * this.x + this.y * this.y + this.z * this.z + this.w * this.w);
    }

    public int length2() {
        return this.x * this.x + this.y * this.y + this.z * this.z + this.w * this.w;
    }

    public int dot(Vector4i other) {
        return this.x * other.x + this.y * other.y + this.z * other.z + this.w * other.w;
    }

    public int dot(int x, int y, int z, int w) {
        return this.x * x + this.y * y + this.z * z + this.w * w;
    }

    public Vector3i xyz() {
        return new Vector3i(this.x, this.y, this.z);
    }

    public Vector2i xy() {
        return new Vector2i(this.x, this.y);
    }

    public int x() {
        return this.x;
    }

    public Vector4i x(int x) {
        this.x = x;
        return this;
    }

    public int y() {
        return this.y;
    }

    public Vector4i y(int y) {
        this.y = y;
        return this;
    }

    public int z() {
        return this.z;
    }

    public Vector4i z(int z) {
        this.z = z;
        return this;
    }

    public int w() {
        return this.w;
    }

    public Vector4i w(int w) {
        this.w = w;
        return this;
    }

    public int get(int index) {
        return switch (index) {
            case 0 -> this.x;
            case 1 -> this.y;
            case 2 -> this.z;
            case 3 -> this.w;
            default -> throw new IndexOutOfBoundsException();
        };
    }

    public Vector4f toFloat() {
        return new Vector4f(this.x, this.y, this.z, this.w);
    }

    public int[] toArray() {
        return new int[] {this.x, this.y, this.z, this.w};
    }

    @Override
    public String toString() {
        return String.format("Vector4f(%d, %d, %d, %d)", this.x, this.y, this.z, this.w);
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof Vector4i vector)) {
            return false;
        }
        return this.x == vector.x && this.y == vector.y && this.z == vector.z && this.w == vector.w;
    }

    public static Vector4i zero() {
        return new Vector4i(0, 0, 0, 0);
    }

    public static Vector4i one() {
        return new Vector4i(1, 1, 1, 1);
    }

    public static Vector4i unitX() {
        return new Vector4i(1, 0, 0, 0);
    }

    public static Vector4i unitY() {
        return new Vector4i(0, 1, 0, 0);
    }

    public static Vector4i unitZ() {
        return new Vector4i(0, 0, 1, 0);
    }

    public static Vector4i unitW() {
        return new Vector4i(0, 0, 0, 1);
    }
}
