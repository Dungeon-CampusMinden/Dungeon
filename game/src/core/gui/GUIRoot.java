package core.gui;

import static core.gui.util.Logging.log;

import core.gui.events.GUIElementListUpdateEvent;
import core.gui.events.GUIResizeEvent;
import core.gui.layouts.BorderLayout;
import core.gui.layouts.hints.BorderLayoutHint;
import core.gui.util.Font;
import core.utils.logging.CustomLogLevel;
import core.utils.math.Vector2f;

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
    }

    private final IGUIBackend backend;
    private final GUIContainer rootContainer;
    private final List<GUIElement> elementList = new ArrayList<>();
    private boolean updateNextFrame = true;
    private boolean firstFrame = true;

    private GUIRoot(IGUIBackend backend) {
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

    private void setup() {
        try {
            Font[] fonts = Font.loadFont("/fonts/arial.ttf", 24);
            GUIText guiText =
                    new GUIText(
                            "ABCDEFGHIJKLMNOPQRSTUVWXYZÄÖÜß1234567890!?\"§$%&/()=\\-_.,:;#*+~<>@[]{}'²³|`´^°",
                            fonts[0]);
            this.rootContainer.add(guiText, BorderLayoutHint.NORTH);
            this.update();
        } catch (IOException ex) {
            log(CustomLogLevel.ERROR, "Failed to load font", ex);
        }
    }

    public void render(float delta) {
        if (firstFrame) {
            firstFrame = false;
            this.setup();
            this.updateNextFrame = true;
        }
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
