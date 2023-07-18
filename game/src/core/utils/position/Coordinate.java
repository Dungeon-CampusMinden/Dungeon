package core.utils.position;

import java.util.Optional;

public class Coordinate implements Position {
    public int x;
    public int y;


    public Coordinate(Number x, Number y) {
        this.x = x.intValue();
        this.y = y.intValue();
    }

    public Optional<Coordinate> coordinate() {
        return Optional.of(this);
    }

    public Point point() {
        return new Point(x, y);
    }
}
