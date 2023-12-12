package core.gui;

import core.Assets;
import core.gui.events.GUIElementListUpdateEvent;
import core.gui.events.GUIResizeEvent;
import core.gui.layouts.RelativeLayout;
import core.gui.math.Vector2f;
import core.gui.util.Font;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class GUIRoot {

    private static GUIRoot instance;

    public static GUIRoot getInstance() {
        return instance;
    }

    public static Optional<GUIRoot> getInstanceOptional() {
        return Optional.ofNullable(instance);
    }

    public static void init(IGUIBackend backend) {
        instance = new GUIRoot(backend);
        try {
            Font.DEFAULT_FONT = Font.loadFont(Assets.Fonts.OPENSANS_REGULAR, 24)[0];
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    private final IGUIBackend backend;
    private GUIContainer rootContainer;
    private final List<GUIElement> elementList = new ArrayList<>();
    private boolean updateNextFrame = true;
    private boolean firstFrame = true;

    private GUIRoot(IGUIBackend backend) {
        this.backend = backend;
        this.rootContainer = new GUIContainer(new RelativeLayout());
        this.rootContainer.position = Vector2f.zero();
        GUIRootListener.init(this);
    }

    public void render(float delta) {
        if (firstFrame) {
            firstFrame = false;
            this.update();
            this.updateNextFrame = true;
        }
        this.backend.render(this.elementList, this.updateNextFrame);
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
        this.updateElementList(this.elementList, this.rootContainer);
        this.updateNextFrame = true;
    }

    private void updateElementList(List<GUIElement> list, GUIContainer container) {
        list.clear();
        List<GUIContainer> queue = new ArrayList<>();
        queue.add(container);
        container.layout().layout(container, container.elements);
        while (!queue.isEmpty()) {
            GUIContainer container1 = queue.remove(0);
            container1.elements.forEach(
                    element -> {
                        if (element instanceof GUIContainer container2) {
                            queue.add(container2);
                            container2.layout().layout(container2, container2.elements);
                        }
                        if (!list.contains(element)) {
                            list.add(element);
                        }
                    });
        }
    }

    public GUIContainer rootContainer() {
        return this.rootContainer;
    }

    public GUIRoot rootContainer(GUIContainer rootContainer) {
        this.rootContainer = rootContainer;
        return this;
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
