package core.gui.backend.opengl;

import org.lwjgl.opengl.GL33;

public record OpenGLBlendState(
        boolean enabled, int srcRGB, int dstRGB, int srcAlpha, int dstAlpha) {
    public static OpenGLBlendState capture() {
        boolean enabled;
        int[] srcRGB = new int[1],
                dstRGB = new int[1],
                srcAlpha = new int[1],
                dstAlpha = new int[1];
        GL33.glGetIntegerv(GL33.GL_BLEND_SRC_RGB, srcRGB);
        GL33.glGetIntegerv(GL33.GL_BLEND_DST_RGB, dstRGB);
        GL33.glGetIntegerv(GL33.GL_BLEND_SRC_ALPHA, srcAlpha);
        GL33.glGetIntegerv(GL33.GL_BLEND_DST_ALPHA, dstAlpha);
        enabled = GL33.glIsEnabled(GL33.GL_BLEND);
        return new OpenGLBlendState(enabled, srcRGB[0], dstRGB[0], srcAlpha[0], dstAlpha[0]);
    }

    public void apply() {
        if (this.enabled) {
            GL33.glEnable(GL33.GL_BLEND);
        } else {
            GL33.glDisable(GL33.GL_BLEND);
        }
        GL33.glBlendFuncSeparate(this.srcRGB, this.dstRGB, this.srcAlpha, this.dstAlpha);
    }
}
