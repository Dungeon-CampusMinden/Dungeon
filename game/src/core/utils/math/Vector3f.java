package core.utils.math;

public class Vector3f {

    private float x, y, z;

    public Vector3f(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Vector3f(Vector3f other) {
        this.x = other.x;
        this.y = other.y;
        this.z = other.z;
    }

    public Vector3f(Vector2f vec2, float z) {
        this.x = vec2.x();
        this.y = vec2.y();
        this.z = z;
    }

    public Vector3f copy() {
        return new Vector3f(this);
    }

    public Vector3f add(Vector3f other) {
        this.x += other.x;
        this.y += other.y;
        this.z += other.z;
        return this;
    }

    public Vector3f add(float x, float y, float z) {
        this.x += x;
        this.y += y;
        this.z += z;
        return this;
    }

    public Vector3f subtract(Vector3f other) {
        this.x -= other.x;
        this.y -= other.y;
        this.z -= other.z;
        return this;
    }

    public Vector3f subtract(float x, float y, float z) {
        this.x -= x;
        this.y -= y;
        this.z -= z;
        return this;
    }

    public Vector3f multiply(Vector3f other) {
        this.x *= other.x;
        this.y *= other.y;
        this.z *= other.z;
        return this;
    }

    public Vector3f multiply(float x, float y, float z) {
        this.x *= x;
        this.y *= y;
        this.z *= z;
        return this;
    }

    public Vector3f divide(Vector3f other) {
        this.x /= other.x;
        this.y /= other.y;
        this.z /= other.z;
        return this;
    }

    public Vector3f divide(float x, float y, float z) {
        this.x /= x;
        this.y /= y;
        this.z /= z;
        return this;
    }

    public float length() {
        return (float) Math.sqrt(this.x * this.x + this.y * this.y + this.z * this.z);
    }

    public float length2() {
        return this.x * this.x + this.y * this.y + this.z * this.z;
    }

    public Vector3f normalize() {
        float length = this.length();
        this.x /= length;
        this.y /= length;
        this.z /= length;
        return this;
    }

    public float dot(Vector3f other) {
        return this.x * other.x + this.y * other.y + this.z * other.z;
    }

    public float dot(float x, float y, float z) {
        return this.x * x + this.y * y + this.z * z;
    }

    public Vector3f cross(Vector3f other) {
        float x = this.y * other.z - this.z * other.y;
        float y = this.z * other.x - this.x * other.z;
        float z = this.x * other.y - this.y * other.x;
        this.x = x;
        this.y = y;
        this.z = z;
        return this;
    }

    public Vector3f cross(float x, float y, float z) {
        float x1 = this.y * z - this.z * y;
        float y1 = this.z * x - this.x * z;
        float z1 = this.x * y - this.y * x;
        this.x = x1;
        this.y = y1;
        this.z = z1;
        return this;
    }

    public Vector3f rotateX(float angle) {
        float radians = (float) Math.toRadians(angle);
        float cos = (float) Math.cos(radians);
        float sin = (float) Math.sin(radians);
        float y = this.y * cos - this.z * sin;
        float z = this.y * sin + this.z * cos;
        this.y = y;
        this.z = z;
        return this;
    }

    public Vector3f rotateY(float angle) {
        float radians = (float) Math.toRadians(angle);
        float cos = (float) Math.cos(radians);
        float sin = (float) Math.sin(radians);
        float x = this.x * cos + this.z * sin;
        float z = -this.x * sin + this.z * cos;
        this.x = x;
        this.z = z;
        return this;
    }

    public Vector3f rotateZ(float angle) {
        float radians = (float) Math.toRadians(angle);
        float cos = (float) Math.cos(radians);
        float sin = (float) Math.sin(radians);
        float x = this.x * cos - this.y * sin;
        float y = this.x * sin + this.y * cos;
        this.x = x;
        this.y = y;
        return this;
    }

    public Vector3f rotate(float x, float y, float z) {
        this.rotateX(x);
        this.rotateY(y);
        this.rotateZ(z);
        return this;
    }

    public Vector3f rotate(Vector3f other) {
        this.rotateX(other.x);
        this.rotateY(other.y);
        this.rotateZ(other.z);
        return this;
    }

    public Vector2f xy() {
        return new Vector2f(this.x, this.y);
    }

    public float x() {
        return this.x;
    }

    public Vector3f x(float x) {
        this.x = x;
        return this;
    }

    public float y() {
        return this.y;
    }

    public Vector3f y(float y) {
        this.y = y;
        return this;
    }

    public float z() {
        return this.z;
    }

    public Vector3f z(float z) {
        this.z = z;
        return this;
    }

    public float get(int index) {
        return switch (index) {
            case 0 -> this.x;
            case 1 -> this.y;
            case 2 -> this.z;
            default -> throw new IndexOutOfBoundsException();
        };
    }

    public Vector3i toInt() {
        return new Vector3i(Math.round(this.x), Math.round(this.y), Math.round(this.z));
    }

    public float[] toArray() {
        return new float[] {this.x, this.y, this.z};
    }

    @Override
    public String toString() {
        return String.format("Vector3f(%f, %f, %f)", this.x, this.y, this.z);
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof Vector3f vector)) {
            return false;
        }
        return this.x == vector.x && this.y == vector.y && this.z == vector.z;
    }

    public static Vector3f zero() {
        return new Vector3f(0, 0, 0);
    }

    public static Vector3f one() {
        return new Vector3f(1, 1, 1);
    }

    public static Vector3f unitX() {
        return new Vector3f(1, 0, 0);
    }

    public static Vector3f unitY() {
        return new Vector3f(0, 1, 0);
    }

    public static Vector3f unitZ() {
        return new Vector3f(0, 0, 1);
    }
}
