package core.utils.position;

import java.util.Optional;

public class Coordinate implements Position {
    private int x;
    private int y;

    // direct access to x and y
    public int x() {
        return x;
    }

    public int y() {
        return y;
    }

    public Coordinate(Number x, Number y) {
        this.x = x.intValue();
        this.y = y.intValue();
    }

    public Optional<Coordinate> coordinate() {
        return Optional.of(this);
    }

    public Point point() {
        return new Point(x(), y());
    }
}
