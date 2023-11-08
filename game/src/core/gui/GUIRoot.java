package core.gui;

import core.gui.events.GUIResizeEvent;
import core.utils.math.Vector3f;

public class GUIRoot {

    private IGUIBackend backend;
    private GUIContainer rootContainer;

    public GUIRoot(IGUIBackend backend) {
        this.backend = backend;
        this.rootContainer = new GUIContainer();

        GUIElement testElement = new GUIElement() {};
        testElement.position(new Vector3f(60, 60, 0));
        testElement.size(new Vector3f(100, 100, 0));

        this.rootContainer.add(testElement);
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

    /**
     * Get the currently used GUIBackend.
     *
     * @return IGUIBackend
     */
    public IGUIBackend backend() {
        return this.backend;
    }
}
