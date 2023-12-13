package core.gui.layouts;

import static core.gui.util.Logging.log;

import core.gui.GUIElement;
import core.gui.IGUILayout;
import core.gui.layouts.hints.BorderLayoutHint;
import core.gui.math.Vector2f;
import core.utils.logging.CustomLogLevel;

import java.util.List;

public class BorderLayout implements IGUILayout {

    private static final int NORTH = BorderLayoutHint.NORTH.ordinal();
    private static final int EAST = BorderLayoutHint.EAST.ordinal();
    private static final int SOUTH = BorderLayoutHint.SOUTH.ordinal();
    private static final int WEST = BorderLayoutHint.WEST.ordinal();
    private static final int CENTER = BorderLayoutHint.CENTER.ordinal();

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
        Vector2f[] positions = new Vector2f[5];

        if (this.mode == BorderLayoutMode.HORIZONTAL) {
            if (array[CENTER] == null) {
                if (array[NORTH] != null) {
                    sizes[NORTH] = new Vector2f(parent.size().x(), parent.size().y() / 4);
                    positions[NORTH] = new Vector2f(0, parent.size().y() - sizes[NORTH].y());
                }
                if (array[SOUTH] != null) {
                    sizes[SOUTH] = new Vector2f(parent.size().x(), parent.size().y() / 4);
                    positions[SOUTH] = new Vector2f(0, 0);
                }
                if (array[EAST] != null) {
                    sizes[EAST] =
                            new Vector2f(
                                    parent.size().x() / 2,
                                    parent.size().y() - sizes[NORTH].y() - sizes[SOUTH].y());
                    positions[EAST] =
                            new Vector2f(parent.size().x() - sizes[EAST].x(), sizes[SOUTH].y());
                }
                if (array[WEST] != null) {
                    sizes[WEST] =
                            new Vector2f(
                                    parent.size().x() / 2,
                                    parent.size().y() - sizes[NORTH].y() - sizes[SOUTH].y());
                    positions[WEST] = new Vector2f(0, sizes[SOUTH].y());
                }
            } else {
                if (array[NORTH] != null) {
                    sizes[NORTH] = new Vector2f(parent.size().x(), sizes[NORTH].y());
                    positions[NORTH] = new Vector2f(0, parent.size().y() - sizes[NORTH].y());
                }
                if (array[SOUTH] != null) {
                    sizes[SOUTH] = new Vector2f(parent.size().x(), sizes[SOUTH].y());
                    positions[SOUTH] = new Vector2f(0, 0);
                }
                if (array[EAST] != null) {
                    sizes[EAST] =
                            new Vector2f(
                                    sizes[EAST].x(),
                                    parent.size().y() - sizes[NORTH].y() - sizes[SOUTH].y());
                    positions[EAST] =
                            new Vector2f(parent.size().x() - sizes[EAST].x(), sizes[SOUTH].y());
                }
                if (array[WEST] != null) {
                    sizes[WEST] =
                            new Vector2f(
                                    sizes[WEST].x(),
                                    parent.size().y() - sizes[NORTH].y() - sizes[SOUTH].y());
                    positions[WEST] = new Vector2f(0, sizes[SOUTH].y());
                }
                sizes[CENTER] =
                        new Vector2f(
                                parent.size().x() - sizes[EAST].x() - sizes[WEST].x(),
                                parent.size().y() - sizes[NORTH].y() - sizes[SOUTH].y());
                positions[CENTER] = new Vector2f(sizes[WEST].x(), sizes[SOUTH].y());
            }
        } else {
            if (array[CENTER] == null) {
                if (array[EAST] != null) {
                    sizes[EAST] = new Vector2f(parent.size().x() / 4, parent.size().y());
                    positions[EAST] = new Vector2f(parent.size().x() - sizes[EAST].x(), 0);
                }
                if (array[WEST] != null) {
                    sizes[WEST] = new Vector2f(parent.size().x() / 4, parent.size().y());
                    positions[WEST] = new Vector2f(0, 0);
                }
                if (array[NORTH] != null) {
                    sizes[NORTH] =
                            new Vector2f(
                                    parent.size().x() - sizes[EAST].x() - sizes[WEST].x(),
                                    parent.size().y() / 2);
                    positions[NORTH] =
                            new Vector2f(sizes[WEST].x(), parent.size().y() - sizes[NORTH].y());
                }
                if (array[SOUTH] != null) {
                    sizes[SOUTH] =
                            new Vector2f(
                                    parent.size().x() - sizes[EAST].x() - sizes[WEST].x(),
                                    parent.size().y() / 2);
                    positions[SOUTH] = new Vector2f(sizes[WEST].x(), 0);
                }
            } else {
                if (array[EAST] != null) {
                    sizes[EAST] = new Vector2f(sizes[EAST].x(), parent.size().y());
                    positions[EAST] = new Vector2f(parent.size().x() - sizes[EAST].x(), 0);
                }
                if (array[WEST] != null) {
                    sizes[WEST] = new Vector2f(sizes[WEST].x(), parent.size().y());
                    positions[WEST] = new Vector2f(0, 0);
                }
                if (array[NORTH] != null) {
                    sizes[NORTH] =
                            new Vector2f(
                                    parent.size().x() - sizes[EAST].x() - sizes[WEST].x(),
                                    sizes[NORTH].y());
                    positions[NORTH] =
                            new Vector2f(sizes[WEST].x(), parent.size().y() - sizes[NORTH].y());
                }
                if (array[SOUTH] != null) {
                    sizes[SOUTH] =
                            new Vector2f(
                                    parent.size().x() - sizes[EAST].x() - sizes[WEST].x(),
                                    sizes[SOUTH].y());
                    positions[SOUTH] = new Vector2f(sizes[WEST].x(), 0);
                }
                sizes[CENTER] =
                        new Vector2f(
                                parent.size().x() - sizes[EAST].x() - sizes[WEST].x(),
                                parent.size().y() - sizes[NORTH].y() - sizes[SOUTH].y());
                positions[CENTER] = new Vector2f(sizes[WEST].x(), sizes[SOUTH].y());
            }
        }

        for (int i = 0; i < array.length; i++) {
            if (array[i] != null) {
                array[i].size(sizes[i]);
                array[i].position(positions[i]);
            }
        }

        elements.forEach(
                e -> {
                    log(
                            CustomLogLevel.DEBUG,
                            "Update size & position of %s to %fx%f at %f;%f",
                            e.getClass().getSimpleName(),
                            e.size().x(),
                            e.size().y(),
                            e.position().x(),
                            e.position().y());
                });
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
            arrangedElements[0] != null ? arrangedElements[0].size() : Vector2f.zero(),
            arrangedElements[1] != null ? arrangedElements[1].size() : Vector2f.zero(),
            arrangedElements[2] != null ? arrangedElements[2].size() : Vector2f.zero(),
            arrangedElements[3] != null ? arrangedElements[3].size() : Vector2f.zero(),
            arrangedElements[4] != null ? arrangedElements[4].size() : Vector2f.zero()
        };
    }
}
