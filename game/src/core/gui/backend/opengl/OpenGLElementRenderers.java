package core.gui.backend.opengl;

import core.gui.*;
import core.gui.util.Logging;
import core.utils.logging.CustomLogLevel;
import core.utils.math.Matrix4f;
import core.utils.math.Vector2f;

import org.lwjgl.opengl.GL33;

import java.lang.reflect.Field;

public class OpenGLElementRenderers {

    private static final int PROP_NONE = 0x0000;
    private static final int PROP_HAS_BACKGROUND_COLOR = 0x0001;
    private static final int PROP_HAS_BACKGROUND_IMAGE = 0x0002;
    private static final int PROP_HAS_TOP_BORDER = 0x0004;
    private static final int PROP_HAS_RIGHT_BORDER = 0x0008;
    private static final int PROP_HAS_BOTTOM_BORDER = 0x0010;
    private static final int PROP_HAS_LEFT_BORDER = 0x0020;

    public static final IOpenGLRenderFunction renderGUIImage =
            (e, context) -> {
                GUIImage element = (GUIImage) e;
                OpenGLImage image = (OpenGLImage) element.image();
                if (image == null) {
                    return;
                }

                // Enable stencil testing & draw stencil mask
                GL33.glEnable(GL33.GL_STENCIL_TEST);
                GL33.glClearStencil(0);
                GL33.glClear(GL33.GL_STENCIL_BUFFER_BIT);

                GL33.glStencilMask(0xFF);
                GL33.glStencilFunc(GL33.GL_ALWAYS, 1, 0xFF);
                GL33.glStencilOp(GL33.GL_KEEP, GL33.GL_KEEP, GL33.GL_REPLACE);

                // Draw element rectangle (background & stencil mask)
                Matrix4f modelElement = createModelMatrix(element);
                context.begin();
                GL33.glUniformMatrix4fv(
                        context.getUniformLocation("uModel"), false, modelElement.toArray());
                if (element.backgroundColor() != null) {
                    GL33.glUniform1i(
                            context.getUniformLocation("uProperties"), PROP_HAS_BACKGROUND_COLOR);
                    GL33.glUniform4fv(
                            context.getUniformLocation("uBackgroundColor"),
                            element.backgroundColor().toArray());
                } else {
                    GL33.glUniform1i(context.getUniformLocation("uProperties"), PROP_NONE);
                }
                context.draw();
                context.end();

                // Disable stencil writing & enable stencil testing
                GL33.glStencilMask(0x00);
                GL33.glStencilFunc(GL33.GL_EQUAL, 1, 0xFF);
                GL33.glStencilOp(GL33.GL_KEEP, GL33.GL_KEEP, GL33.GL_KEEP);

                float width = 0;
                float height = 0;

                switch (element.scaleMode()) {
                    case STRETCH -> {
                        width = element.size().x();
                        height = element.size().y();
                    }
                    case CONTAIN -> {
                        float aspect = (float) image.width() / (float) image.height();
                        width = element.size().x();
                        height = width / aspect;
                        if (height > element.size().y()) {
                            height = element.size().y();
                            width = height * aspect;
                        }
                    }
                    case COVER -> {
                        float aspect = (float) image.width() / (float) image.height();
                        width = element.size().x();
                        height = width / aspect;
                        if (height < element.size().y()) {
                            height = element.size().y();
                            width = height * aspect;
                        }
                    }
                }

                Vector2f absPos = element.absolutePosition();
                float x = absPos.x() + element.size().x() / 2 - width / 2;
                float y = absPos.y() + element.size().y() / 2 - height / 2;

                Matrix4f model = Matrix4f.identity();
                model = model.multiply(Matrix4f.translate(x, y, 0.0f));
                model = model.multiply(Matrix4f.scale(width, height, 1));
                model = model.multiply(Matrix4f.rotateX(element.rotation().x()));
                model = model.multiply(Matrix4f.rotateY(element.rotation().y()));
                model = model.multiply(Matrix4f.rotateZ(element.rotation().z()));

                context.begin();
                GL33.glUniformMatrix4fv(
                        context.getUniformLocation("uModel"), false, model.toArray());
                GL33.glUniform1i(
                        context.getUniformLocation("uProperties"), PROP_HAS_BACKGROUND_IMAGE);
                GL33.glUniform1i(context.getUniformLocation("uBackgroundTexture"), 0);
                GL33.glActiveTexture(GL33.GL_TEXTURE0);
                GL33.glBindTexture(GL33.GL_TEXTURE_2D, image.glTextureHandle);
                context.draw();
                context.end();
                validate(element);

                GL33.glDisable(GL33.GL_STENCIL_TEST);
            };

    public static final IOpenGLRenderFunction renderGUIColorPane =
            (e, context) -> {
                GUIColorPane element = (GUIColorPane) e;

                Matrix4f model = createModelMatrix(element);
                context.begin();

                GL33.glUniformMatrix4fv(
                        context.getUniformLocation("uModel"), false, model.toArray());
                GL33.glUniform1i(
                        context.getUniformLocation("uProperties"), PROP_HAS_BACKGROUND_COLOR);
                GL33.glUniform4fv(
                        context.getUniformLocation("uBackgroundColor"),
                        element.backgroundColor().toArray());

                context.draw();
                context.end();
                validate(element);
            };

    public static final IOpenGLRenderFunction renderGUIText =
            (e, context) -> {
                GUIText element = (GUIText) e;

                Matrix4f model = createModelMatrix(element);
                context.begin();

                int props = 0x00;
                if (element.backgroundColor() != null) {
                    props |= PROP_HAS_BACKGROUND_COLOR;
                    GL33.glUniform4fv(
                            context.getUniformLocation("uBackgroundColor"),
                            element.backgroundColor().toArray());
                }

                GL33.glUniformMatrix4fv(
                        context.getUniformLocation("uModel"), false, model.toArray());
                GL33.glUniform1i(context.getUniformLocation("uProperties"), props);

                context.draw();
                context.end();

                Vector2f absPos = element.absolutePosition();

                GUIRoot.getInstance()
                        .backend()
                        .drawText(
                                element.text(),
                                element.font(),
                                absPos.x() + element.font().fontSize,
                                absPos.y() + element.size().y() - element.font().fontSize,
                                element.size().x() - 2 * element.font().fontSize,
                                element.size().y() - 2 * element.font().fontSize,
                                element.textColor().toRGBA());
                validate(element);
            };

    private static Matrix4f createModelMatrix(GUIElement element) {
        Vector2f absPos = element.absolutePosition();
        Matrix4f model = Matrix4f.identity();
        model = model.multiply(Matrix4f.translate(absPos.x(), absPos.y(), 0.0f));
        model = model.multiply(Matrix4f.scale(element.size().x(), element.size().y(), 1));
        model = model.multiply(Matrix4f.rotateX(element.rotation().x()));
        model = model.multiply(Matrix4f.rotateY(element.rotation().y()));
        model = model.multiply(Matrix4f.rotateZ(element.rotation().z()));
        return model;
    }

    private static void validate(GUIElement element) {
        if (element.valid()) return;
        try {
            Field f = GUIElement.class.getDeclaredField("valid");
            f.setAccessible(true);
            f.setBoolean(element, true);
            f.setAccessible(false);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            Logging.log(
                    CustomLogLevel.WARNING,
                    "Failed to validate element: %s",
                    e,
                    element.getClass().getSimpleName());
        }
    }
}
