package core.gui;

import core.utils.math.Vector2f;
import core.utils.math.Vector4f;

public class GUIColorPane extends GUIElement {

    public GUIColorPane(Vector4f color) {
        this.backgroundColor = color;
    }

    @Override
    public Vector2f minimalSize() {
        return this.size();
    }

    @Override
    public Vector2f preferredSize() {
        return this.size();
    }
}
