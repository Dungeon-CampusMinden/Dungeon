package core.gui;

import core.gui.events.GUIResizeEvent;

public class GUIRoot {

    private IGUIBackend backend;
    private GUIContainer rootContainer;

    public GUIRoot(IGUIBackend backend) {
        this.backend = backend;
        this.rootContainer = new GUIContainer();
    }

    public void render(float delta) {
        this.backend.render(this.rootContainer.elements);
    }

    public void event(GUIEvent event) {
        // Handle events

        if (event instanceof GUIResizeEvent resizeEvent) {
            this.backend.resize(resizeEvent.width(), resizeEvent.height());
            return;
        }

        this.rootContainer.elements.forEach(element -> element.event(event));
    }

    /** Invalidates the current layout and redraws & updates it. */
    public void update() {
        // TODO
    }
}
