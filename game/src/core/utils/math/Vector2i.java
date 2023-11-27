package core.utils.math;

public class Vector2i {

    private int x, y;

    public Vector2i(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public Vector2i(Vector2i other) {
        this.x = other.x;
        this.y = other.y;
    }

    public Vector2i copy() {
        return new Vector2i(this);
    }

    public Vector2i add(Vector2i other) {
        this.x += other.x;
        this.y += other.y;
        return this;
    }

    public Vector2i add(int x, int y) {
        this.x += x;
        this.y += y;
        return this;
    }

    public Vector2i subtract(Vector2i other) {
        this.x -= other.x;
        this.y -= other.y;
        return this;
    }

    public Vector2i subtract(int x, int y) {
        this.x -= x;
        this.y -= y;
        return this;
    }

    public Vector2i multiply(Vector2i other) {
        this.x *= other.x;
        this.y *= other.y;
        return this;
    }

    public Vector2i multiply(int x, int y) {
        this.x *= x;
        this.y *= y;
        return this;
    }

    public Vector2i divide(Vector2i other) {
        this.x /= other.x;
        this.y /= other.y;
        return this;
    }

    public Vector2i divide(int x, int y) {
        this.x /= x;
        this.y /= y;
        return this;
    }

    public int length() {
        return (int) Math.sqrt(this.x * this.x + this.y * this.y);
    }

    public Vector2i normalize() {
        int length = this.length();
        this.x /= length;
        this.y /= length;
        return this;
    }

    public int dot(Vector2i other) {
        return this.x * other.x + this.y * other.y;
    }

    public int dot(int x, int y) {
        return this.x * x + this.y * y;
    }

    public int cross(Vector2i other) {
        return this.x * other.y - this.y * other.x;
    }

    public int cross(int x, int y) {
        return this.x * y - this.y * x;
    }

    public Vector2i rotate(int angle) {
        int cos = (int) Math.cos(angle);
        int sin = (int) Math.sin(angle);
        int newX = this.x * cos - this.y * sin;
        int newY = this.x * sin + this.y * cos;
        this.x = newX;
        this.y = newY;
        return this;
    }

    public Vector2i rotateAround(Vector2i point, int angle) {
        int cos = (int) Math.cos(angle);
        int sin = (int) Math.sin(angle);
        int newX = point.x + (this.x - point.x) * cos - (this.y - point.y) * sin;
        int newY = point.y + (this.x - point.x) * sin + (this.y - point.y) * cos;
        this.x = newX;
        this.y = newY;
        return this;
    }

    public Vector2i rotateAround(int x, int y, int angle) {
        int cos = (int) Math.cos(angle);
        int sin = (int) Math.sin(angle);
        int newX = x + (this.x - x) * cos - (this.y - y) * sin;
        int newY = y + (this.x - x) * sin + (this.y - y) * cos;
        this.x = newX;
        this.y = newY;
        return this;
    }

    public int x() {
        return this.x;
    }

    public Vector2i x(int x) {
        this.x = x;
        return this;
    }

    public int y() {
        return this.y;
    }

    public Vector2i y(int y) {
        this.y = y;
        return this;
    }

    public int get(int index) {
        return switch (index) {
            case 0 -> this.x;
            case 1 -> this.y;
            default -> throw new IndexOutOfBoundsException();
        };
    }

    public Vector2f toFloat() {
        return new Vector2f(this.x, this.y);
    }

    public int[] toArray() {
        return new int[] {this.x, this.y};
    }

    @Override
    public String toString() {
        return String.format("Vector2i(%d, %d)", this.x, this.y);
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof Vector2i vector)) return false;
        return this.x == vector.x && this.y == vector.y;
    }

    public static Vector2i zero() {
        return new Vector2i(0, 0);
    }

    public static Vector2i one() {
        return new Vector2i(1, 1);
    }

    public static Vector2i unitX() {
        return new Vector2i(1, 0);
    }

    public static Vector2i unitY() {
        return new Vector2i(0, 1);
    }
}
