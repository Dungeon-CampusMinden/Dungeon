package core.utils.position;

import java.util.Optional;

public class Point implements Position {
    private float x;
    private float y;

    public float x() {
        return x;
    }

    public float y() {
        return y;
    }

    public Point(Number x, Number y) {
        this.x = x.floatValue();
        this.y = y.floatValue();
    }

    @Override
    public Optional<Tile> tile() {
        return Optional.of(new Tile(x(), y()));
    }

    @Override
    public Point point() {
        return this;
    }
}
