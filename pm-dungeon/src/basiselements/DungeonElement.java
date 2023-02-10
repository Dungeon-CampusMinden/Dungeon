package basiselements;

import graphic.Painter;
import graphic.PainterConfig;
import java.util.HashMap;
import java.util.Map;
import tools.Point;

/**
 * An object that has a position and a texture path.
 *
 * <p>Must be implemented for all objects that should be controlled by the <code>EntityController
 * </code>.
 */
public abstract class DungeonElement implements Removable {
    protected Map<String, PainterConfig> configs = new HashMap<>();

    /** Will be executed every frame. */
    public abstract void update();

    /** Draws this instance on the batch. */
    public void draw(Painter painter) {
        final String path = getTexturePath();
        if (!configs.containsKey(path)) {
            configs.put(path, new PainterConfig(path));
        }
        painter.draw(this, configs.get(path));
    }

    /**
     * @return the exact position in the dungeon of this instance
     */
    public abstract Point getPosition();

    /**
     * @return the (current) Texture-Path of the object
     */
    public abstract String getTexturePath();
}
