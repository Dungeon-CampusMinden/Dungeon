package core.gui;

import core.gui.events.GUIMouseClickEvent;
import core.gui.events.GUIMouseMoveEvent;

import org.lwjgl.glfw.GLFW;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class GUIButton extends GUIContainer {

    private BiConsumer<GUIButton, GUIMouseClickEvent> onClick = (a, b) -> {};
    private Consumer<GUIButton> onHoverEnter = (a) -> {};
    private Consumer<GUIButton> onHoverLeave = (a) -> {};
    protected boolean hovering = false;

    public GUIButton() {}

    public BiConsumer<GUIButton, GUIMouseClickEvent> onClick() {
        return this.onClick;
    }

    public Consumer<GUIButton> onHoverEnter() {
        return this.onHoverEnter;
    }

    public Consumer<GUIButton> onHoverLeave() {
        return this.onHoverLeave;
    }

    public GUIButton onClick(BiConsumer<GUIButton, GUIMouseClickEvent> callback) {
        this.onClick = callback;
        return this;
    }

    public GUIButton onHoverEnter(Consumer<GUIButton> callback) {
        this.onHoverEnter = callback;
        return this;
    }

    public GUIButton onHoverLeave(Consumer<GUIButton> callback) {
        this.onHoverLeave = callback;
        return this;
    }

    @Override
    public void event(GUIEvent event) {
        super.event(event);

        if (event instanceof GUIMouseMoveEvent mouseMoveEvent) {
            boolean isIn = this.isPointIn(mouseMoveEvent.to);
            if (isIn && !this.hovering) {
                this.hovering = true;
                this.onHoverEnter.accept(this);
                return;
            }
            if (!isIn && this.hovering) {
                this.hovering = false;
                this.onHoverLeave.accept(this);
                return;
            }
            return;
        }

        if (event instanceof GUIMouseClickEvent mouseClickEvent) {
            if (mouseClickEvent.action == GLFW.GLFW_PRESS
                    && this.isPointIn(mouseClickEvent.mousePos)) {
                if (this.onClick != null) this.onClick.accept(this, mouseClickEvent);
            }
        }
    }
}
