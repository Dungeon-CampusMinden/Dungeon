package core.utils.math;

public class Vector4f {

    private float x, y, z, w;

    public Vector4f(float x, float y, float z, float w) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.w = w;
    }

    public Vector4f(Vector4f other) {
        this.x = other.x;
        this.y = other.y;
        this.z = other.z;
        this.w = other.w;
    }

    public Vector4f(float[] array) {
        this.x = array[0];
        this.y = array[1];
        this.z = array[2];
        this.w = array[3];
    }

    public Vector4f(Vector3f vec3, float w) {
        this.x = vec3.x();
        this.y = vec3.y();
        this.z = vec3.z();
        this.w = w;
    }

    public Vector4f(Vector2f vec2, float z, float w) {
        this.x = vec2.x();
        this.y = vec2.y();
        this.z = z;
        this.w = w;
    }

    public Vector4f copy() {
        return new Vector4f(this);
    }

    public Vector4f add(Vector4f other) {
        this.x += other.x;
        this.y += other.y;
        this.z += other.z;
        this.w += other.w;
        return this;
    }

    public Vector4f add(float x, float y, float z, float w) {
        this.x += x;
        this.y += y;
        this.z += z;
        this.w += w;
        return this;
    }

    public Vector4f subtract(Vector4f other) {
        this.x -= other.x;
        this.y -= other.y;
        this.z -= other.z;
        this.w -= other.w;
        return this;
    }

    public Vector4f subtract(float x, float y, float z, float w) {
        this.x -= x;
        this.y -= y;
        this.z -= z;
        this.w -= w;
        return this;
    }

    public Vector4f multiply(Vector4f other) {
        this.x *= other.x;
        this.y *= other.y;
        this.z *= other.z;
        this.w *= other.w;
        return this;
    }

    public Vector4f multiply(float x, float y, float z, float w) {
        this.x *= x;
        this.y *= y;
        this.z *= z;
        this.w *= w;
        return this;
    }

    public Vector4f divide(Vector4f other) {
        this.x /= other.x;
        this.y /= other.y;
        this.z /= other.z;
        this.w /= other.w;
        return this;
    }

    public Vector4f divide(float x, float y, float z, float w) {
        this.x /= x;
        this.y /= y;
        this.z /= z;
        this.w /= w;
        return this;
    }

    public Vector4f normalize() {
        float length = this.length();
        this.x /= length;
        this.y /= length;
        this.z /= length;
        this.w /= length;
        return this;
    }

    public float length() {
        return (float)
                Math.sqrt(this.x * this.x + this.y * this.y + this.z * this.z + this.w * this.w);
    }

    public float length2() {
        return this.x * this.x + this.y * this.y + this.z * this.z + this.w * this.w;
    }

    public float dot(Vector4f other) {
        return this.x * other.x + this.y * other.y + this.z * other.z + this.w * other.w;
    }

    public float dot(float x, float y, float z, float w) {
        return this.x * x + this.y * y + this.z * z + this.w * w;
    }

    public Vector3f xyz() {
        return new Vector3f(this.x, this.y, this.z);
    }

    public Vector2f xy() {
        return new Vector2f(this.x, this.y);
    }

    public float x() {
        return this.x;
    }

    public Vector4f x(float x) {
        this.x = x;
        return this;
    }

    public float y() {
        return this.y;
    }

    public Vector4f y(float y) {
        this.y = y;
        return this;
    }

    public float z() {
        return this.z;
    }

    public Vector4f z(float z) {
        this.z = z;
        return this;
    }

    public float w() {
        return this.w;
    }

    public Vector4f w(float w) {
        this.w = w;
        return this;
    }

    public float get(int index) {
        return switch (index) {
            case 0 -> this.x;
            case 1 -> this.y;
            case 2 -> this.z;
            case 3 -> this.w;
            default -> throw new IndexOutOfBoundsException();
        };
    }

    public Vector4i toInt() {
        return new Vector4i(
                Math.round(this.x), Math.round(this.y), Math.round(this.z), Math.round(this.w));
    }

    public float[] toArray() {
        return new float[] {this.x, this.y, this.z, this.w};
    }

    /**
     * Convert the vector to a RGBA color.
     *
     * @return RGBA color
     */
    public int toRGBA() {
        return ((int) (this.x * 255) << 24)
                | ((int) (this.y * 255) << 16)
                | ((int) (this.z * 255) << 8)
                | ((int) (this.w * 255));
    }

    @Override
    public String toString() {
        return String.format("Vector4f(%f, %f, %f, %f)", this.x, this.y, this.z, this.w);
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof Vector4f vector)) {
            return false;
        }
        return this.x == vector.x && this.y == vector.y && this.z == vector.z && this.w == vector.w;
    }

    public static Vector4f zero() {
        return new Vector4f(0, 0, 0, 0);
    }

    public static Vector4f one() {
        return new Vector4f(1, 1, 1, 1);
    }

    public static Vector4f unitX() {
        return new Vector4f(1, 0, 0, 0);
    }

    public static Vector4f unitY() {
        return new Vector4f(0, 1, 0, 0);
    }

    public static Vector4f unitZ() {
        return new Vector4f(0, 0, 1, 0);
    }

    public static Vector4f unitW() {
        return new Vector4f(0, 0, 0, 1);
    }

    public static Vector4f fromRGBA(int rgba) {
        return new Vector4f(
                ((rgba >> 24) & 0xFF) / 255.0f,
                ((rgba >> 16) & 0xFF) / 255.0f,
                ((rgba >> 8) & 0xFF) / 255.0f,
                (rgba & 0xFF) / 255.0f);
    }
}
