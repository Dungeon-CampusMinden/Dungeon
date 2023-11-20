package core.gui;

import core.gui.util.Font;
import core.utils.math.Vector2f;

public class GUIText extends GUIElement {

    private Font font;
    private String text;

    public GUIText(String text, Font font) {
        this.text = text;
        this.font = font;
    }

    public Font font() {
        return font;
    }

    public GUIText font(Font font) {
        this.font = font;
        return this;
    }

    public String text() {
        return text;
    }

    public GUIText text(String text) {
        this.text = text;
        return this;
    }

    @Override
    public Vector2f preferredSize() {
        // TODO: Implement
        return new Vector2f(500, 200);
    }
}
