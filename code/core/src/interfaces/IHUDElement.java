package interfaces;

import com.badlogic.gdx.graphics.Texture;
import graphic.TextureFactory;
import tools.Point;

/** Should be implemented by all HUD objects. */
public interface IHUDElement {

    /**
     * The position of HUD elements are based on virtual coordinates.
     *
     * @return the position
     */
    Point getPosition();

    Texture getTexture();

    TextureFactory getFactory();

    default float getWidth() {
        return 0.5f;
    }

    default float getHeight() {
        return getTexture().getHeight() / 2f;
    }
}
