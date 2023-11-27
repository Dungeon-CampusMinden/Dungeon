package core.gui.events;

import core.gui.GUIEvent;
import core.utils.math.Vector2i;

public class GUIMouseMoveEvent extends GUIEvent {

    public final Vector2i from, to;

    public GUIMouseMoveEvent(Vector2i from, Vector2i to) {
        this.from = from;
        this.to = to;
    }
}
