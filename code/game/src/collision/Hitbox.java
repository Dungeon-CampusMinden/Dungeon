package collision;

import tools.Point;

public class Hitbox {
    // Local Position of the hitbox, bottom left is always (0|0), add position.x/y to get the
    // position of the hitbox in the game
    private Point[] corners;
    private Point position;

    /**
     * @param widthInPixel Width of the hitbox in pixels
     * @param heightInPixel Height of the hitbox in pixels
     * @param position Position of the lower left corner of the hitbox (0|0)
     */
    public Hitbox(int widthInPixel, int heightInPixel, Point position) {
        this.position = position;
        // from pixel to point 16px x 16px =1x1
        float width = widthInPixel / 16f;
        float height = heightInPixel / 16f;
        corners = new Point[4];
        corners[0] = new Point(0, 0);
        corners[1] = new Point(0, height);
        corners[2] = new Point(width, height);
        corners[3] = new Point(width, 0);
    }

    /**
     * @return The local positions of the corners.
     */
    public Point[] getCorners() {
        return corners;
    }

    /**
     * Check if two hitboxes colided with each other. TODO
     *
     * @param other Hitbox to check for collision with
     * @return The direction from which this Hitbox consolidates with the other. NONE if there is no
     *     collision
     */
    public CharacterDirection colide(Hitbox other) {
        return CharacterDirection.NONE;
    }

    /**
     * Set the position of the bottom left corner
     *
     * @param position
     */
    public void setPosition(Point position) {
        this.position = position;
    }

    /**
     * Get the position of the bottom left corner
     *
     * @return
     */
    public Point getPosition() {
        return position;
    }
}
