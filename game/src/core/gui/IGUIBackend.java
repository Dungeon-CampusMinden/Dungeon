package core.gui;

import java.util.List;

public interface IGUIBackend {

    /**
     * Renders the given elements.
     *
     * @param elements The elements to render.
     */
    void render(List<GUIElement> elements);

    void resize(int width, int height);
}
