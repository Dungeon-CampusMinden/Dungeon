package core.gui.layouts;

import core.gui.GUIElement;
import core.gui.IGUILayout;
import core.gui.layouts.hints.BorderLayoutHint;
import core.utils.math.Vector2f;

import java.util.List;

public class BorderLayout implements IGUILayout {

    // Reihenfolge: N, E, S, W, C

    public enum BorderLayoutPosition {
        NORTH,
        EAST,
        SOUTH,
        WEST,
        CENTER
    }

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

        GUIElement[] array = new GUIElement[5];
        elements.forEach(
                e -> {
                    if (e.layoutHint() != null
                            && e.layoutHint() instanceof BorderLayoutHint hint
                            && hint.position() != null) {
                        array[hint.position().ordinal()] = e;
                    }
                });
        elements.forEach(
                e -> {
                    if (e.layoutHint() == null
                            || !(e.layoutHint() instanceof BorderLayoutHint hint)
                            || hint.position() == null) {
                        for (int i = 0; i < array.length; i++) {
                            if (array[i] == null) {
                                array[i] = e;
                                break;
                            }
                        }
                    }
                });

        Vector2f[] sizes =
                new Vector2f[] {
                    array[0] != null ? array[0].size() : Vector2f.zero(),
                    array[1] != null ? array[1].size() : Vector2f.zero(),
                    array[2] != null ? array[2].size() : Vector2f.zero(),
                    array[3] != null ? array[3].size() : Vector2f.zero(),
                    array[4] != null ? array[4].size() : Vector2f.zero()
                };

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
        }

        // CENTER
        if (array[4] != null) {
            GUIElement element = array[4];
            Vector2f size = sizes[4]; // Get preferred size of center element
            element.size()
                    .x(parent.size().x() - sizes[1].x() - sizes[3].x())
                    .y(parent.size().y() - sizes[0].y() - sizes[2].y());
            element.position().x(sizes[3].x()).y(sizes[2].y()); // Stick to bottom left edge
        }
    }
}
