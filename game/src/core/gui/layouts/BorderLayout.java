package core.gui.layouts;

import core.gui.GUIElement;
import core.gui.IGUILayout;
import core.gui.layouts.hints.BorderLayoutHint;
import core.utils.math.Vector2f;

import java.util.List;

public class BorderLayout implements IGUILayout {

    // Reihenfolge: N, E, S, W, C

    /**
     * The mode of the border layout.
     *
     * <p>The mode defines how the elements are laid out.
     * <li>HORIZONTAL: The north and south elements will get the full width of the parent element
     *     and the east and west elements will get the remaining height.
     * <li>VERTICAL: The east and west elements will get the full height of the parent element and
     *     the north and south elements will get the remaining width.
     */
    public enum BorderLayoutMode {
        HORIZONTAL,
        VERTICAL
    }

    private BorderLayoutMode mode;

    /**
     * Create a new BorderLayout with the given mode.
     *
     * @param mode The mode of the border layout.
     */
    public BorderLayout(BorderLayoutMode mode) {
        this.mode = mode;
    }

    @Override
    public void layout(GUIElement parent, List<GUIElement> elements) {
        if (elements.size() > 5) {
            throw new IllegalArgumentException(
                    "BorderLayout only handles up to 5 direct child elements!");
        }

        GUIElement[] array = getArrangedElementes(elements);
        Vector2f[] sizes = getSizes(array);

        // NORTH
        if (array[0] != null) {
            GUIElement element = array[0];
            Vector2f size = sizes[0]; // Get preferred size of north element
            if (this.mode == BorderLayoutMode.HORIZONTAL) { // North gets full width
                element.size().x(parent.size().x()).y(size.y());
                element.position().x(0).y(parent.size().y() - size.y()); // Stick to top left edge
            } else if (this.mode == BorderLayoutMode.VERTICAL) { // North gets remaining width
                element.size().x(parent.size().x() - sizes[1].x() - sizes[3].x()).y(size.y());
                element.position()
                        .x(sizes[3].x())
                        .y(parent.size().y() - size.y()); // Stick to bottom left edge
            }
            if (element.minimalSize().y() > element.size().y()) {
                element.size().y(element.minimalSize().y());
            }
            if (element.minimalSize().x() > element.size().x()) {
                element.size().x(element.minimalSize().x());
            }
        }

        // SOUTH
        if (array[2] != null) {
            GUIElement element = array[2];
            Vector2f size = sizes[2]; // Get preferred size of south element
            if (this.mode == BorderLayoutMode.HORIZONTAL) { // South gets full width
                element.size().x(parent.size().x()).y(size.y());
                element.position().x(0).y(0); // Stick to bottom left edge
            } else if (this.mode == BorderLayoutMode.VERTICAL) { // South gets remaining width
                element.size().x(parent.size().x() - sizes[1].x() - sizes[3].x()).y(size.y());
                element.position().x(sizes[3].x()).y(0); // Stick to top left edge
            }
            if (element.minimalSize().y() > element.size().y()) {
                element.size().y(element.minimalSize().y());
            }
            if (element.minimalSize().x() > element.size().x()) {
                element.size().x(element.minimalSize().x());
            }
        }

        // EAST
        if (array[1] != null) {
            GUIElement element = array[1];
            Vector2f size = sizes[1]; // Get preferred size of east element
            if (this.mode == BorderLayoutMode.HORIZONTAL) { // East gets remaining height
                element.size().x(size.x()).y(parent.size().y() - sizes[0].y() - sizes[2].y());
                element.position()
                        .x(parent.size().x() - size.x())
                        .y(sizes[2].y()); // Stick to bottom right edge
            } else if (this.mode == BorderLayoutMode.VERTICAL) { // East gets full height
                element.size().x(size.x()).y(parent.size().y());
                element.position().x(parent.size().x() - size.x()).y(0); // Stick to top right edge
            }
            if (element.minimalSize().y() > element.size().y()) {
                element.size().y(element.minimalSize().y());
            }
            if (element.minimalSize().x() > element.size().x()) {
                element.size().x(element.minimalSize().x());
            }
        }

        // WEST
        if (array[3] != null) {
            GUIElement element = array[3];
            Vector2f size = sizes[3]; // Get preferred size of west element
            if (this.mode == BorderLayoutMode.HORIZONTAL) { // West gets remaining height
                element.size().x(size.x()).y(parent.size().y() - sizes[0].y() - sizes[2].y());
                element.position().x(0).y(sizes[2].y()); // Stick to bottom left edge
            } else if (this.mode == BorderLayoutMode.VERTICAL) { // West gets full height
                element.size().x(size.x()).y(parent.size().y());
                element.position().x(0).y(0); // Stick to top left edge
            }
            if (element.minimalSize().y() > element.size().y()) {
                element.size().y(element.minimalSize().y());
            }
            if (element.minimalSize().x() > element.size().x()) {
                element.size().x(element.minimalSize().x());
            }
        }

        // CENTER
        if (array[4] != null) {
            GUIElement element = array[4];
            Vector2f size = sizes[4]; // Get preferred size of center element
            element.size()
                    .x(parent.size().x() - sizes[1].x() - sizes[3].x())
                    .y(parent.size().y() - sizes[0].y() - sizes[2].y());
            element.position().x(sizes[3].x()).y(sizes[2].y()); // Stick to bottom left edge

            if (element.minimalSize().y() > element.size().y()) {
                element.size().y(element.minimalSize().y());
            }
            if (element.minimalSize().x() > element.size().x()) {
                element.size().x(element.minimalSize().x());
            }
        }
    }

    @Override
    public Vector2f calcMinSize(GUIElement parent, List<GUIElement> elements) {
        GUIElement[] array = getArrangedElementes(elements);
        Vector2f[] sizes = getSizes(array);

        float width = 0;
        float height = 0;
        height += sizes[0].y(); // North
        height += sizes[2].y(); // South
        height += sizes[4].y(); // Center
        width += sizes[1].x(); // East
        width += sizes[3].x(); // West
        width += sizes[4].x(); // Center

        return new Vector2f(width, height);
    }

    private GUIElement[] getArrangedElementes(List<GUIElement> elements) {
        GUIElement[] array = new GUIElement[5];
        elements.forEach(
                e -> {
                    if (e.layoutHint() != null && e.layoutHint() instanceof BorderLayoutHint hint) {
                        array[hint.ordinal()] = e;
                    }
                });
        elements.forEach(
                e -> {
                    if (e.layoutHint() == null
                            || !(e.layoutHint() instanceof BorderLayoutHint hint)) {
                        for (int i = 0; i < array.length; i++) {
                            if (array[i] == null) {
                                array[i] = e;
                                break;
                            }
                        }
                    }
                });
        return array;
    }

    private Vector2f[] getSizes(GUIElement[] arrangedElements) {
        return new Vector2f[] {
            arrangedElements[0] != null ? arrangedElements[0].minimalSize() : Vector2f.zero(),
            arrangedElements[1] != null ? arrangedElements[1].minimalSize() : Vector2f.zero(),
            arrangedElements[2] != null ? arrangedElements[2].minimalSize() : Vector2f.zero(),
            arrangedElements[3] != null ? arrangedElements[3].minimalSize() : Vector2f.zero(),
            arrangedElements[4] != null ? arrangedElements[4].minimalSize() : Vector2f.zero()
        };
    }
}
