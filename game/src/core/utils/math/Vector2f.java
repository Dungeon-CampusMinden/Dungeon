package core.utils.math;

public class Vector2f {

    private float x, y;

    public Vector2f(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public Vector2f(Vector2f other) {
        this.x = other.x;
        this.y = other.y;
    }

    public Vector2f copy() {
        return new Vector2f(this);
    }

    public Vector2f add(Vector2f other) {
        this.x += other.x;
        this.y += other.y;
        return this;
    }

    public Vector2f add(float x, float y) {
        this.x += x;
        this.y += y;
        return this;
    }

    public Vector2f subtract(Vector2f other) {
        this.x -= other.x;
        this.y -= other.y;
        return this;
    }

    public Vector2f subtract(float x, float y) {
        this.x -= x;
        this.y -= y;
        return this;
    }

    public Vector2f multiply(Vector2f other) {
        this.x *= other.x;
        this.y *= other.y;
        return this;
    }

    public Vector2f multiply(float x, float y) {
        this.x *= x;
        this.y *= y;
        return this;
    }

    public Vector2f divide(Vector2f other) {
        this.x /= other.x;
        this.y /= other.y;
        return this;
    }

    public Vector2f divide(float x, float y) {
        this.x /= x;
        this.y /= y;
        return this;
    }

    public float length() {
        return (float) Math.sqrt(this.x * this.x + this.y * this.y);
    }

    public Vector2f normalize() {
        float length = this.length();
        this.x /= length;
        this.y /= length;
        return this;
    }

    public float dot(Vector2f other) {
        return this.x * other.x + this.y * other.y;
    }

    public float dot(float x, float y) {
        return this.x * x + this.y * y;
    }

    public float cross(Vector2f other) {
        return this.x * other.y - this.y * other.x;
    }

    public float cross(float x, float y) {
        return this.x * y - this.y * x;
    }

    public Vector2f rotate(float angle) {
        float cos = (float) Math.cos(angle);
        float sin = (float) Math.sin(angle);
        float newX = this.x * cos - this.y * sin;
        float newY = this.x * sin + this.y * cos;
        this.x = newX;
        this.y = newY;
        return this;
    }

    public Vector2f rotateAround(Vector2f point, float angle) {
        float cos = (float) Math.cos(angle);
        float sin = (float) Math.sin(angle);
        float newX = point.x + (this.x - point.x) * cos - (this.y - point.y) * sin;
        float newY = point.y + (this.x - point.x) * sin + (this.y - point.y) * cos;
        this.x = newX;
        this.y = newY;
        return this;
    }

    public Vector2f rotateAround(float x, float y, float angle) {
        float cos = (float) Math.cos(angle);
        float sin = (float) Math.sin(angle);
        float newX = x + (this.x - x) * cos - (this.y - y) * sin;
        float newY = y + (this.x - x) * sin + (this.y - y) * cos;
        this.x = newX;
        this.y = newY;
        return this;
    }

    public float x() {
        return this.x;
    }

    public Vector2f x(float x) {
        this.x = x;
        return this;
    }

    public float y() {
        return this.y;
    }

    public Vector2f y(float y) {
        this.y = y;
        return this;
    }

    public float get(int index) {
        return switch (index) {
            case 0 -> this.x;
            case 1 -> this.y;
            default -> throw new IndexOutOfBoundsException();
        };
    }

    public Vector2i toInt() {
        return new Vector2i(Math.round(this.x), Math.round(this.y));
    }

    public float[] toArray() {
        return new float[] {this.x, this.y};
    }

    @Override
    public String toString() {
        return String.format("Vector2f(%f, %f)", this.x, this.y);
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof Vector2f vector)) return false;
        return this.x == vector.x && this.y == vector.y;
    }

    public static Vector2f zero() {
        return new Vector2f(0, 0);
    }

    public static Vector2f one() {
        return new Vector2f(1, 1);
    }

    public static Vector2f unitX() {
        return new Vector2f(1, 0);
    }

    public static Vector2f unitY() {
        return new Vector2f(0, 1);
    }
}
