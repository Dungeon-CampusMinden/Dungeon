package core.gui.layouts.hints;

import core.gui.LayoutHint;
import core.gui.layouts.BorderLayout;

public class BorderLayoutHint extends LayoutHint {

    private BorderLayout.BorderLayoutPosition position;

    public BorderLayoutHint(BorderLayout.BorderLayoutPosition position) {
        super();
    }

    public BorderLayout.BorderLayoutPosition position() {
        return this.position;
    }

    public void position(BorderLayout.BorderLayoutPosition position) {
        this.position = position;
    }
}
