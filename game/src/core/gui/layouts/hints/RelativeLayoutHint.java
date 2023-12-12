package core.gui.layouts.hints;

import core.gui.ILayoutHint;

public class RelativeLayoutHint implements ILayoutHint {

    public final float x, y, width, height;

    /**
     * Create a new layouting hint for the Relative Layout.
     *
     * @param x The x position in percent (0.0 - 1.0) of the parent's width
     * @param y The y position in percent (0.0 - 1.0) of the parent's height
     * @param width The width in percent (0.0 - 1.0) of the parent's width
     * @param height The height in percent (0.0 - 1.0) of the parent's height
     */
    public RelativeLayoutHint(float x, float y, float width, float height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }
}
