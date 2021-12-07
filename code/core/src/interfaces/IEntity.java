package interfaces;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import controller.GraphicController;
import graphic.TextureFactory;
import tools.Point;

/** Must be implemented for all objects that should be controlled by the DungeonEntityController */
public interface IEntity {
    /** Will be executed every frame. */
    void update();
    /**
     * @return if this instance can be deleted (than will be removed from DungeonEntityController
     *     list);
     */
    boolean removable();

    SpriteBatch getBatch();
    /** @return the exact position in the dungeon of this instance */
    Point getPosition();
    /** @return the (current)texture of the object. */
    Texture getTexture();

    TextureFactory getFactory();

    /** Each drawable should use this controller to draw itself */
    GraphicController getGraphicController();

    /** Draws this instance on the spritebatch */
    default void draw() {
        getGraphicController().draw(getTexture(), getPosition(), getBatch());
    }
}
