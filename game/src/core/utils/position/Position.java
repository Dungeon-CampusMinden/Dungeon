package core.utils.position;

import java.util.Optional;

public interface Position {

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
        float xDiff = p1.point().x - p2.point().x;
        float yDiff = p1.point().y - p2.point().y;
        return (float) Math.sqrt(xDiff * xDiff + yDiff * yDiff);
    }

    /**
     * Check if two points are positioned in a specified range from each other.
     *
     * @param p1 The first point which is considered.
     * @param p2 The second point which is considered.
     * @param range The range in which the two points are positioned from each other.
     * @return True if the distance between the two points is within the radius, else false.
     */
    static boolean inRange(final Position p1, final Position p2, final float range) {
        return calculateDistance(p1, p2) <= range;
    }

    /**
     * Creates the unit vector between point a and b
     *
     * @param a Position A
     * @param b Position B
     * @return the unit vector
     */
    public static Position unitDirectionalVector(Position b, Position a) {
        Point interactionDir = new Point(b.point().x, b.point().y);
        // (interactable - a) / len(interactable - a)
        interactionDir.x -= a.point().x;
        interactionDir.y -= a.point().y;
        double vecLength = calculateDistance(a, b);
        interactionDir.x /= vecLength;
        interactionDir.y /= vecLength;
        return interactionDir;
    }
}
