package basiselements;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import tools.Point;

public abstract class DungeonElement {
    private SpriteBatch batch;

    /**
     * An object in the dungeon that can be drawn
     *
     * @param batch SpriteBatch to draw on
     */
    public DungeonElement(SpriteBatch batch) {
        this.batch = batch;
    }

    /** Will be executed every frame. */
    public void update() {}

    /** @return <code>true</code>, if this instance can be deleted; <code>false</code> otherwise */
    public boolean removable() {
        return false;
    }

    public SpriteBatch getBatch() {
        return batch;
    }

    /** @return the exact position in the dungeon of this instance */
    public abstract Point getPosition();

    /** @return the (current) Texture-Path of the object */
    public abstract String getTexturePath();
}
