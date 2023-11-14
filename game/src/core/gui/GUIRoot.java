package core.gui;

import core.gui.events.GUIElementListUpdateEvent;
import core.gui.events.GUIResizeEvent;
import core.gui.layouts.BorderLayout;
import core.utils.math.Vector2f;

import java.util.ArrayList;
import java.util.List;

public class GUIRoot {

    private final IGUIBackend backend;
    private final GUIContainer rootContainer;
    private final List<GUIElement> elementList = new ArrayList<>();
    private boolean updateNextFrame = true;

    public GUIRoot(IGUIBackend backend) {
        this.backend = backend;
        this.rootContainer =
                new GUIContainer(new BorderLayout(BorderLayout.BorderLayoutMode.VERTICAL)) {
                    @Override
                    public void event(GUIEvent event) {
                        if (event instanceof GUIElementListUpdateEvent) {
                            GUIRoot.this.update();
                        }
                    }
                };
        this.rootContainer.position = Vector2f.zero();
    }

    public void render(float delta) {
        this.backend.render(this.elementList, updateNextFrame);
        this.updateNextFrame = false;
    }

    public void event(GUIEvent event) {
        // Handle events
        if (event instanceof GUIResizeEvent resizeEvent) {
            this.backend.resize(resizeEvent.width(), resizeEvent.height());
            this.rootContainer.size = new Vector2f(resizeEvent.width(), resizeEvent.height());
            this.update();
            return;
        }
        if (event instanceof GUIElementListUpdateEvent) {
            this.update();
        }
        this.rootContainer.fireEvent(event, GUIEvent.TraverseMode.DOWN);
    }

    /** Invalidates the current layout and redraws & updates it. */
    public void update() {
        this.elementList.clear();
        // Breitendurchlauf durch elemente
        List<GUIContainer> queue = new ArrayList<>();
        queue.add(this.rootContainer);
        while (!queue.isEmpty()) {
            GUIContainer container = queue.remove(0);
            container.layout.layout(container, container.elements);
            container.elements.forEach(
                    element -> {
                        if (element instanceof GUIContainer container1) {
                            queue.add(container1);
                        } else {
                            this.elementList.add(element);
                        }
                        element.update();
                    });
        }
        this.updateNextFrame = true;
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
