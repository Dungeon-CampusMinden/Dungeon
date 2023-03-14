package graphic.hud;

import static java.util.Objects.requireNonNull;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter;

public class FontBuilder {
    public static final BitmapFont DEFAULT_FONT = new BitmapFont();
    private final FreeTypeFontParameter parameters;
    private final String fontPath;

    /**
     * Starts the building progress for a font.
     *
     * @param fontpath an internal path to the font file
     */
    public FontBuilder(String fontpath) {
        requireNonNull(fontpath);
        parameters = new FreeTypeFontParameter();
        this.fontPath = fontpath;
    }

    /**
     * Set the size of the font.
     *
     * @param size of the font
     * @return the {@link FontBuilder} to chain calls.
     */
    public FontBuilder setSize(int size) {
        parameters.size = size;
        return this;
    }

    /**
     * Set the font color.
     *
     * @param color the {@link Color} for the font
     * @return the {@link FontBuilder} to chain calls.
     */
    public FontBuilder setFontColor(Color color) {
        requireNonNull(color);
        parameters.color = color;
        return this;
    }

    /**
     * Adds a border to the font.
     *
     * @param color the {@link Color} for the border
     * @param width the width of the border
     * @return the {@link FontBuilder} to chain calls.
     */
    public FontBuilder addBorder(Color color, int width) {
        requireNonNull(color);
        parameters.borderWidth = width;
        parameters.borderColor = color;
        return this;
    }

    /**
     * Creates the {@link BitmapFont} with the previous configuration.
     *
     * @return the {@link BitmapFont} for UI Elements.
     */
    public BitmapFont build() {
        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal(fontPath));
        BitmapFont font = generator.generateFont(parameters);
        generator.dispose();
        return font;
    }
}
