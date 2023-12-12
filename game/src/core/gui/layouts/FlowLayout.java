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

    private FlowDirection direction;
    private FlowAlignment alignment;
    private int gap = 5;

    public FlowLayout(FlowDirection direction, FlowAlignment alignment) {
        this.direction = direction;
        this.alignment = alignment;
    }

    @Override
    public void layout(GUIElement parent, List<GUIElement> elements) {
        Vector2f size = parent.size();
        Vector2f pos = Vector2f.zero();

        switch (this.alignment) {
            case START -> pos = Vector2f.zero();
            case END -> pos = size.subtract(this.calcMinSize(parent, elements));
            case CENTER -> pos =
                    size.subtract(
                            this.calcMinSize(parent, elements)
                                    .divide(
                                            this.direction == FlowDirection.ROW ? 2 : 1,
                                            this.direction == FlowDirection.COLUMN ? 2 : 1));
        }

        if (this.direction == FlowDirection.COLUMN) {
            for (GUIElement element : elements) {
                Vector2f minSize = element.minimalSize();
                element.position(pos);
                element.size(new Vector2f(size.x(), minSize.y()));
                pos.y(pos.y() + minSize.y() + this.gap);
            }
        } else if (this.direction == FlowDirection.ROW) {
            for (GUIElement element : elements) {
                Vector2f minSize = element.minimalSize();
                element.position(pos);
                element.size(new Vector2f(minSize.x(), size.y()));
                pos.x(pos.x() + minSize.x() + this.gap);
            }
        }
    }

    @Override
    public Vector2f calcMinSize(GUIElement parent, List<GUIElement> elements) {
        Vector2f size = Vector2f.zero();
        if (this.direction == FlowDirection.COLUMN) {
            for (GUIElement element : elements) {
                Vector2f minSize = element.minimalSize();
                size.x(Math.max(size.x(), minSize.x()));
                size.y(size.y() + minSize.y());
            }
        } else if (this.direction == FlowDirection.ROW) {
            for (GUIElement element : elements) {
                Vector2f minSize = element.minimalSize();
                size.x(size.x() + minSize.x());
                size.y(Math.max(size.y(), minSize.y()));
            }
        }
        return size;
    }

    public FlowDirection direction() {
        return direction;
    }

    public FlowLayout direction(FlowDirection direction) {
        this.direction = direction;
        return this;
    }

    public FlowAlignment alignment() {
        return alignment;
    }

    public FlowLayout alignment(FlowAlignment alignment) {
        this.alignment = alignment;
        return this;
    }

    public int gap() {
        return gap;
    }

    public FlowLayout gap(int gap) {
        this.gap = gap;
        return this;
    }
}
