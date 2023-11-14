package core.gui;

import core.utils.math.Vector2f;
import core.utils.math.Vector3f;
import core.utils.math.Vector4f;

public class GUIColorPane extends GUIElement {

    public GUIColorPane(Vector4f color) {
        this.backgroundColor = color;
        this.minimalSize = new Vector2f(100, 100);
    }

    public GUIColorPane(
            Vector4f color,
            Vector2f position,
            Vector2f size,
            Vector2f minimalSize,
            Vector3f rotation) {
        super(position, size, minimalSize, rotation);
        this.backgroundColor = color;
    }

    @Override
    public Vector2f preferredSize() {
        return this.minimalSize();
    }
}
