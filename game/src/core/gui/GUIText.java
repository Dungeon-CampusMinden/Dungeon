package core.gui;

import core.gui.util.Font;
import core.utils.math.Vector2f;
import core.utils.math.Vector4f;

public class GUIText extends GUIElement {

    private Font font;
    private String text;
    private Vector4f textColor = new Vector4f(1, 1, 1, 1);

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

    /**
     * Sets the text color of this text.
     *
     * @param textColor The text color of this text.
     * @return This text.
     */
    public GUIText textColor(Vector4f textColor) {
        this.textColor = textColor;
        return this;
    }

    public GUIText textColor(int rgba) {
        this.textColor = Vector4f.fromRGBA(rgba);
        return this;
    }

    /**
     * Returns the text color of this text.
     *
     * @return The text color of this text.
     */
    public Vector4f textColor() {
        return textColor;
    }

    @Override
    public Vector2f preferredSize() {
        return new Vector2f(500, 200);
    }

    @Override
    public Vector2f minimalSize() {
        return new Vector2f(100, 100);
    }
}
