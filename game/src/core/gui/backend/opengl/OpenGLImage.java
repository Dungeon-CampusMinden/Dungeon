package core.gui.backend.opengl;

import core.Assets;
import core.gui.backend.BackendImage;

public class OpenGLImage extends BackendImage {

    protected final int glTextureHandle;

    public OpenGLImage(
            Assets.Images path, int width, int height, int channels, int glTextureHandle) {
        super(path, width, height, channels);
        this.glTextureHandle = glTextureHandle;
    }
}
