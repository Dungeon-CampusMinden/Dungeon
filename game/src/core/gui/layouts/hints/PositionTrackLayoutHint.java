package core.gui.layouts.hints;

import core.components.PositionComponent;
import core.gui.ILayoutHint;

public class PositionTrackLayoutHint implements ILayoutHint {

    public final PositionComponent positionComponent;

    public PositionTrackLayoutHint(PositionComponent positionComponent) {
        this.positionComponent = positionComponent;
    }
}
