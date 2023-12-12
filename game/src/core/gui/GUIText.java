package core.gui;

import core.gui.events.GUIScrollEvent;
import core.gui.math.Vector2f;
import core.gui.math.Vector4f;
import core.gui.util.Font;

public class GUIText extends GUIElement {

    private Font font;
    private String text;
    private Vector4f textColor = new Vector4f(1, 1, 1, 1);
    private boolean scrollX = false;
    private boolean scrollY = false;
    private Vector2f scrollOffset = new Vector2f(0, 0);
    private Vector2f textBounds;

    public GUIText(String text, Font font) {
        this.text = text;
        this.font = font;
        this.textBounds = this.font.boundingBox(this.text, this.size.x());
    }

    public GUIText(String text) {
        this(text, Font.DEFAULT_FONT);
    }

    public Font font() {
        return font;
    }

    public GUIText font(Font font) {
        this.font = font;
        this.textBounds = this.font.boundingBox(this.text, this.size.x());
        return this;
    }

    public String text() {
        return text;
    }

    public GUIText text(String text) {
        this.text = text;
        this.textBounds = this.font.boundingBox(this.text, this.size.x());
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

    public boolean scrollX() {
        return scrollX;
    }

    public GUIText scrollX(boolean scrollX) {
        this.scrollX = scrollX;
        return this;
    }

    public boolean scrollY() {
        return scrollY;
    }

    public GUIText scrollY(boolean scrollY) {
        this.scrollY = scrollY;
        return this;
    }

    public Vector2f scrollOffset() {
        return scrollOffset.copy();
    }

    public GUIText scrollOffset(Vector2f scrollOffset) {
        this.scrollOffset.x(scrollOffset.x());
        this.scrollOffset.y(scrollOffset.y());
        this.checkScrollOffset();
        return this;
    }

    @Override
    public void size(Vector2f size) {
        super.size(size);
        this.textBounds = this.font.boundingBox(this.text, this.size.x() - 2 * this.font.fontSize);
    }

    @Override
    public void event(GUIEvent event) {
        super.event(event);
        if (event instanceof GUIScrollEvent scrollEvent) {
            // Check if mouse over this element
            Vector2f absPos = this.absolutePosition();
            if (scrollEvent.mousePos.x() < absPos.x()
                    || scrollEvent.mousePos.y() < absPos.y()
                    || scrollEvent.mousePos.x() > absPos.x() + this.size().x()
                    || scrollEvent.mousePos.y() > absPos.y() + this.size().y()) {
                return;
            }
            if (scrollX) {
                this.scrollOffset.x(this.scrollOffset.x() + scrollEvent.scroll.x() * 20);
                this.checkScrollOffset();
                this.invalidate();
            }
            if (scrollY) {
                this.scrollOffset.y(this.scrollOffset.y() + scrollEvent.scroll.y() * 20);
                this.checkScrollOffset();
                this.invalidate();
            }
        }
    }

    private void checkScrollOffset() {
        if (this.scrollOffset.x() < -(this.textBounds.x() - this.size.x())) {
            this.scrollOffset.x(-(this.textBounds.x() - this.size.x()));
        }
        if (this.scrollOffset.x() > 0) {
            this.scrollOffset.x(0);
        }
        if (this.scrollOffset.y() < -(this.textBounds.y() - this.size.y())) {
            this.scrollOffset.y(-(this.textBounds.y() - this.size.y()));
        }
        if (this.scrollOffset.y() > 0) {
            this.scrollOffset.y(0);
        }
    }
}
