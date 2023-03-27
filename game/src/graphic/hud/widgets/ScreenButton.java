package graphic.hud.widgets;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import tools.Constants;
import tools.Point;

/** This class is intended for the configuration of the button to be displayed. */
public class ScreenButton extends TextButton {
    private static final TextButtonStyle DEFAULT_BUTTON_STYLE;
    private static final Color DEFAULT_BACKGROUND_COLOR = Color.DARK_GRAY;

    static {
        DEFAULT_BUTTON_STYLE =
                new TextButtonStyleBuilder(FontBuilder.DEFAULT_FONT)
                        .setFontColor(Color.WHITE)
//                        .setBackgroundColor(Color.GRAY)
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
        setBackground(DEFAULT_BACKGROUND_COLOR);

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

    public void setBackground(Color color) {
        Drawable grayBackground = createColoredBackground(color, getWidth(), getHeight());
        getStyle().up = grayBackground;
    }

    private Drawable createColoredBackground(Color color, float width, float height) {
        Pixmap pixmap = new Pixmap((int)width, (int)height, Pixmap.Format.RGBA8888);
        pixmap.setColor(color);
        pixmap.fill();
        NinePatch ninePatch = new NinePatch(new TextureRegion(new Texture(pixmap)), 4, 4, 4, 4);
        return new NinePatchDrawable(ninePatch);
    }
}
