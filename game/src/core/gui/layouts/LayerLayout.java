package core.gui.layouts;

import core.gui.GUIElement;
import core.gui.IGUILayout;
import core.gui.math.Vector2f;

import java.util.List;

public class LayerLayout implements IGUILayout {

    @Override
    public void layout(GUIElement parent, List<GUIElement> elements) {
        elements.forEach(
                (element) -> {
                    element.position(Vector2f.zero());
                    element.size(parent.size());
                });
    }
}
