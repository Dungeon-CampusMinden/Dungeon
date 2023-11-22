package core.gui.layouts;

import core.gui.GUIElement;
import core.gui.IGUILayout;
import core.gui.layouts.hints.FillLayoutHint;
import core.utils.math.Vector2f;

import java.util.List;

public class FillLayout implements IGUILayout {

    public enum FillDirection {
        ROW,
        COLUMN
    }

    private final FillDirection direction;

    public FillLayout(FillDirection direction) {
        this.direction = direction;
    }

    @Override
    public void layout(GUIElement parent, List<GUIElement> elements) {

        Vector2f minSize = calcMinSize(parent, elements);

        if (this.direction == FillDirection.ROW) {

            float emptySpace = parent.size().x() - minSize.x();
            float currentX = 0;

            for (GUIElement element : elements) {
                float width =
                        element.minimalSize().x()
                                + emptySpace * ((FillLayoutHint) element.layoutHint()).weight;
                element.size().x(Math.max(width, element.minimalSize().x()));
                element.size().y(parent.size().y());
                element.position().x(currentX);
                element.position().y(0);
                currentX += width;
            }

        } else if (this.direction == FillDirection.COLUMN) {
            float emptySpace = parent.size().y() - minSize.y();
            float currentY = 0;

            for (GUIElement element : elements) {
                float height =
                        element.minimalSize().y()
                                + emptySpace * ((FillLayoutHint) element.layoutHint()).weight;
                element.size().x(parent.size().x());
                element.size().y(Math.max(height, element.minimalSize().y()));
                element.position().x(0);
                element.position().y(currentY);
                currentY += height;
            }
        }
    }

    @Override
    public Vector2f calcMinSize(GUIElement parent, List<GUIElement> elements) {
        Vector2f min = new Vector2f(0, 0);
        if (this.direction == FillDirection.ROW) {
            elements.forEach(
                    e -> {
                        Vector2f size = e.minimalSize();
                        min.x(min.x() + size.x());
                        min.y(Math.max(min.y(), size.y()));
                    });
        } else if (this.direction == FillDirection.COLUMN) {
            elements.forEach(
                    e -> {
                        Vector2f size = e.minimalSize();
                        min.x(Math.max(min.x(), size.x()));
                        min.y(min.y() + size.y());
                    });
        }
        return min;
    }
}
