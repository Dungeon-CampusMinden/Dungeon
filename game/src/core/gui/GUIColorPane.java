package core.gui;

import core.gui.math.Vector2f;
import core.gui.math.Vector3f;
import core.gui.math.Vector4f;

public class GUIColorPane extends GUIElement {

    public GUIColorPane(Vector4f color) {
        this.backgroundColor = color;
    }

    public GUIColorPane(Vector4f color, Vector2f position, Vector2f size, Vector3f rotation) {
        super(position, size, rotation);
        this.backgroundColor = color;
    }
}
