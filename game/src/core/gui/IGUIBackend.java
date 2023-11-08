package core.gui;

import core.Assets;
import core.gui.backend.BackendImage;

import java.util.List;

public interface IGUIBackend {

    /**
     * Renders the given elements.
     *
     * @param elements The elements to render.
     */
    void render(List<GUIElement> elements);

    void resize(int width, int height);

    /**
     * Load an image from the given {@link Assets.Images} enum
     *
     * @param image The image to load
     * @return The loaded image
     */
    BackendImage loadImage(Assets.Images image);
}
