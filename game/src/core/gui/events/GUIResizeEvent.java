package core.gui.events;

import core.gui.GUIEvent;

public class GUIResizeEvent extends GUIEvent implements Cancelable {

    private int width;
    private int height;
    private boolean canceled;

    public GUIResizeEvent(int width, int height) {
        super();
        this.width = width;
        this.height = height;
    }

    public int width() {
        return this.width;
    }

    public int height() {
        return this.height;
    }

    @Override
    public boolean isCanceled() {
        return this.canceled;
    }

    @Override
    public void setCanceled(boolean canceled) {
        this.canceled = canceled;
    }
}
