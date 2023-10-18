package core.utils.components.draw;

import com.badlogic.gdx.graphics.Texture;

/** This class serves as a configuration class for the {@link Painter} class. */
public class PainterConfig {
    float xOffset;
    float yOffset;
    float xScaling;
    float yScaling;

    private PainterConfig(float xOffset, float yOffset, float xScaling, float yScaling) {
        this.xOffset = xOffset;
        this.yOffset = yOffset;
        this.xScaling = xScaling;
        this.yScaling = yScaling;
    }

    private PainterConfig(float xOffset, float yOffset, float xScaling, Texture texture) {
        this(
                xOffset,
                yOffset,
                xScaling,
                ((float) texture.getHeight() / (float) texture.getWidth()));
    }

    private PainterConfig(Texture texture) {
        this(0f, 0f, 1, texture);
    }

    /**
     * Paints the given texture at the given position on the given batch with default offset and
     * default scaling.
     *
     * @param texturePath path to the texture
     */
    public PainterConfig(String texturePath) {
        this(TextureMap.instance().textureAt(texturePath));
    }

    /**
     * Paints the given texture at the given position on the given batch with default offset and
     * default scaling.
     *
     * @param texturePath path to the texture
     */
    public PainterConfig(String texturePath, float xOffset, float yOffset) {
        // half the texture xOffset, yOffset is a quarter texture down
        this(xOffset, yOffset, 1, TextureMap.instance().textureAt(texturePath));
    }

    /**
     * Paints the given texture at the given position on the given batch with default offset and a
     * specific given scaling.
     *
     * @param xScaling specific x scaling factor
     * @param yScaling specific y scaling factor
     */
    public PainterConfig(float xScaling, float yScaling) {
        this(-0.85f, -0.5f, xScaling, yScaling);
    }
}
