package core.utils.position;

import java.util.Optional;

public class Point implements Position {
    public float x;
    public float y;

    public Point(Number x, Number y) {
        this.x = x.floatValue();
        this.y = y.floatValue();
    }

    @Override
    public Optional<Coordinate> coordinate() {
        return Optional.of(new Coordinate(x, y));
    }

    @Override
    public Point point() {
        return this;
    }
}
