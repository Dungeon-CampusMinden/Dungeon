package graphic.hud;

import static java.util.Objects.requireNonNull;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.SpriteDrawable;

public class LabelStyleBuilder {
    private final BitmapFont font;
    private Color fontColor;
    private Drawable background;

    /**
     * Starts the building progress for a {@link LabelStyle}.
     *
     * @param font a preconfigured {@link BitmapFont}
     */
    public LabelStyleBuilder(BitmapFont font) {
        requireNonNull(font);
        this.font = font;
    }

    /**
     * Set a background image.
     *
     * @param path the internal path to the image
     * @return the {@link LabelStyleBuilder} to chain calls.
     */
    public LabelStyleBuilder setBackground(String path) {
        requireNonNull(path);
        this.background = createDrawable(path);
        return this;
    }

    /**
     * Set a color for the font.
     *
     * @param fontColor the internal path to the image
     * @return the {@link LabelStyleBuilder} to chain calls.
     */
    public LabelStyleBuilder setFontcolor(Color fontColor) {
        requireNonNull(fontColor);
        this.fontColor = fontColor;
        return this;
    }

    /**
     * Creates the {@link LabelStyle} with the previous configuration.
     *
     * @return the {@link LabelStyle} for the {@link ScreenText}
     */
    public LabelStyle build() {
        LabelStyle labelStyle = new LabelStyle(font, fontColor);
        if (background != null) labelStyle.background = background;
        return labelStyle;
    }

    /**
     * Small little helper which loads the images.
     *
     * @param path the internal path to the image
     * @return a SpriteDrawable from the given path
     */
    private Drawable createDrawable(String path) {
        return new SpriteDrawable(new Sprite(new Texture(Gdx.files.internal(path))));
    }
}
