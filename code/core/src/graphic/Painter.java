package graphic;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import textures.TextureMap;
import tools.Point;

/** Uses LibGDX to draw sprites on the various <code>SpriteBatch</code>es. */
public class Painter {
    private final DungeonCamera camera;
    private final TextureMap textureMap = new TextureMap();

    /**
     * Uses LibGDX to draw sprites on the various <code>SpriteBatch</code>es.
     *
     * @param camera only objects that are in the camera are drawn
     */
    public Painter(DungeonCamera camera) {
        this.camera = camera;
    }

    /** Draws the instance based on its position. */
    public void draw(
            float xOffset,
            float yOffset,
            float xScaling,
            float yScaling,
            String texturePath,
            Point position,
            SpriteBatch batch) {
        if (camera.isPointInFrustum(position.x, position.y)) {
            Sprite sprite = new Sprite(textureMap.getTexture(texturePath));
            // set up scaling of textures
            sprite.setSize(xScaling, yScaling);
            // where to draw the sprite
            sprite.setPosition(position.x + xOffset, position.y + yOffset);

            // need to be called before drawing
            batch.begin();
            // draw sprite
            sprite.draw(batch);
            // need to be called after drawing
            batch.end();
        }
    }

    /** Draws the instance based on its position with default offset and default scaling. */
    public void draw(String texturePath, Point position, SpriteBatch batch) {
        // the concrete offset values are best guesses
        Texture t = textureMap.getTexture(texturePath);

        draw(
                -0.85f,
                -0.5f,
                1,
                ((float) t.getHeight() / (float) t.getWidth()),
                texturePath,
                position,
                batch);
    }

    /** Draws the instance based on its position with default scaling and specific offset. */
    public void draw(
            float xOffset, float yOffset, String texturePath, Point position, SpriteBatch batch) {
        Texture t = textureMap.getTexture(texturePath);
        draw(
                xOffset,
                yOffset,
                1,
                ((float) t.getHeight() / (float) t.getWidth()),
                texturePath,
                position,
                batch);
    }

    /** Draws the instance based on its position with default offset and specific scaling. */
    public void drawWithScaling(
            float xScaling, float yScaling, String texturePath, Point position, SpriteBatch batch) {
        draw(-0.85f, -0.5f, xScaling, yScaling, texturePath, position, batch);
    }
}
