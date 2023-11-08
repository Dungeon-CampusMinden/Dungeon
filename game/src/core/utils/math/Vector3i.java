package core.utils.math;

public class Vector3i {

    private int x, y, z;

    public Vector3i(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Vector3i(Vector3i other) {
        this.x = other.x;
        this.y = other.y;
        this.z = other.z;
    }

    public Vector3i(Vector2i vec2, int z) {
        this.x = vec2.x();
        this.y = vec2.y();
        this.z = z;
    }

    public Vector3i copy() {
        return new Vector3i(this);
    }

    public Vector3i add(Vector3i other) {
        this.x += other.x;
        this.y += other.y;
        this.z += other.z;
        return this;
    }

    public Vector3i add(int x, int y, int z) {
        this.x += x;
        this.y += y;
        this.z += z;
        return this;
    }

    public Vector3i subtract(Vector3i other) {
        this.x -= other.x;
        this.y -= other.y;
        this.z -= other.z;
        return this;
    }

    public Vector3i subtract(int x, int y, int z) {
        this.x -= x;
        this.y -= y;
        this.z -= z;
        return this;
    }

    public Vector3i multiply(Vector3i other) {
        this.x *= other.x;
        this.y *= other.y;
        this.z *= other.z;
        return this;
    }

    public Vector3i multiply(int x, int y, int z) {
        this.x *= x;
        this.y *= y;
        this.z *= z;
        return this;
    }

    public Vector3i divide(Vector3i other) {
        this.x /= other.x;
        this.y /= other.y;
        this.z /= other.z;
        return this;
    }

    public Vector3i divide(int x, int y, int z) {
        this.x /= x;
        this.y /= y;
        this.z /= z;
        return this;
    }

    public int length() {
        return (int) Math.sqrt(this.x * this.x + this.y * this.y + this.z * this.z);
    }

    public int length2() {
        return this.x * this.x + this.y * this.y + this.z * this.z;
    }

    public Vector3i normalize() {
        int length = this.length();
        this.x /= length;
        this.y /= length;
        this.z /= length;
        return this;
    }

    public int dot(Vector3i other) {
        return this.x * other.x + this.y * other.y + this.z * other.z;
    }

    public int dot(int x, int y, int z) {
        return this.x * x + this.y * y + this.z * z;
    }

    public Vector3i cross(Vector3i other) {
        int x = this.y * other.z - this.z * other.y;
        int y = this.z * other.x - this.x * other.z;
        int z = this.x * other.y - this.y * other.x;
        this.x = x;
        this.y = y;
        this.z = z;
        return this;
    }

    public Vector3i cross(int x, int y, int z) {
        int x1 = this.y * z - this.z * y;
        int y1 = this.z * x - this.x * z;
        int z1 = this.x * y - this.y * x;
        this.x = x1;
        this.y = y1;
        this.z = z1;
        return this;
    }

    public Vector3i rotateX(int angle) {
        int radians = (int) Math.toRadians(angle);
        int cos = (int) Math.cos(radians);
        int sin = (int) Math.sin(radians);
        int y = this.y * cos - this.z * sin;
        int z = this.y * sin + this.z * cos;
        this.y = y;
        this.z = z;
        return this;
    }

    public Vector3i rotateY(int angle) {
        int radians = (int) Math.toRadians(angle);
        int cos = (int) Math.cos(radians);
        int sin = (int) Math.sin(radians);
        int x = this.x * cos + this.z * sin;
        int z = -this.x * sin + this.z * cos;
        this.x = x;
        this.z = z;
        return this;
    }

    public Vector3i rotateZ(int angle) {
        int radians = (int) Math.toRadians(angle);
        int cos = (int) Math.cos(radians);
        int sin = (int) Math.sin(radians);
        int x = this.x * cos - this.y * sin;
        int y = this.x * sin + this.y * cos;
        this.x = x;
        this.y = y;
        return this;
    }

    public Vector3i rotate(int x, int y, int z) {
        this.rotateX(x);
        this.rotateY(y);
        this.rotateZ(z);
        return this;
    }

    public Vector3i rotate(Vector3i other) {
        this.rotateX(other.x);
        this.rotateY(other.y);
        this.rotateZ(other.z);
        return this;
    }

    public Vector2i xy() {
        return new Vector2i(this.x, this.y);
    }

    public int x() {
        return this.x;
    }

    public Vector3i x(int x) {
        this.x = x;
        return this;
    }

    public int y() {
        return this.y;
    }

    public Vector3i y(int y) {
        this.y = y;
        return this;
    }

    public int z() {
        return this.z;
    }

    public Vector3i z(int z) {
        this.z = z;
        return this;
    }

    public int get(int index) {
        return switch (index) {
            case 0 -> this.x;
            case 1 -> this.y;
            case 2 -> this.z;
            default -> throw new IndexOutOfBoundsException();
        };
    }

    public int[] toArray() {
        return new int[] {this.x, this.y, this.z};
    }

    @Override
    public String toString() {
        return String.format("Vector3i(%d, %d, %d)", this.x, this.y, this.z);
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof Vector3i vector)) {
            return false;
        }
        return this.x == vector.x && this.y == vector.y && this.z == vector.z;
    }

    public static Vector3i zero() {
        return new Vector3i(0, 0, 0);
    }

    public static Vector3i one() {
        return new Vector3i(1, 1, 1);
    }

    public static Vector3i unitX() {
        return new Vector3i(1, 0, 0);
    }

    public static Vector3i unitY() {
        return new Vector3i(0, 1, 0);
    }

    public static Vector3i unitZ() {
        return new Vector3i(0, 0, 1);
    }
}
