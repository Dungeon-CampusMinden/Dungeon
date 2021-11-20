package tools;
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

    /**
     * Copy Point
     *
     * @param p
     */
    public Point(Point p) {
        this.x = p.x;
        this.y = p.y;
    }
}
