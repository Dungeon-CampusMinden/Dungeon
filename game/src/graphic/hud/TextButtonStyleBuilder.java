package graphic.hud;

import static java.util.Objects.requireNonNull;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.SpriteDrawable;

public class TextButtonStyleBuilder {
    private final BitmapFont font;
    private Drawable up;
    private Drawable down;
    private Drawable checked;
    private Color overFontColor;
    private Color downFontColor;
    private Color fontColor;

    /**
     * Starts the building progress for a {@link TextButtonStyle}.
     *
     * @param font a preconfigured {@link BitmapFont}
     */
    public TextButtonStyleBuilder(BitmapFont font) {
        requireNonNull(font);
        this.font = font;
    }

    /**
     * Set a backgroundimage for when the {@link ScreenButton} is not pressed.
     *
     * @param path the internal path to the image
     * @return the {@link TextButtonStyleBuilder} to chain calls.
     */
    public TextButtonStyleBuilder setUpImage(String path) {
        requireNonNull(path);
        this.up = createDrawable(path);
        return this;
    }

    /**
     * Set a backgroundimage for when the {@link ScreenButton} is pressed
     *
     * @param path the internal path to the image
     * @return the {@link TextButtonStyleBuilder} to chain calls.
     */
    public TextButtonStyleBuilder setDownImage(String path) {
        requireNonNull(path);
        this.down = createDrawable(path);
        return this;
    }

    /**
     * Set a backgroundimage for when the {@link ScreenButton} is selected
     *
     * @param path the internal path to the image
     * @return the {@link TextButtonStyleBuilder} to chain calls.
     */
    public TextButtonStyleBuilder setCheckedImage(String path) {
        requireNonNull(path);
        this.checked = createDrawable(path);
        return this;
    }
    /**
     * Set a default color for the font
     *
     * @param fontColor the internal path to the image
     * @return the {@link TextButtonStyleBuilder} to chain calls.
     */
    public TextButtonStyleBuilder setFontColor(Color fontColor) {
        requireNonNull(fontColor);
        this.fontColor = fontColor;
        return this;
    }
    /**
     * Set a color for the font when the {@link ScreenButton} is pressed
     *
     * @param downFontColor the Color for the font
     * @return the {@link TextButtonStyleBuilder} to chain calls.
     */
    public TextButtonStyleBuilder setDownFontColor(Color downFontColor) {
        requireNonNull(downFontColor);
        this.downFontColor = downFontColor;
        return this;
    }
    /**
     * Set a color for the font when the {@link ScreenButton} is hovered
     *
     * @param overFontColor the Color for the font
     * @return the {@link TextButtonStyleBuilder} to chain calls.
     */
    public TextButtonStyleBuilder setOverFontColor(Color overFontColor) {
        requireNonNull(overFontColor);
        this.overFontColor = overFontColor;
        return this;
    }

    /**
     * Creates the {@link TextButtonStyle} with the previous configuration.
     *
     * @return the {@link TextButtonStyle} for the {@link ScreenButton}
     */
    public TextButtonStyle build() {
        TextButtonStyle style = new TextButtonStyle(up, down, checked, font);
        if (fontColor != null) style.fontColor = fontColor;
        if (downFontColor != null) style.downFontColor = downFontColor;
        if (overFontColor != null) style.overFontColor = overFontColor;
        return style;
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
