package core.hud;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.ui.Image;

import core.utils.Constants;
import core.utils.Point;

/** This class is intended for the configuration of the image to be displayed. */
public class ScreenImage extends Image {

    /**
     * Creates an Image for the UI
     *
     * @param texturePath the Path to the Texture
     * @param position the Position where the Image should be drawn
     * @param scale Determination of the scale
     */
    public ScreenImage(String texturePath, Point position, float scale) {
        super(new Texture(texturePath));
        this.setPosition(position.x, position.y);

        if (scale > 0) this.setScale(scale);
        else this.setScale(1 / Constants.DEFAULT_ZOOM_FACTOR);
    }
}
