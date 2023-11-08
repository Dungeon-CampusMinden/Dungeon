package core.gui;

import core.gui.events.GUIResizeEvent;
import core.gui.layouts.BorderLayout;
import core.utils.math.Vector2f;
import core.utils.math.Vector4f;

public class GUIRoot {

    private IGUIBackend backend;
    private GUIContainer rootContainer;

    public GUIRoot(IGUIBackend backend) {
        this.backend = backend;
        this.rootContainer =
                new GUIContainer(new BorderLayout(BorderLayout.BorderLayoutMode.VERTICAL));
        this.rootContainer.position = Vector2f.zero();

        GUIColorPane pane1 = new GUIColorPane(new Vector4f(1, 0, 0, 1.0f));
        GUIColorPane pane2 = new GUIColorPane(new Vector4f(0.99f, 0.6f, 0, 1f));
        GUIColorPane pane3 = new GUIColorPane(new Vector4f(1f, 1f, 0f, 1f));
        GUIColorPane pane4 = new GUIColorPane(new Vector4f(0f, 0.6f, 0f, 1f));

        pane1.size(new Vector2f(100, 100));
        pane2.size(new Vector2f(100, 100));
        pane3.size(new Vector2f(100, 100));
        pane4.size(new Vector2f(100, 100));

        this.rootContainer.add(pane1);
        this.rootContainer.add(pane2);
        this.rootContainer.add(pane3);
        this.rootContainer.add(pane4);

        this.rootContainer.layout.layout(this.rootContainer, this.rootContainer.elements);
    }

    public void render(float delta) {
        this.backend.render(this.rootContainer.elements);
    }

    public void event(GUIEvent event) {
        // Handle events

        if (event instanceof GUIResizeEvent resizeEvent) {
            this.backend.resize(resizeEvent.width(), resizeEvent.height());
            this.rootContainer.size = new Vector2f(resizeEvent.width(), resizeEvent.height());
            this.rootContainer.layout.layout(this.rootContainer, this.rootContainer.elements);
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
