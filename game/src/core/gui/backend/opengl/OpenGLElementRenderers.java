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
                Matrix4f model = createModelMatrix(element);
                OpenGLImage image = (OpenGLImage) element.image();
                if (image == null) {
                    return;
                }
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

                GL33.glUniformMatrix4fv(
                        context.getUniformLocation("uModel"), false, model.toArray());
                GL33.glUniform1i(
                        context.getUniformLocation("uProperties"), PROP_HAS_BACKGROUND_COLOR);
                GL33.glUniform4fv(
                        context.getUniformLocation("uBackgroundColor"),
                        element.backgroundColor().toArray());

                context.draw();
                context.end();

                /*GUIRoot.getInstance()
                .backend()
                .drawText(
                        element.text(),
                        element.font(),
                        element.position().x(),
                        element.position().y(),
                        element.size().x(),
                        element.size().y(),
                        element.textColor().toRGBA());*/
                GUIRoot.getInstance()
                        .backend()
                        .drawText(
                                element.text(),
                                element.font(),
                                element.position().x() + 10,
                                element.position().y()
                                        + element.size().y()
                                        - element.font().fontSize,
                                element.size().x(),
                                element.size().y(),
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
