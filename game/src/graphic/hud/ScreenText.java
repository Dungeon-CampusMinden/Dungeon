package graphic.hud;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import tools.Point;

/** This class is intended for the configuration of the text to be displayed. */
public class ScreenText extends Label {
    /** Allows the dynamic configuration of the default style for the generated ScreenTexts */
    public static final LabelStyle DEFAULT_LABEL_STYLE;

    static {
        DEFAULT_LABEL_STYLE =
                new LabelStyleBuilder(FontBuilder.DEFAULT_FONT).setFontcolor(Color.BLUE).build();
    }

    /**
     * Creates a Text with the default label style at the given position.
     *
     * @param text the text which should be written
     * @param position the Point where the ScreenText should be written 0,0 bottom left
     * @param scaleXY the scale for the ScreenText
     * @param style the style
     */
    public ScreenText(String text, Point position, float scaleXY, LabelStyle style) {
        super(text, style);
        this.setPosition(position.x, position.y);
        this.setScale(scaleXY);
    }

    /**
     * Creates the ScreenText with the default style.
     *
     * @param text the text which should be written
     * @param position the position for the ScreenText 0,0 bottom left
     * @param scaleXY the scale for the ScreenText
     */
    public ScreenText(String text, Point position, float scaleXY) {
        this(text, position, scaleXY, DEFAULT_LABEL_STYLE);
    }
}
