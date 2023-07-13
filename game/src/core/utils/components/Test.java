package core.utils.components;

public interface Test {
    interface Position {
        default Pair<Integer> tile() {
            return new Pair<>(pixel().x().intValue(), pixel().y().intValue());
        }

        default Pair<Float> pixel() {
            return new Pair<>(tile().x().floatValue(), tile().y().floatValue());
        }
    }

    record Pair<T>(T x, T y) {}

    final class Point implements Position {
        private final Pair<Float> position;

        public Point(float x, float y) {
            position = new Pair<>(x, y);
        }

        @Override
        public Pair<Float> pixel() {
            return position;
        }
    }

    final class Coordinate implements Position {
        private final Pair<Integer> position;

        public Coordinate(int x, int y) {
            position = new Pair<>(x, y);
        }

        @Override
        public Pair<Integer> tile() {
            return position;
        }
    }
}
