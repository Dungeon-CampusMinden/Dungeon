package core.gui.layouts;

import core.gui.GUIElement;
import core.gui.IGUILayout;
import core.gui.layouts.hints.RelativeLayoutHint;
import core.gui.math.Vector2f;

import java.util.List;

/**
 * A layout that scales and positions elements based on the parent's size. The position and size of
 * elements will be calculated based on the RelativeLayoutHint of the element.
 */
public class RelativeLayout implements IGUILayout {

    @Override
    public void layout(GUIElement parent, List<GUIElement> elements) {
        elements.stream()
                .filter(
                        element ->
                                element.layoutHint() != null
                                        && element.layoutHint() instanceof RelativeLayoutHint)
                .forEach(
                        (element) -> {
                            RelativeLayoutHint hint = (RelativeLayoutHint) element.layoutHint();
                            element.size(
                                    new Vector2f(
                                            hint.width * parent.size().x(),
                                            hint.height * parent.size().y()));
                            element.position(
                                    new Vector2f(
                                            hint.x * parent.size().x(),
                                            hint.y * parent.size().y()));
                        });
    }
}
