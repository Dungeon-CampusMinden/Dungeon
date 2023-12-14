package core.gui.layouts;

import com.badlogic.gdx.math.Vector3;

import core.gui.GUIElement;
import core.gui.IGUILayout;
import core.gui.layouts.hints.PositionTrackLayoutHint;
import core.gui.math.Vector2f;
import core.gui.util.Logging;
import core.systems.CameraSystem;
import core.utils.logging.CustomLogLevel;

import java.util.List;

public class PositionTrackLayout implements IGUILayout {

    @Override
    public void layout(GUIElement parent, List<GUIElement> elements) {
        elements.forEach(
                (element) -> {
                    PositionTrackLayoutHint hint =
                            element.layoutHint(PositionTrackLayoutHint.class);
                    if (hint == null) return;

                    float x = hint.positionComponent.position().x;
                    float y = hint.positionComponent.position().y;

                    Vector3 coords = CameraSystem.camera().project(new Vector3(x, y, 0));
                    Vector2f parentAbsPos = parent.absolutePosition();
                    Vector2f pos =
                            new Vector2f(coords.x - parentAbsPos.x(), coords.y - parentAbsPos.y());
                    element.position(pos);

                    Logging.log(CustomLogLevel.DEBUG, "Set to %f %f", pos.x(), pos.y());
                });
    }
}
