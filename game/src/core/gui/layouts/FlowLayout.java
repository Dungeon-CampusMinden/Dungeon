package core.gui.layouts;

import core.gui.GUIElement;
import core.gui.IGUILayout;
import core.gui.math.Vector2f;

import java.util.List;

public class FlowLayout implements IGUILayout {

    public enum FlowDirection {
        ROW,
        COLUMN
    }

    public enum FlowAlignment {
        START,
        CENTER,
        END
    }

    private float gap = 10.0f;
    private FlowDirection direction = FlowDirection.ROW;
    private FlowAlignment alignment = FlowAlignment.START;

    public FlowLayout(FlowDirection direction, FlowAlignment alignment, float gap) {
        this.direction = direction;
        this.alignment = alignment;
        this.gap = gap;
    }

    public FlowLayout(FlowDirection direction, FlowAlignment alignment) {
        this(direction, alignment, 10.0f);
    }

    public FlowLayout(FlowDirection direction) {
        this(direction, FlowAlignment.START);
    }

    public FlowLayout() {
        this(FlowDirection.ROW);
    }

    @Override
    public void layout(GUIElement parent, List<GUIElement> elements) {
        elements.forEach(GUIElement::pack); // Pack elements to their minimal/preferred size.
        Vector2f size = this.calcSize(elements);

        Vector2f pos =
                switch (this.alignment) {
                    case START -> Vector2f.zero();
                    case CENTER -> this.direction == FlowDirection.ROW
                            ? new Vector2f((parent.size().x() - size.x()) / 2.0f, 0.0f)
                            : new Vector2f(0.0f, (parent.size().y() - size.y()) / 2.0f);
                    case END -> this.direction == FlowDirection.ROW
                            ? new Vector2f(parent.size().x() - size.x(), 0.0f)
                            : new Vector2f(0.0f, parent.size().y() - size.y());
                };

        elements.forEach(
                (element) -> {
                    element.position(pos.copy());
                    if (this.direction == FlowDirection.ROW) {
                        pos.x(pos.x() + element.size().x() + this.gap);
                    } else {
                        pos.y(pos.y() + element.size().y() + this.gap);
                    }
                });
    }

    private Vector2f calcSize(List<GUIElement> elements) {
        Vector2f size = Vector2f.zero();
        for (int i = 0; i < elements.size(); i++) {
            GUIElement element = elements.get(i);
            switch (this.direction) {
                case ROW -> size.x(
                        size.x() + element.size().x() + (i == elements.size() - 1 ? 0 : this.gap));

                case COLUMN -> size.y(
                        size.y() + element.size().y() + (i == elements.size() - 1 ? 0 : this.gap));
            }
        }
        return size;
    }
}
