package core.gui;

import core.gui.events.GUIMouseClickEvent;
import core.gui.events.GUIMouseMoveEvent;
import core.gui.math.Vector4f;

import java.util.function.BiConsumer;

public class GUIButton extends GUIContainer {

    private BiConsumer<GUIButton, GUIMouseClickEvent> onClick;

    public GUIButton() {
        this.backgroundColor = new Vector4f(0.0f, 0.0f, 0.0f, 1.0f);
    }

    public BiConsumer<GUIButton, GUIMouseClickEvent> onClick() {
        return onClick;
    }

    public GUIButton onClick(BiConsumer<GUIButton, GUIMouseClickEvent> onClick) {
        this.onClick = onClick;
        return this;
    }

    @Override
    public void event(GUIEvent event) {
        super.event(event);

        if (event instanceof GUIMouseMoveEvent mouseMoveEvent) {
            // TODO: Make background color configurable
            if (this.isPointIn(mouseMoveEvent.to)) {
                this.backgroundColor = new Vector4f(0.0f, 0.0f, 1.0f, 1.0f);
            } else {
                this.backgroundColor = new Vector4f(0.0f, 1.0f, 0.0f, 1.0f);
            }
            return;
        }

        if (event instanceof GUIMouseClickEvent mouseClickEvent) {
            if (this.isPointIn(mouseClickEvent.mousePos)) {
                if (this.onClick != null) this.onClick.accept(this, mouseClickEvent);
            }
            return;
        }
    }
}
