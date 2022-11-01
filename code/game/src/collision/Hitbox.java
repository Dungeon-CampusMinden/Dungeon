package collision;

import tools.Point;

public class Hitbox {
    public static final int CORNER_BOTTOM_LEFT = 0,
            CORNER_TOP_LEFT = 1,
            CORNER_TOP_RIGHT = 2,
            CORNER_BOTTOM_RIGHT = 3;

    // Local Position of the hitbox, bottom left is always (0|0), add position.x/y to get the
    // position of the hitbox in the game
    private Point[] corners;
    private Collidable collidable;

    /**
     * Position of the lower left corner of the hitbox (0|0)
     *
     * @param widthInPixel Width of the hitbox in pixels
     * @param heightInPixel Height of the hitbox in pixels
     */
    public Hitbox(int widthInPixel, int heightInPixel) {
        this(widthInPixel, heightInPixel, new Point(0, 0));
    }
    /**
     * @param widthInPixel Width of the hitbox in pixels
     * @param heightInPixel Height of the hitbox in pixels
     * @param offset Offset of the corners of the hitbox
     */
    public Hitbox(int widthInPixel, int heightInPixel, Point offset) {
        // from pixel to point 16px x 16px =1x1
        float width = widthInPixel / 16f;
        float height = heightInPixel / 16f;
        float offsetX = offset.x / 16f;
        float offsetY = offset.y / 16f;

        corners = new Point[4];
        corners[CORNER_BOTTOM_LEFT] = new Point(offsetX, offsetY);
        corners[CORNER_TOP_LEFT] = new Point(offsetX, offsetY + height);
        corners[CORNER_TOP_RIGHT] = new Point(offsetX + width, offsetY + height);
        corners[CORNER_BOTTOM_RIGHT] = new Point(offsetX + width, offsetY);
    }

    /**
     * Get the Corners
     *
     * <p>topLeft: 1 , topRight: 2 bottomLeft:0 , bottomRight: 3
     *
     * @return The local positions of the corners.
     */
    public Point[] getCorners() {
        return corners;
    }

    /**
     * Check if two hitboxes collided with each other. TODO
     *
     * @param other Hitbox to check for collision with
     * @return The direction from which this Hitbox consolidates with the other. NONE if there is no
     *     collision
     */
    public CharacterDirection collide(Hitbox other) {
        // get real position data for the bottomLeft and topRight
        Point bottomLeft = new Point(corners[Hitbox.CORNER_BOTTOM_LEFT]);
        bottomLeft.x += collidable.getPosition().x;
        bottomLeft.y += collidable.getPosition().y;
        Point topRight = new Point(corners[Hitbox.CORNER_TOP_RIGHT]);
        topRight.x += collidable.getPosition().x;
        topRight.y += collidable.getPosition().y;

        // get real position data for the bottomLeft and topRight of the possible collided
        Point otherBottomLeft = new Point(other.corners[Hitbox.CORNER_BOTTOM_LEFT]);
        otherBottomLeft.x += other.collidable.getPosition().x;
        otherBottomLeft.y += other.collidable.getPosition().y;
        Point otherTopRight = new Point(other.corners[Hitbox.CORNER_TOP_RIGHT]);
        otherTopRight.x += other.collidable.getPosition().x;
        otherTopRight.y += other.collidable.getPosition().y;
        // easy axis alligned collision check
        // https://developer.mozilla.org/en-US/docs/Games/Techniques/2D_collision_detection
        if (bottomLeft.x < otherTopRight.x
                && topRight.x > otherBottomLeft.x
                && bottomLeft.y < otherTopRight.y
                && topRight.y > otherBottomLeft.y) {
            // any collision solve the Direction

            Point centerthis = getCenter(bottomLeft, topRight);
            Point centerOther = getCenter(otherBottomLeft, otherTopRight);

            float y = centerOther.y - centerthis.y;
            float x = centerOther.x - centerthis.x;
            float rads = (float) Math.atan2(y, x);
            double piQuarter = Math.PI / 4;
            // Direction based on the radians
            if (rads < 3 * -piQuarter) {
                return CharacterDirection.LEFT;
            } else if (rads < -piQuarter) {
                return CharacterDirection.UP;
            } else if (rads < piQuarter) {
                return CharacterDirection.RIGHT;
            } else if (rads < 3 * piQuarter) {
                return CharacterDirection.DOWN;
            } else {
                return CharacterDirection.LEFT;
            }
        }

        return CharacterDirection.NONE;
    }

    /**
     * Set the collidable of the bottom left corner
     *
     * @param collidable
     */
    public void setCollidable(Collidable collidable) {
        this.collidable = collidable;
    }

    /**
     * Get the position of the bottom left corner
     *
     * @return
     */
    public Collidable getCollidable() {
        return collidable;
    }

    private Point getCenter(Point p1, Point p2) {
        // (p2 - p1) / 2 + p1
        // move p2 to the center with the help of p1 then half the vector and then move back with p1
        float cx = (p2.x - p1.x) / 2f + p1.x;
        float cy = (p2.y - p1.y) / 2f + p2.x;
        return new Point(cx, cy);
    }
}
