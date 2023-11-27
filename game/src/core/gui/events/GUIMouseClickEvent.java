package core.gui.events;

import core.gui.GUIEvent;
import core.gui.math.Vector2i;

public class GUIMouseClickEvent extends GUIEvent {

    public final int button, action, mods;

    public final Vector2i mousePos;

    public GUIMouseClickEvent(int button, int action, int mods, Vector2i mousePos) {
        this.button = button;
        this.action = action;
        this.mods = mods;
        this.mousePos = mousePos;
    }
}
