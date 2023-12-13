package core.gui.layouts.hints;

import core.gui.ILayoutHint;

public class FillLayoutHint implements ILayoutHint {

    private float weight;

    public FillLayoutHint(float weight) {
        this.weight = weight;
    }

    public float weight() {
        return this.weight;
    }

    public void weight(float weight) {
        this.weight = weight;
    }
}
