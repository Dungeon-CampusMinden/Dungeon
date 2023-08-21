package core.utils;

import core.level.utils.Coordinate;

/**
 * For easy handling of positions in the dungeon. <br>
 * No getter needed. All attributes are public. <br>
 * Point.x to get x <br>
 * Point.y to get y <br>
 */
public class Point {

    private static final float EPSILON = 0.000001f;

    public float x;
    public float y;

    /**
     * A simple {@code float} point class.
     *
     * @param x the x value
     * @param y the y value
     */
    public Point(float x, float y) {
        this.x = x;
        this.y = y;
    }

    /** Copies the point. */
    public Point(Point p) {
        x = p.x;
        y = p.y;
    }

    /**
     * Check if two points are positioned in a specified range from each other.
     *
     * @param p1 The first point which is considered.
     * @param p2 The second point which is considered.
     * @param range The range in which the two points are positioned from each other.
     * @return True if the distance between the two points is within the radius, else false.
     */
    public static boolean inRange(final Point p1, final Point p2, final float range) {
        return calculateDistance(p1, p2) <= range;
    }

    /**
     * Convert Point to Coordinate by parsing float to int
     *
     * @return the converted point
     */
    public Coordinate toCoordinate() {
        return new Coordinate((int) x, (int) y);
    }

    /**
     * Creates the unit vector between point a and b
     *
     * @param a Point A
     * @param b Point B
     * @return the unit vector
     */
    public static Point unitDirectionalVector(Point b, Point a) {
        Point interactionDir = new Point(b);
        // (interactable - a) / len(interactable - a)
        interactionDir.x -= a.x;
        interactionDir.y -= a.y;
        double vecLength = calculateDistance(a, b);
        interactionDir.x /= vecLength;
        interactionDir.y /= vecLength;
        return interactionDir;
    }
    /**
     * calculates the distance between two points
     *
     * @param p1 Point A
     * @param p2 Point B
     * @return the Distance between the two points
     */
    public static float calculateDistance(Point p1, Point p2) {
        float xDiff = p1.x - p2.x;
        float yDiff = p1.y - p2.y;
        return (float) Math.sqrt(xDiff * xDiff + yDiff * yDiff);
    }

    /**
     * Two points are equal, if they have the same x and y values.
     *
     * @param other Point to compare with
     * @return if the x and y values of the points are equal.
     */
    public boolean equals(Point other) {
        return Math.abs(x - other.x) < EPSILON && Math.abs(y - other.y) < EPSILON;
    }
}
