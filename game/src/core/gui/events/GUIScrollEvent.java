package core.gui.events;

import core.gui.GUIEvent;
import core.utils.math.Vector2i;

public class GUIScrollEvent extends GUIEvent {

    public final Vector2i scroll, mousePos;

    public GUIScrollEvent(Vector2i scroll, Vector2i mousePos) {
        this.scroll = scroll;
        this.mousePos = mousePos;
    }
}
