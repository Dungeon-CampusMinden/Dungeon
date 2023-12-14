package core.gui;

import core.gui.backend.BackendImage;
import core.gui.events.GUIMouseMoveEvent;

public class GUITexturedButton extends GUIButton {

    private BackendImage imageIdle;
    private BackendImage imageHover;
    private BackendImage imageClicked;

    public GUITexturedButton(BackendImage idle, BackendImage hover, BackendImage active) {
        this.imageIdle = idle;
        this.imageHover = hover;
        this.imageClicked = active;
        this.backgroundImage(idle);
    }

    public GUITexturedButton(BackendImage idle, BackendImage hover) {
        this(idle, hover, idle);
    }

    public GUITexturedButton(BackendImage idle) {
        this(idle, idle, idle);
    }

    public BackendImage imageIdle() {
        return imageIdle;
    }

    public GUITexturedButton imageIdle(BackendImage imageIdle) {
        this.imageIdle = imageIdle;
        return this;
    }

    public BackendImage imageHover() {
        return imageHover;
    }

    public GUITexturedButton imageHover(BackendImage imageHover) {
        this.imageHover = imageHover;
        return this;
    }

    public BackendImage imageClicked() {
        return imageClicked;
    }

    public GUITexturedButton imageClicked(BackendImage imageClicked) {
        this.imageClicked = imageClicked;
        return this;
    }

    @Override
    public void event(GUIEvent event) {
        if (event instanceof GUIMouseMoveEvent mouseMoveEvent) {
            boolean isIn = this.isPointIn(mouseMoveEvent.to);
            if (isIn && !this.hovering) {
                if (this.imageHover != null) {
                    this.backgroundImage(this.imageHover);
                    System.out.println("Set image to hover");
                }
            } else if (!isIn && this.hovering) {
                if (this.imageIdle != null) {
                    this.backgroundImage(this.imageIdle);
                    System.out.println("Set image to idle");
                }
            }
        }
        super.event(event);
    }
}
