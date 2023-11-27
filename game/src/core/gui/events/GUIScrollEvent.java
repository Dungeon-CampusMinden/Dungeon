package core.gui.events;

import core.gui.GUIEvent;

public class GUIScrollEvent extends GUIEvent {

    public final float scrollX, scrollY;
    public final float mouseX, mouseY;

    public GUIScrollEvent(float scrollX, float scrollY, float mouseX, float mouseY) {
        this.scrollX = scrollX;
        this.scrollY = scrollY;
        this.mouseX = mouseX;
        this.mouseY = mouseY;
    }
}
