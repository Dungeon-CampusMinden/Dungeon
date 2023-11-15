package core.gui.backend.opengl;

import core.gui.backend.BackendImage;

import org.lwjgl.opengl.GL33;

public class OpenGLImage extends BackendImage {

    protected final int glTextureHandle;

    public OpenGLImage(int width, int height, int channels, int glTextureHandle) {
        super(width, height, channels);
        this.glTextureHandle = glTextureHandle;
    }

    @Override
    public void free() {
        GL33.glDeleteTextures(this.glTextureHandle);
    }
}
