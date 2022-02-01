package tools;

import level.tools.Coordinate;

/**
 * For easy handling of positions in the dungeon. <br>
 * No getter needed. All attributes are public. <br>
 * Point.x to get x <br>
 * Point.y to get y <br>
 */
public class Point {

    public float x, y;

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
     * Convert Point to Coordinate by parsing float to int
     *
     * @return
     */
    public Coordinate toCoordinate() {
        return new Coordinate((int) x, (int) y);
    }
}
