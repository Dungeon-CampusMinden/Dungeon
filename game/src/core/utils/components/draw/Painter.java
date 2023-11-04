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
        float realX = position.x + config.xOffset; // including the drawOffset
        float realY = position.y + config.yOffset; // including the drawOffset
        if (CameraSystem.isPointInFrustum(realX, realY)) {
            Sprite sprite = new Sprite(TextureMap.instance().textureAt(texturePath));
            // set up scaling of textures
            sprite.setSize(config.xScaling, config.yScaling);
            // where to draw the sprite
            sprite.setPosition(realX, realY);

            // need to be called before drawing
            batch.begin();
            // draw sprite
            sprite.draw(batch);
            // need to be called after drawing
            batch.end();
        }
    }
}
