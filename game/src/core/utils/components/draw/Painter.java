package core.utils.components.draw;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import core.systems.CameraSystem;
import core.utils.Point;

/** Uses LibGDX to draw sprites on the various <code>SpriteBatch</code>es. */
public class Painter {
    private final SpriteBatch batch;

    /** Uses LibGDX to draw sprites on the various <code>SpriteBatch</code>es. */
    public Painter(SpriteBatch batch) {
        this.batch = batch;
    }

    public void draw(Point position, String texturePath, PainterConfig config) {
        if (CameraSystem.isPointInFrustum(position.x, position.y)) {
            Sprite sprite = new Sprite(TextureMap.getInstance().getTexture(texturePath));
            // set up scaling of textures
            sprite.setSize(config.xScaling, config.yScaling);
            // where to draw the sprite
            sprite.setPosition(position.x + config.xOffset, position.y + config.yOffset);

            // need to be called before drawing
            batch.begin();
            // draw sprite
            sprite.draw(batch);
            // need to be called after drawing
            batch.end();
        }
    }
}
