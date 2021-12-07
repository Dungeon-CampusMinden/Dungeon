package controller;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import graphic.DungeonCamera;
import tools.Point;

/** Uses libGDX to draw sprites on the various SpriteBatches. */
public class GraphicController {
    private final DungeonCamera camera;

    /**
     * Uses libGDX to draw sprites on the various SpriteBatches.
     *
     * @param camera only objects that are in the camera are drawn
     */
    public GraphicController(DungeonCamera camera) {
        this.camera = camera;
    }

    /** Draws the instance based on its position. */
    public void draw(
            float xOffset,
            float yOffset,
            float xScaling,
            float yScaling,
            Texture texture,
            Point position,
            SpriteBatch batch) {
        if (isPointInFrustum((int) position.x, (int) position.y)) {

            Sprite sprite = new Sprite(texture);
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
    public void draw(Texture texture, Point position, SpriteBatch batch) {
        // the concrete offset values are best guesses
        this.draw(
                -0.85f,
                -0.5f,
                1,
                ((float) texture.getHeight() / (float) texture.getWidth()),
                texture,
                position,
                batch);
    }

    /** Draws the instance based on its position with default scaling and specific offset */
    @SuppressWarnings("unused")
    public void draw(
            float xOffset, float yOffset, Texture texture, Point position, SpriteBatch batch) {
        this.draw(
                xOffset,
                yOffset,
                1,
                ((float) texture.getHeight() / (float) texture.getWidth()),
                texture,
                position,
                batch);
    }

    /** Draws the instance based on its position with default offset and specific scaling. */
    @SuppressWarnings("unused")
    public void drawWithScaling(
            float xScaling, float yScaling, Texture texture, Point position, SpriteBatch batch) {
        draw(-0.85f, -0.5f, xScaling, yScaling, texture, position, batch);
    }

    private boolean isPointInFrustum(int x, int y) {
        int buffer = 2;

        return camera.getFrustum().pointInFrustum(x + buffer, y - buffer, 0)
                || camera.getFrustum().pointInFrustum(x + buffer, y + buffer, 0)
                || camera.getFrustum().pointInFrustum(x - buffer, y - buffer, 0)
                || camera.getFrustum().pointInFrustum(x - buffer, y + buffer, 0);
    }
}
