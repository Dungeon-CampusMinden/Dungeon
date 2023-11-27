package core.gui.layouts.hints;

import core.gui.ILayoutHint;
import core.gui.math.Vector2i;

public class AbsolutLayoutHint implements ILayoutHint {

    public final int x;
    public final int y;
    public final int width;
    public final int height;

    public AbsolutLayoutHint(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public AbsolutLayoutHint(Vector2i position, Vector2i size) {
        this(position.x(), position.y(), size.x(), size.y());
    }
}
