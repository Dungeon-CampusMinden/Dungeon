package level.elements;

import level.tools.LevelElement;
import tools.Point;

public class Tile {

    private String texture;
    private Point globalPosition;
    private LevelElement e;

    public Tile(String texture, Point p, LevelElement e) {
        this.texture = texture;
        this.e = e;
        globalPosition = p;
    }

    public String getTexture() {
        return texture;
    }

    public Point getGlobalPosition() {
        return globalPosition;
    }

    public LevelElement getLevelElement() {
        return e;
    }
}
