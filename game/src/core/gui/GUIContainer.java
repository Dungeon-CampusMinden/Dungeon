package core.gui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GUIContainer extends GUIElement {

    protected final List<GUIElement> elements = new ArrayList<>();

    /** Create a new GUIContainer */
    public GUIContainer() {
        super();
    }

    @Override
    public void pack() {
        // Pack elements based in layout and its own size.
        // TODO
    }

    @Override
    public void event(GUIEvent event) {
        // Handle events
        this.elements.forEach(element -> element.event(event));
    }

    /**
     * Create a new GUIContainer with the given elements
     *
     * @param elements GUIElements to add
     */
    public GUIContainer(List<GUIElement> elements) {
        super();
        elements.forEach(
                e -> {
                    e.parent = this;
                    this.elements.add(e);
                });
    }

    /**
     * Create a new GUIContainer with the given elements
     *
     * @param elements GUIElements to add
     */
    public GUIContainer(GUIElement... elements) {
        super();
        Arrays.stream(elements)
                .forEach(
                        e -> {
                            e.parent = this;
                            this.elements.add(e);
                        });
    }

    /**
     * Add a GUIElement to the container
     *
     * @param element GUIElement to add
     * @return GUIContainer (self)
     */
    public GUIContainer add(GUIElement element) {
        element.parent = this;
        this.elements.add(element);
        return this;
    }

    /**
     * Remove a GUIElement from the container
     *
     * @param element GUIElement to remove
     * @return GUIContainer (self)
     */
    public GUIContainer remove(GUIElement element) {
        this.elements.remove(element);
        return this;
    }
}
