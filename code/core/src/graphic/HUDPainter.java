package graphic;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import textures.TextureMap;
import tools.Constants;
import tools.Point;

/** Uses LibGDX to draw sprites on the various <code>SpriteBatch</code>es. */
public class HUDPainter {
    private final TextureMap textureMap = new TextureMap();

    /** Draws the instance based on its position. */
    public void draw(String texture, Point position, SpriteBatch batch) {
        Texture texture1 = textureMap.getTexture(texture);
        Sprite sprite = new Sprite(texture1);

        // set up scaling of textures
        sprite.setSize(
                texture1.getWidth() / Constants.DEFAULT_ZOOM_FACTOR,
                texture1.getHeight() / Constants.DEFAULT_ZOOM_FACTOR);

        // where to draw the sprite
        sprite.setPosition(
                position.x,
                Constants.WINDOW_HEIGHT
                        - position.y
                        - texture1.getHeight() / Constants.DEFAULT_ZOOM_FACTOR);

        // need to be called before drawing
        batch.begin();
        // draw sprite
        sprite.draw(batch);
        // need to be called after drawing
        batch.end();
    }

    /** Draws the instance based on its position with default offset and specific scaling. */
    public void drawWithScaling(
            float xScaling, float yScaling, String texture, Point position, SpriteBatch batch) {
        Texture texture1 = textureMap.getTexture(texture);
        Sprite sprite = new Sprite(texture1);

        // set up scaling of textures
        sprite.setSize(texture1.getWidth() * xScaling, texture1.getHeight() * yScaling);

        // where to draw the sprite
        sprite.setPosition(
                position.x, Constants.WINDOW_HEIGHT - position.y - texture1.getHeight() * yScaling);

        // need to be called before drawing
        batch.begin();
        // draw sprite
        sprite.draw(batch);
        // need to be called after drawing
        batch.end();
    }
}
