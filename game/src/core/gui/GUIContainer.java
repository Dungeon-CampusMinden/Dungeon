package core.gui;

import core.gui.events.GUIElementListUpdateEvent;
import core.gui.layouts.AbsoluteLayout;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GUIContainer extends GUIElement {

    public final List<GUIElement> elements = new ArrayList<>();
    protected IGUILayout layout = new AbsoluteLayout();

    /** Create a new GUIContainer */
    public GUIContainer(IGUILayout layout) {
        super();
        this.layout = layout;
    }

    public GUIContainer() {
        super();
    }

    /**
     * This method is used to fire events. Based on the traverse mode the event should be passed on
     * to the child elements or to the parent element or neither.
     *
     * @param event GUIEvent
     * @param traverseMode TraverseMode
     */
    public void fireEvent(GUIEvent event, GUIEvent.TraverseMode traverseMode) {
        if (traverseMode == GUIEvent.TraverseMode.DOWN) {
            this.elements.forEach(
                    element -> {
                        if (element instanceof GUIContainer container) {
                            container.fireEvent(event, GUIEvent.TraverseMode.DOWN);
                        }
                        element.event(event);
                    });
        } else if (traverseMode == GUIEvent.TraverseMode.UP && this.parent != null) {
            this.parent.fireEvent(event, traverseMode);
        }
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
        if (this.parent != null)
            this.parent.fireEvent(new GUIElementListUpdateEvent(), GUIEvent.TraverseMode.UP);
        return this;
    }

    /**
     * Add a GUIElement to the container
     *
     * @param element GUIElement to add
     * @param layoutHint LayoutHint
     * @return GUIContainer (self)
     */
    public GUIContainer add(GUIElement element, ILayoutHint layoutHint) {
        element.parent = this;
        element.layoutHint = layoutHint;
        this.elements.add(element);
        if (this.parent != null)
            this.parent.fireEvent(new GUIElementListUpdateEvent(), GUIEvent.TraverseMode.UP);
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
        if (this.parent != null)
            this.parent.fireEvent(new GUIElementListUpdateEvent(), GUIEvent.TraverseMode.UP);
        return this;
    }

    public IGUILayout layout() {
        return layout;
    }

    public GUIContainer layout(IGUILayout layout) {
        this.layout = layout;
        return this;
    }
}
