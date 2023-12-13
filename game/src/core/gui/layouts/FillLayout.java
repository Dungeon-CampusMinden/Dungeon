package core.gui.layouts;

import core.gui.GUIElement;
import core.gui.IGUILayout;
import core.gui.layouts.hints.FillLayoutHint;
import core.gui.math.Vector2f;

import java.util.*;

public class FillLayout implements IGUILayout {

    public enum FlowDirection {
        ROW,
        COLUMN
    }

    private FlowDirection direction;
    private int gap = 5;

    public FillLayout(FlowDirection direction) {
        this.direction = direction;
    }

    @Override
    public void layout(GUIElement parent, List<GUIElement> elements) {
        this.calcSizes(parent, elements); // Calculate sized based on weights
        Vector2f pos = Vector2f.zero();
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

    private void calcSizes(GUIElement parent, List<GUIElement> elements) {
        float totalWeight =
                elements.stream()
                        .map(
                                (e) ->
                                        Objects.requireNonNullElse(
                                                e.layoutHint(FillLayoutHint.class),
                                                new FillLayoutHint(1.0f)))
                        .map(FillLayoutHint::weight)
                        .reduce(0.0f, Float::sum);
        float totalGap = (elements.size() - 1) * this.gap;

        elements.forEach(
                (element) -> {
                    FillLayoutHint hint = element.layoutHint(FillLayoutHint.class);
                    if (hint != null) {
                        if (hint.weight() > 0) {
                            if (this.direction == FlowDirection.ROW) {
                                float size =
                                        (hint.weight() / totalWeight)
                                                * (parent.size().x() - totalGap);
                                element.size(new Vector2f(size, parent.size().y()));
                            } else {
                                float size =
                                        (hint.weight() / totalWeight)
                                                * (parent.size().y() - totalGap);
                                element.size(new Vector2f(parent.size().x(), size));
                            }
                        }
                    }
                });
    }

    public FlowDirection direction() {
        return direction;
    }

    public FillLayout direction(FlowDirection direction) {
        this.direction = direction;
        return this;
    }

    public int gap() {
        return gap;
    }

    public FillLayout gap(int gap) {
        this.gap = gap;
        return this;
    }
}
