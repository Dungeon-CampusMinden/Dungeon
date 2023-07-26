package core.utils.position;

public class Point implements Position {
    public float x;
    public float y;

    public Point(Number x, Number y) {
        this.x = x.floatValue();
        this.y = y.floatValue();
    }

    public Point(Position oldPoint) {
        x = oldPoint.point().x;
        y = oldPoint.point().y;
    }

    public int x_i() {
        return (int) x;
    }

    public int y_i() {
        return (int) y;
    }

    @Override
    public Point point() {
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Point)) {
            return false;
        }
        Point other = (Point) o;
        return Float.compare(x, other.x) == 0 && Float.compare(y, other.y) == 0;
    }
}
