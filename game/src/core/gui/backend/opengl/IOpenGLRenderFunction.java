package core.gui.backend.opengl;

import core.gui.GUIElement;

public interface IOpenGLRenderFunction {

    void render(GUIElement element, OpenGLRenderContext context);
}
