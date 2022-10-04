package collision;

import tools.Point;

public class Hitbox {
    private Point[] corners;
    private Point position;

    public Hitbox(int widthPX, int heightPX, Point position) {
        this.position = position;
        // from pixel to point 16px x 16px =1x1
        float width = widthPX / 16f;
        float height = heightPX / 16f;
        corners = new Point[4];
        corners[0] = new Point(0, 0);
        corners[1] = new Point(0, height);
        corners[2] = new Point(width, height);
        corners[3] = new Point(width, 0);
    }

    public Point[] getCorners() {
        return corners;
    }

    // TODO
    public CharacterDirection colide(Hitbox other) {
        return CharacterDirection.NONE;
    }

    public void setPosition(Point position) {
        this.position = position;
    }

    public Point getPosition() {
        return position;
    }
}
