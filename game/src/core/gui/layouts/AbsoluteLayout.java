package core.gui.layouts;

import core.gui.GUIElement;
import core.gui.IGUILayout;
import core.gui.math.Vector2f;

import java.util.List;

public class AbsoluteLayout implements IGUILayout {

    @Override
    public void layout(GUIElement parent, List<GUIElement> elements) {}

    @Override
    public Vector2f calcMinSize(GUIElement parent, List<GUIElement> elements) {
        float maxX = 0;
        float maxY = 0;
        for (GUIElement element : elements) {
            Vector2f minSize = element.size();
            float x = element.position().x() + minSize.x();
            float y = element.position().y() + minSize.y();
            maxX = Math.max(maxX, x);
            maxY = Math.max(maxY, y);
        }
        return new Vector2f(maxX, maxY);
    }
}
