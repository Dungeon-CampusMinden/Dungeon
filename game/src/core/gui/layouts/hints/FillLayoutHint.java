package core.gui.layouts.hints;

import core.gui.ILayoutHint;

public class FillLayoutHint implements ILayoutHint {

    public final float weight;

    public FillLayoutHint(float weight) {
        this.weight = Math.min(1, Math.max(0, weight));
    }
}
