package graphic.hud;

import static java.util.Objects.requireNonNull;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.scenes.scene2d.ui.TextField.TextFieldStyle;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.SpriteDrawable;

/** Builder implementation to help create a {@link TextFieldStyle}. */
public class TextFieldStyleBuilder {
    private BitmapFont font;
    private Color fontColor = Color.BLACK;
    private Drawable cursor;
    private Drawable selection;
    private Drawable background;

    /**
     * Starts the building process for a {@link TextFieldStyle}.
     *
     * @param font a preconfigured {@link BitmapFont}
     */
    public TextFieldStyleBuilder(BitmapFont font) {
        requireNonNull(font);
        this.font = font;
    }

    /**
     * Set a color for the font the default is black.
     *
     * @param fontColor the internal path to the image
     * @return the {@link TextFieldStyleBuilder} to chain calls.
     */
    public TextFieldStyleBuilder setFontColor(Color fontColor) {
        requireNonNull(fontColor);
        this.fontColor = fontColor;
        return this;
    }

    /**
     * Set a texture for the {@link ScreenInput}s cursor.
     *
     * @param cursorTexture the internal path to the texture
     * @return the {@link TextFieldStyleBuilder} to chain calls.
     */
    public TextFieldStyleBuilder setCursor(String cursorTexture) {
        requireNonNull(cursorTexture);
        this.cursor = createDrawable(cursorTexture);
        return this;
    }

    /**
     * Set a texture for the {@link ScreenInput}s selection.
     *
     * @param selectionTexture the internal path to the texture
     * @return the {@link TextFieldStyleBuilder} to chain calls.
     */
    public TextFieldStyleBuilder setSelection(String selectionTexture) {
        requireNonNull(selectionTexture);
        this.selection = createDrawable(selectionTexture);
        return this;
    }

    /**
     * Set a texture for the {@link ScreenInput}s background.
     *
     * @param backgroundTexture the internal path to the texture
     * @return the {@link TextFieldStyleBuilder} to chain calls.
     */
    public TextFieldStyleBuilder setBackground(String backgroundTexture) {
        requireNonNull(backgroundTexture);
        this.background = createDrawable(backgroundTexture);
        return this;
    }

    /**
     * Creates the {@link TextFieldStyle} with the previous configuration.
     *
     * @return the {@link TextFieldStyle} for the {@link ScreenInput}
     */
    public TextFieldStyle build() {
        return new TextFieldStyle(font, fontColor, cursor, selection, background);
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
