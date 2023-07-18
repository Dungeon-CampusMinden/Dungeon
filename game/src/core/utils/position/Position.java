package core.utils.position;

import java.util.Optional;

/// **
// * For easy handling of positions in the dungeon. <br>
// * No getter needed. All attributes are public. <br>
// * Position.x to get x <br>
// * Position.y to get y <br>
// */
// public class Position {
//
//    public float x;
//    public float y;
//
//    /**
//     * A simple {@code float} point class.
//     *
//     * @param x the x value
//     * @param y the y value
//     */
//    public Position(float x, float y) {
//        this.x = x;
//        this.y = y;
//    }
//
//    /** Copies the point. */
//    public Position(Position p) {
//        x = p.x;
//        y = p.y;
//    }
//
//    /**
//     * Check if two points are positioned in a specified range from each other.
//     *
//     * @param p1 The first point which is considered.
//     * @param p2 The second point which is considered.
//     * @param range The range in which the two points are positioned from each other.
//     * @return True if the distance between the two points is within the radius, else false.
//     */
//    public static boolean inRange(final Position p1, final Position p2, final float range) {
//        return calculateDistance(p1, p2) <= range;
//    }
//
//    /**
//     * Convert Position to Coordinate by parsing float to int
//     *
//     * @return the converted point
//     */
//    public Coordinate toCoordinate() {
//        return new Coordinate((int) x, (int) y);
//    }
//
//    /**
//     * Creates the unit vector between point a and b
//     *
//     * @param a Position A
//     * @param b Position B
//     * @return the unit vector
//     */
//    public static Position unitDirectionalVector(Position b, Position a) {
//        Position interactionDir = new Position(b);
//        // (interactable - a) / len(interactable - a)
//        interactionDir.x -= a.x;
//        interactionDir.y -= a.y;
//        double vecLength = calculateDistance(a, b);
//        interactionDir.x /= vecLength;
//        interactionDir.y /= vecLength;
//        return interactionDir;
//    }
//    /**
//     * calculates the distance between two points
//     *
//     * @param p1 Position A
//     * @param p2 Position B
//     * @return the Distance between the two points
//     */
//    public static float calculateDistance(Position p1, Position p2) {
//        float xDiff = p1.x - p2.x;
//        float yDiff = p1.y - p2.y;
//        return (float) Math.sqrt(xDiff * xDiff + yDiff * yDiff);
//    }
// }

public interface Position {
    // tile-based access
    Optional<Coordinate> coordinate();

    // pixel-perfect access
    Point point();

    /**
     * calculates the distance between two points
     *
     * @param p1 Position A
     * @param p2 Position B
     * @return the Distance between the two points
     */
    static float calculateDistance(Position p1, Position p2) {
        float xDiff = p1.point().x() - p2.point().x();
        float yDiff = p1.point().y() - p2.point().y();
        return (float) Math.sqrt(xDiff * xDiff + yDiff * yDiff);
    }
}
