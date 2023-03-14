package graphic.hud;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import tools.Constants;
import tools.Point;

/** This class is intended for the configuration of the image to be displayed. */
public class ScreenImage extends Image {

    /**
     * Creates an Image for the UI
     *
     * @param texturePath the Path to the Texture
     * @param position the Position where the Image should be drawn
     */
    public ScreenImage(String texturePath, Point position) {
        super(new Texture(texturePath));
        this.setPosition(position.x, position.y);
        this.setScale(1 / Constants.DEFAULT_ZOOM_FACTOR);
    }
}
