package graphic.hud.widgets;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.utils.Null;
import tools.Constants;
import tools.Point;

/** This class is intended for the configuration of the button to be displayed. */
public class ScreenButton extends TextButton {
    private static final TextButtonStyle DEFAULT_BUTTON_STYLE;
    private static final Color DEFAULT_BACKGROUND_COLOR_UP = Color.DARK_GRAY;
    private static final Color DEFAULT_BACKGROUND_COLOR_DOWN = Color.LIGHT_GRAY;
    private static final Color DEFAULT_BORDER_COLOR = Color.BLACK;
    private static final int DEFAULT_BORDER_THICKNESS = 1;

    static {
        DEFAULT_BUTTON_STYLE =
                new TextButtonStyleBuilder(FontBuilder.DEFAULT_FONT)
                        .setFontColor(Color.WHITE)
                        .build();
    }

    /**
     * Creates a ScreenButton which can be used with the ScreenController.
     *
     * @param text the text for the ScreenButton
     * @param position the Position where the ScreenButton should be placed 0,0 is bottom left
     * @param listener the TextButtonListener which handles the button press
     * @param style the TextButtonStyle to use
     */
    public ScreenButton(String text, Point position, TextButtonListener listener, TextButtonStyle style) {
        super(text, style);
        setUpBackground(DEFAULT_BACKGROUND_COLOR_UP, DEFAULT_BORDER_COLOR, DEFAULT_BORDER_THICKNESS);
        setDownBackground(DEFAULT_BACKGROUND_COLOR_DOWN, DEFAULT_BORDER_COLOR, DEFAULT_BORDER_THICKNESS);

        this.setPosition(position.x, position.y);
        if (listener != null) {
            this.addListener(listener);
        }
        this.setScale(1 / Constants.DEFAULT_ZOOM_FACTOR);
    }

    /**
     * Creates a ScreenButton which can be used with the ScreenController.
     *
     * <p>Uses the DEFAULT_BUTTON_STYLE
     *
     * @param text the text for the ScreenButton
     * @param position the Position where the ScreenButton should be placed 0,0 is bottom left
     * @param listener the TextButtonListener which handles the button press
     */
    public ScreenButton(String text, Point position, TextButtonListener listener) {
        this(text, position, listener, DEFAULT_BUTTON_STYLE);
    }

    public void setUpBackground(@Null Color backgroundColor, @Null Color borderColor, int borderThickness) {
        getStyle().up =
            backgroundColor != null
                ? createDrawableBackground(backgroundColor, borderColor,  borderThickness, getWidth(), getHeight())
                : null;
    }

    public void setDownBackground(@Null Color backgroundColor, @Null Color borderColor, int borderThickness) {
        getStyle().down =
            backgroundColor != null
                ? createDrawableBackground(backgroundColor, borderColor,  borderThickness, getWidth(), getHeight())
                : null;
    }

    private Drawable createDrawableBackground(Color backgroundColor, Color borderColor, int borderThickness, float width, float height) {
        Pixmap pixmap = new Pixmap((int)(width + borderThickness*2), (int)(height + borderThickness*2), Pixmap.Format.RGBA8888);

        // Draw background
        pixmap.setColor(backgroundColor);
        pixmap.fill();

        // Draw the border
        pixmap.setColor(borderColor);
        pixmap.fillRectangle(0, 0, pixmap.getWidth(), borderThickness); // Top
        pixmap.fillRectangle(0, 0, borderThickness, pixmap.getHeight()); // Left
        pixmap.fillRectangle(0, pixmap.getHeight()-borderThickness, pixmap.getWidth(), borderThickness); // Bottom
        pixmap.fillRectangle(pixmap.getWidth()-borderThickness, 0, borderThickness, pixmap.getHeight()); // Right

        NinePatch ninePatch = new NinePatch(
            new TextureRegion(new Texture(pixmap)),
            borderThickness,
            borderThickness,
            borderThickness,
            borderThickness
        );
        return new NinePatchDrawable(ninePatch);
    }
}
