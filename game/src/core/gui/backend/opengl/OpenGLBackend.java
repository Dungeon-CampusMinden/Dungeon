package core.gui.backend.opengl;

import static core.gui.util.Logging.log;

import core.Assets;
import core.gui.*;
import core.gui.backend.BackendImage;
import core.gui.math.Matrix4f;
import core.gui.math.Vector2i;
import core.gui.util.Font;
import core.gui.util.Logging;
import core.utils.logging.CustomLogLevel;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.*;
import org.lwjgl.system.MemoryUtil;

import java.io.*;
import java.util.*;

public class OpenGLBackend implements IGUIBackend {

    private static final boolean OPENGL_DEBUG = false, DRAW_DEBUG_IMAGE = false;
    private static final Map<Assets.Images, OpenGLImage> LOADED_IMAGES = new HashMap<>();
    private static final Map<Class<? extends GUIElement>, IOpenGLRenderFunction> RENDER_FUNCTIONS =
            new HashMap<>();
    public final Vector2i size;
    private final OpenGLRenderContext guiRenderContext = new OpenGLRenderContext(this);
    private final OpenGLRenderContext bufferRenderContext = new OpenGLRenderContext(this);
    private final OpenGLRenderContext debugRenderContext = new OpenGLRenderContext(this);
    private final OpenGLRenderContext textRenderContext = new OpenGLRenderContext(this);
    private Matrix4f projection, view;
    private boolean screenshot;

    public OpenGLBackend(Vector2i size) {
        this.size = size;
        this.init();
    }

    @Override
    public void resize(int width, int height) {
        this.size.x(width);
        this.size.y(height);

        this.projection =
                Matrix4f.orthographicProjection(0, this.size.x(), 0, this.size.y(), 0, 100);
        this.view = Matrix4f.identity();

        GL33.glBindTexture(GL33.GL_TEXTURE_2D, this.bufferRenderContext.texture);
        GL33.glTexImage2D(
                GL33.GL_TEXTURE_2D,
                0,
                GL33.GL_RGBA,
                width,
                height,
                0,
                GL33.GL_RGBA,
                GL33.GL_UNSIGNED_BYTE,
                0);
        GL33.glBindTexture(GL33.GL_TEXTURE_2D, 0); // Unbinding texture

        // Delete old RenderBuffer, create new and attach
        GL33.glDeleteRenderbuffers(this.bufferRenderContext.rboDepthStencil);
        this.bufferRenderContext.rboDepthStencil = GL33.glGenRenderbuffers();
        GL33.glBindRenderbuffer(GL33.GL_RENDERBUFFER, this.bufferRenderContext.rboDepthStencil);
        GL33.glRenderbufferStorage(GL33.GL_RENDERBUFFER, GL33.GL_DEPTH24_STENCIL8, width, height);
        GL33.glBindFramebuffer(GL33.GL_FRAMEBUFFER, this.bufferRenderContext.frameBuffer);
        GL33.glFramebufferRenderbuffer(
                GL33.GL_FRAMEBUFFER,
                GL33.GL_DEPTH_STENCIL_ATTACHMENT,
                GL33.GL_RENDERBUFFER,
                this.bufferRenderContext.rboDepthStencil);
        GL33.glBindRenderbuffer(GL33.GL_RENDERBUFFER, 0);
        GL33.glBindFramebuffer(GL33.GL_FRAMEBUFFER, 0);
    }

    @Override
    public void render(List<GUIElement> elements, boolean updateNextFrame) {

        if (GLFW.glfwGetKey(GLFW.glfwGetCurrentContext(), GLFW.GLFW_KEY_F12) == GLFW.GLFW_PRESS) {
            GL33.glPolygonMode(GL33.GL_FRONT_AND_BACK, GL33.GL_LINE);
        } else {
            GL33.glPolygonMode(GL33.GL_FRONT_AND_BACK, GL33.GL_FILL);
        }

        if (updateNextFrame
                || elements.stream().anyMatch(e -> !e.valid())) { // Render to Framebuffer
            GL33.glBindFramebuffer(GL33.GL_FRAMEBUFFER, this.bufferRenderContext.frameBuffer);
            GL33.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
            GL33.glViewport(0, 0, this.size.x(), this.size.y());
            GL33.glClear(
                    GL33.GL_DEPTH_BUFFER_BIT
                            | GL33.GL_COLOR_BUFFER_BIT
                            | GL33.GL_STENCIL_BUFFER_BIT);

            OpenGLBlendState blendState = OpenGLBlendState.capture();
            GL33.glEnable(GL33.GL_BLEND);
            GL33.glBlendFunc(GL33.GL_SRC_ALPHA, GL33.GL_ONE_MINUS_SRC_ALPHA);

            GL33.glDisable(GL33.GL_DEPTH_TEST);

            this.renderGUI(elements);

            if (DRAW_DEBUG_IMAGE) this.renderDebug();

            blendState.apply();

            GL33.glBindFramebuffer(GL33.GL_FRAMEBUFFER, 0);
        }

        this.renderBuffer();

        // Screenshot
        if (GLFW.glfwGetKey(GLFW.glfwGetCurrentContext(), GLFW.GLFW_KEY_F2) == GLFW.GLFW_PRESS) {
            if (!this.screenshot) {
                String path = OpenGLUtil.screenshot();
                Logging.log(CustomLogLevel.INFO, "Saved screenshot to %s", path);
                this.screenshot = true;
            }
        } else {
            this.screenshot = false;
        }
    }

    @Override
    public OpenGLImage loadImage(Assets.Images image) {
        return LOADED_IMAGES.computeIfAbsent(
                image,
                (imageKey) -> {
                    try {
                        byte[] imageBytes = OpenGLUtil.loadResource(imageKey.path());
                        int[] width = new int[1];
                        int[] height = new int[1];
                        int[] channels = new int[1];
                        int textureHandle =
                                OpenGLUtil.createTextureFromMemoryImage(
                                        imageBytes, width, height, channels);
                        log(
                                CustomLogLevel.DEBUG,
                                "Loaded image %s(\"%s\")",
                                imageKey.name(),
                                imageKey.path());
                        return new OpenGLImage(width[0], height[0], channels[0], textureHandle);
                    } catch (IOException ex) {
                        log(CustomLogLevel.ERROR, "Failed to load image: %s", ex, ex.getMessage());
                        return null;
                    }
                });
    }

    @Override
    public BackendImage loadImageFromBitmap(byte[] bitmap, int width, int height, int channels) {
        int textureHandler =
                OpenGLUtil.createTextureFromMemoryBitmap(bitmap, width, height, channels);
        return new OpenGLImage(width, height, channels, textureHandler);
    }

    @Override
    public void drawText(
            String text,
            Font font,
            float x,
            float y,
            float maxWidth,
            float maxHeight,
            int textColor) {

        OpenGLBlendState blendState = OpenGLBlendState.capture();
        GL33.glEnable(GL33.GL_BLEND);
        GL33.glBlendFunc(GL33.GL_SRC_ALPHA, GL33.GL_ONE_MINUS_SRC_ALPHA);

        int initX = Math.round(x);
        int initY = Math.round(y);

        float maxXCoord = initX + maxWidth;

        int currentX = initX;
        int currentY = initY - (font.fontSize);

        float[] texCoords = new float[text.length() * 4 * 2]; // 4 vertices a 2 coordinates
        float[] transform = new float[text.length() * 4 * 4]; // 4x4 matrix per instance
        int[] atlas = new int[text.length()];
        float[] color = new float[text.length() * 4];

        int lastWrapIndex = 0;
        boolean tried = false;

        for (int i = 0; i < text.length(); i++) {
            int codePoint = Character.codePointAt(text, i);
            Font.Glyph glyph = font.glyphMap.get(codePoint);

            if (Font.WRAPPING_CHARACTERS.contains(codePoint)) {
                lastWrapIndex = i;
                tried = false;
            }
            if (Font.NEWLINE_CHARACTERS.contains(codePoint)) {
                currentX = initX;
                currentY =
                        currentY - (font.lineGap + font.ascent + font.descent); // TODO: Check if ok
                lastWrapIndex = i;
                tried = false;
                continue;
            }
            if (Font.WHITESPACE_CHARACTERS.containsKey(codePoint)) {
                currentX +=
                        Math.round(
                                Font.WHITESPACE_CHARACTERS.getOrDefault(codePoint, 1.0f)
                                        * font.fontSize);
                if (currentX >= maxXCoord) {
                    currentX = initX;
                    currentY =
                            currentY
                                    - (font.lineGap
                                            + font.ascent
                                            + font.descent); // TODO: Check if ok
                }
                lastWrapIndex = i;
                tried = false;
                continue;
            }
            if (codePoint == Font.CODEPOINT_TABULATOR) {
                int tabWith =
                        4
                                * Math.round(
                                        Font.WHITESPACE_CHARACTERS.get(Font.CODEPOINT_SPACE)
                                                * font.fontSize);
                currentX = currentX + tabWith - (currentX % tabWith);
                lastWrapIndex = i;
                tried = false;
                continue;
            }

            if (glyph == null) {
                glyph = font.glyphMap.get(Character.codePointAt("?", 0));
                if (glyph == null) {
                    currentX +=
                            Math.round(
                                    Font.WHITESPACE_CHARACTERS.getOrDefault(
                                                    Font.CODEPOINT_SPACE, 1.0f)
                                            * font.fontSize);
                    continue;
                }
            }

            // bottom right
            texCoords[i * 4 * 2 + 0] =
                    (glyph.xOffset() + glyph.width()) / (float) Font.MAX_ATLAS_SIZE;
            texCoords[i * 4 * 2 + 1] =
                    1 - (Font.MAX_ATLAS_SIZE - glyph.yOffset()) / (float) Font.MAX_ATLAS_SIZE;

            // bottom left
            texCoords[i * 4 * 2 + 2] = glyph.xOffset() / (float) Font.MAX_ATLAS_SIZE;
            texCoords[i * 4 * 2 + 3] =
                    1 - (Font.MAX_ATLAS_SIZE - glyph.yOffset()) / (float) Font.MAX_ATLAS_SIZE;

            // top right
            texCoords[i * 4 * 2 + 4] =
                    (glyph.xOffset() + glyph.width()) / (float) Font.MAX_ATLAS_SIZE;
            texCoords[i * 4 * 2 + 5] =
                    1
                            - (Font.MAX_ATLAS_SIZE - (glyph.yOffset() + glyph.height()))
                                    / (float) Font.MAX_ATLAS_SIZE;

            // top left
            texCoords[i * 4 * 2 + 6] = glyph.xOffset() / (float) Font.MAX_ATLAS_SIZE;
            texCoords[i * 4 * 2 + 7] =
                    1
                            - (Font.MAX_ATLAS_SIZE - (glyph.yOffset() + glyph.height()))
                                    / (float) Font.MAX_ATLAS_SIZE;

            if (currentX + glyph.width() > maxXCoord && !(glyph.width() > maxXCoord)) {
                currentX = initX;
                currentY =
                        currentY - (font.lineGap + font.ascent + font.descent); // TODO: Check if ok
                if (!tried) {
                    i = lastWrapIndex;
                    tried = true;
                    continue;
                } else {
                    lastWrapIndex = i;
                    tried = false;
                }
            }

            float gx = currentX;
            float gy = currentY + font.ascent - glyph.y2();

            Matrix4f matrix = Matrix4f.identity();
            matrix = matrix.multiply(Matrix4f.translate(gx, gy, 0));
            matrix = matrix.multiply(Matrix4f.scale(glyph.width(), glyph.height(), 1));

            float[] matrixOpenGL = matrix.toArray();
            System.arraycopy(matrixOpenGL, 0, transform, i * 4 * 4, 4 * 4);

            atlas[i] = glyph.atlas();

            color[i * 4] = ((textColor >> 24) & 0xFF) / 255f;
            color[i * 4 + 1] = ((textColor >> 16) & 0xFF) / 255f;
            color[i * 4 + 2] = ((textColor >> 8) & 0xFF) / 255f;
            color[i * 4 + 3] = (textColor & 0xFF) / 255f;

            int kerning = 0;
            int nextCodepoint = Character.codePointAt(text, Math.min(i + 1, text.length() - 1));
            if (font.kerningMap.containsKey(codePoint) && i != text.length() - 1) {
                kerning = font.kerningMap.get(codePoint).getOrDefault(nextCodepoint, 0);
            }
            currentX += glyph.xAdvance() + kerning;
        }

        GL33.glBindBuffer(
                GL33.GL_ARRAY_BUFFER, this.textRenderContext.additionalBuffers.get("texCoords"));
        GL33.glBufferData(GL33.GL_ARRAY_BUFFER, texCoords, GL33.GL_DYNAMIC_DRAW);
        GL33.glBindBuffer(
                GL33.GL_ARRAY_BUFFER, this.textRenderContext.additionalBuffers.get("transform"));
        GL33.glBufferData(GL33.GL_ARRAY_BUFFER, transform, GL33.GL_DYNAMIC_DRAW);
        GL33.glBindBuffer(
                GL33.GL_ARRAY_BUFFER, this.textRenderContext.additionalBuffers.get("atlas"));
        GL33.glBufferData(GL33.GL_ARRAY_BUFFER, atlas, GL33.GL_DYNAMIC_DRAW);
        GL33.glBindBuffer(
                GL33.GL_ARRAY_BUFFER, this.textRenderContext.additionalBuffers.get("color"));
        GL33.glBufferData(GL33.GL_ARRAY_BUFFER, color, GL33.GL_DYNAMIC_DRAW);
        GL33.glBindBuffer(GL33.GL_ARRAY_BUFFER, 0);

        GL33.glUseProgram(this.textRenderContext.shader);
        GL33.glUniformMatrix4fv(
                this.textRenderContext.getUniformLocation("uProjection"),
                false,
                this.projection.toArray());
        GL33.glUniformMatrix4fv(
                this.textRenderContext.getUniformLocation("uView"), false, this.view.toArray());

        for (int i = 0; i < font.fontAtlas.length; i++) {
            GL33.glActiveTexture(GL33.GL_TEXTURE0 + i);
            GL33.glBindTexture(
                    GL33.GL_TEXTURE_2D, ((OpenGLImage) font.fontAtlas[i]).glTextureHandle);
            GL33.glUniform1i(this.textRenderContext.getUniformLocation("atlases[" + i + "]"), i);
        }

        GL33.glBindVertexArray(this.textRenderContext.vao);
        GL33.glDrawElementsInstanced(
                GL33.GL_TRIANGLE_STRIP, 4, GL33.GL_UNSIGNED_SHORT, 0, text.length());
        GL33.glBindVertexArray(0);

        blendState.apply();
    }

    private void renderGUI(List<GUIElement> elements) {
        OpenGLBlendState blendState = OpenGLBlendState.capture();

        GL33.glUseProgram(this.guiRenderContext.shader);
        GL33.glUniformMatrix4fv(
                this.guiRenderContext.getUniformLocation("uProjection"),
                false,
                this.projection.toArray());
        GL33.glUniformMatrix4fv(
                this.guiRenderContext.getUniformLocation("uView"), false, this.view.toArray());

        GL33.glBindVertexArray(this.guiRenderContext.vao);

        elements.forEach(
                element -> {
                    Class<? extends GUIElement> elementClass = element.getClass();
                    IOpenGLRenderFunction renderFunction = RENDER_FUNCTIONS.get(elementClass);
                    if (renderFunction != null) {
                        renderFunction.render(element, guiRenderContext);
                    }
                });
        GL33.glBindVertexArray(0);

        blendState.apply();
    }

    private void renderBuffer() {
        OpenGLBlendState blendState = OpenGLBlendState.capture();

        // Enable Alpha Blending
        GL33.glEnable(GL33.GL_BLEND);
        GL33.glBlendFunc(GL33.GL_SRC_ALPHA, GL33.GL_ONE_MINUS_SRC_ALPHA);

        // Bind Shader Program
        GL33.glUseProgram(this.bufferRenderContext.shader);

        // Bind buffer VAO
        GL33.glBindVertexArray(this.bufferRenderContext.vao);

        // Bind buffer texture
        GL33.glActiveTexture(GL33.GL_TEXTURE0);
        GL33.glBindTexture(GL33.GL_TEXTURE_2D, this.bufferRenderContext.texture);
        GL33.glUniform1i(this.bufferRenderContext.getUniformLocation("uTexture"), 0);

        // Draw buffer
        GL33.glDrawElements(GL33.GL_TRIANGLE_STRIP, 4, GL33.GL_UNSIGNED_SHORT, 0);

        // Unbind VAO
        GL33.glBindVertexArray(0);

        blendState.apply();
    }

    private void renderDebug() {
        OpenGLBlendState blendState = OpenGLBlendState.capture();

        GL33.glEnable(GL33.GL_BLEND);
        GL33.glBlendFuncSeparate(GL33.GL_SRC_ALPHA, GL33.GL_ONE_MINUS_SRC_ALPHA, 1, 1);

        GL33.glUseProgram(this.debugRenderContext.shader);
        GL33.glUniformMatrix4fv(
                this.debugRenderContext.getUniformLocation("uProjection"),
                false,
                this.projection.toArray());
        GL33.glUniformMatrix4fv(
                this.debugRenderContext.getUniformLocation("uView"), false, this.view.toArray());

        GL33.glActiveTexture(GL33.GL_TEXTURE0);
        GL33.glBindTexture(GL33.GL_TEXTURE_2D, this.debugRenderContext.texture);
        GL33.glUniform1i(this.debugRenderContext.getUniformLocation("uTexture"), 0);

        GL33.glBindVertexArray(this.debugRenderContext.vao);
        GL33.glDrawElements(GL33.GL_TRIANGLE_STRIP, 4, GL33.GL_UNSIGNED_SHORT, 0);
        GL33.glBindVertexArray(0);

        blendState.apply();
    }

    private void init() {
        if (OPENGL_DEBUG) this.initOpenGLDebugging();

        // Swap top & bottom so that the origin is in the bottom left corner
        this.projection =
                Matrix4f.orthographicProjection(0, this.size.x(), 0, this.size.y(), 0, 100);
        this.view = Matrix4f.identity();

        this.initGUI();
        this.initText();
        this.initBuffer();
        if (DRAW_DEBUG_IMAGE) this.initDebug();
    }

    private void initOpenGLDebugging() {
        GLCapabilities caps = GL.getCapabilities();
        if (!caps.OpenGL33) {
            throw new OpenGLException("OpenGL 3.3 or higher is required for this program to work.");
        }

        int[] version = OpenGLUtil.getOpenGLVersion();
        log(CustomLogLevel.DEBUG, "OpenGL Version: %d.%d.%d\n", version[0], version[1], version[2]);

        if (caps.OpenGL43) {
            GL43.glEnable(GL43.GL_DEBUG_OUTPUT);
            GL43.glDebugMessageCallback(
                    GLDebugMessageCallback.create(
                            (source, type, id, severity, length, message, userParam) -> {
                                if (type == GL43.GL_DEBUG_TYPE_ERROR) {
                                    log(
                                            CustomLogLevel.ERROR,
                                            "OpenGL ERROR [t: %x, s: %x]: %s\n",
                                            type,
                                            severity,
                                            GLDebugMessageCallback.getMessage(length, message));
                                } else {
                                    log(
                                            CustomLogLevel.DEBUG,
                                            "OpenGL DEBUG [t: %x, s: %x]: %s\n",
                                            type,
                                            severity,
                                            GLDebugMessageCallback.getMessage(length, message));
                                }
                            }),
                    MemoryUtil.NULL);
            log(
                    CustomLogLevel.DEBUG,
                    "OpenGL version 4.3.0 or higher detected. Debugging enabled.");
        } else {
            log(
                    CustomLogLevel.WARNING,
                    "Current OpenGL version does not support debugging. Debugging will be disabled.");
        }
    }

    private void initGUI() {
        this.guiRenderContext.shader =
                OpenGLUtil.makeShaderProgram("/shaders/gui/GUI.vsh", "/shaders/gui/GUI.fsh");

        float[] vertices =
                new float[] {
                    1.0f, 1.0f, 0.0f, 1.0f, 1.0f, // top right
                    0.0f, 1.0f, 0.0f, 0.0f, 1.0f, // top left
                    1.0f, 0.0f, 0.0f, 1.0f, 0.0f, // bottom right
                    0.0f, 0.0f, 0.0f, 0.0f, 0.0f // bottom left
                };
        short[] indices = new short[] {0, 1, 2, 3};

        this.guiRenderContext.vao = GL33.glGenVertexArrays();
        this.guiRenderContext.vbo = GL33.glGenBuffers();
        this.guiRenderContext.ebo = GL33.glGenBuffers();

        GL33.glBindVertexArray(this.guiRenderContext.vao);
        GL33.glBindBuffer(GL33.GL_ARRAY_BUFFER, this.guiRenderContext.vbo);
        GL33.glBindBuffer(GL33.GL_ELEMENT_ARRAY_BUFFER, this.guiRenderContext.ebo);

        GL33.glBufferData(GL33.GL_ARRAY_BUFFER, vertices, GL33.GL_STATIC_DRAW);
        GL33.glBufferData(GL33.GL_ELEMENT_ARRAY_BUFFER, indices, GL33.GL_STATIC_DRAW);

        int aPositionLocation = GL33.glGetAttribLocation(this.guiRenderContext.shader, "aPosition");
        GL33.glEnableVertexAttribArray(aPositionLocation);
        GL33.glVertexAttribPointer(aPositionLocation, 2, GL33.GL_FLOAT, false, 5 * Float.BYTES, 0);

        int aTexCoordLocation = GL33.glGetAttribLocation(this.guiRenderContext.shader, "aTexCoord");
        GL33.glEnableVertexAttribArray(aTexCoordLocation);
        GL33.glVertexAttribPointer(
                aTexCoordLocation, 3, GL33.GL_FLOAT, false, 5 * Float.BYTES, 3 * Float.BYTES);

        GL33.glBindVertexArray(0);
        GL33.glBindBuffer(GL33.GL_ARRAY_BUFFER, 0);
        GL33.glBindBuffer(GL33.GL_ELEMENT_ARRAY_BUFFER, 0);

        RENDER_FUNCTIONS.put(GUIColorPane.class, OpenGLElementRenderers.renderGUIColorPane);
        RENDER_FUNCTIONS.put(GUIImage.class, OpenGLElementRenderers.renderGUIImage);
        RENDER_FUNCTIONS.put(GUIText.class, OpenGLElementRenderers.renderGUIText);
    }

    private void initBuffer() {

        // Init Shader
        this.bufferRenderContext.shader =
                OpenGLUtil.makeShaderProgram(
                        null, "/shaders/gui/GUIBuffer.vsh", "/shaders/gui/GUIBuffer.fsh");

        float[] vertices =
                new float[] {
                    1.0f, 1.0f, 1.0f, 1.0f, // top right
                    -1.0f, 1.0f, 0.0f, 1.0f, // top left
                    1.0f, -1.0f, 1.0f, 0.0f, // bottom right
                    -1.0f, -1.0f, 0.0f, 0.0f // bottom left
                };

        short[] indices = new short[] {0, 1, 2, 3};

        this.bufferRenderContext.vao = GL33.glGenVertexArrays();
        this.bufferRenderContext.vbo = GL33.glGenBuffers();
        this.bufferRenderContext.ebo = GL33.glGenBuffers();

        GL33.glBindVertexArray(this.bufferRenderContext.vao);
        GL33.glBindBuffer(GL33.GL_ARRAY_BUFFER, this.bufferRenderContext.vbo);
        GL33.glBindBuffer(GL33.GL_ELEMENT_ARRAY_BUFFER, this.bufferRenderContext.ebo);

        GL33.glBufferData(GL33.GL_ARRAY_BUFFER, vertices, GL33.GL_STATIC_DRAW);
        GL33.glBufferData(GL33.GL_ELEMENT_ARRAY_BUFFER, indices, GL33.GL_STATIC_DRAW);

        int aPositionLocation =
                GL33.glGetAttribLocation(this.bufferRenderContext.shader, "aPosition");
        GL33.glEnableVertexAttribArray(aPositionLocation);
        GL33.glVertexAttribPointer(aPositionLocation, 2, GL33.GL_FLOAT, false, 4 * Float.BYTES, 0);

        int aTexCoordLocation =
                GL33.glGetAttribLocation(this.bufferRenderContext.shader, "aTexCoord");
        GL33.glEnableVertexAttribArray(aTexCoordLocation);
        GL33.glVertexAttribPointer(
                aTexCoordLocation, 2, GL33.GL_FLOAT, false, 4 * Float.BYTES, 2 * Float.BYTES);

        GL33.glBindVertexArray(0);
        GL33.glBindBuffer(GL33.GL_ARRAY_BUFFER, 0);
        GL33.glBindBuffer(GL33.GL_ELEMENT_ARRAY_BUFFER, 0);

        // Init texture
        this.bufferRenderContext.texture =
                OpenGLUtil.createEmptyTexture(this.size.x(), this.size.y());

        // Init Framebuffer
        this.bufferRenderContext.frameBuffer = GL33.glGenFramebuffers();
        GL33.glBindFramebuffer(GL33.GL_FRAMEBUFFER, this.bufferRenderContext.frameBuffer);

        // Attach texture to framebuffer
        GL33.glFramebufferTexture2D(
                GL33.GL_FRAMEBUFFER,
                GL33.GL_COLOR_ATTACHMENT0,
                GL33.GL_TEXTURE_2D,
                this.bufferRenderContext.texture,
                0);

        this.bufferRenderContext.rboDepthStencil = GL33.glGenRenderbuffers();
        GL33.glBindRenderbuffer(GL33.GL_RENDERBUFFER, this.bufferRenderContext.rboDepthStencil);
        GL33.glRenderbufferStorage(
                GL33.GL_RENDERBUFFER, GL33.GL_DEPTH24_STENCIL8, this.size.x(), this.size.y());
        GL33.glBindRenderbuffer(GL33.GL_RENDERBUFFER, 0);
        GL33.glFramebufferRenderbuffer(
                GL33.GL_FRAMEBUFFER,
                GL33.GL_DEPTH_STENCIL_ATTACHMENT,
                GL33.GL_RENDERBUFFER,
                this.bufferRenderContext.rboDepthStencil);

        // Check if framebuffer is complete
        if (GL33.glCheckFramebufferStatus(GL33.GL_FRAMEBUFFER) != GL33.GL_FRAMEBUFFER_COMPLETE) {
            throw new OpenGLException("Failed to create framebuffer");
        }

        // Unbind texture and framebuffer
        GL33.glBindFramebuffer(GL33.GL_FRAMEBUFFER, 0);
        GL33.glBindTexture(GL33.GL_TEXTURE_2D, 0);
        GL33.glBindRenderbuffer(GL33.GL_RENDERBUFFER, 0);
    }

    private void initDebug() {
        this.debugRenderContext.shader =
                OpenGLUtil.makeShaderProgram(
                        "/shaders/gui/GUIDebug.vsh", "/shaders/gui/GUIDebug.fsh");

        float[] vertices =
                new float[] {
                    0.5f, 0.5f, 1.0f, 1.0f, // top right
                    -0.5f, 0.5f, 0.0f, 1.0f, // top left
                    0.5f, -0.5f, 1.0f, 0.0f, // bottom right
                    -0.5f, -0.5f, 0.0f, 0.0f // bottom left
                };
        short[] indices = new short[] {0, 1, 2, 3};

        this.debugRenderContext.vao = GL33.glGenVertexArrays();
        this.debugRenderContext.vbo = GL33.glGenBuffers();
        this.debugRenderContext.ebo = GL33.glGenBuffers();

        GL33.glBindVertexArray(this.debugRenderContext.vao);
        GL33.glBindBuffer(GL33.GL_ARRAY_BUFFER, this.debugRenderContext.vbo);
        GL33.glBindBuffer(GL33.GL_ELEMENT_ARRAY_BUFFER, this.debugRenderContext.ebo);

        GL33.glBufferData(GL33.GL_ARRAY_BUFFER, vertices, GL33.GL_STATIC_DRAW);
        GL33.glBufferData(GL33.GL_ELEMENT_ARRAY_BUFFER, indices, GL33.GL_STATIC_DRAW);

        int aPositionLocation =
                GL33.glGetAttribLocation(this.debugRenderContext.shader, "aPosition");
        GL33.glEnableVertexAttribArray(aPositionLocation);
        GL33.glVertexAttribPointer(aPositionLocation, 2, GL33.GL_FLOAT, false, 4 * Float.BYTES, 0);

        int aTexCoordLocation =
                GL33.glGetAttribLocation(this.debugRenderContext.shader, "aTexCoord");
        GL33.glEnableVertexAttribArray(aTexCoordLocation);
        GL33.glVertexAttribPointer(
                aTexCoordLocation, 2, GL33.GL_FLOAT, false, 4 * Float.BYTES, 2 * Float.BYTES);

        GL33.glBindVertexArray(0);
        GL33.glBindBuffer(GL33.GL_ARRAY_BUFFER, 0);
        GL33.glBindBuffer(GL33.GL_ELEMENT_ARRAY_BUFFER, 0);

        try {
            byte[] imageFileBytes = OpenGLUtil.loadResource("/debug/debug.png");
            int[] width = new int[1];
            int[] height = new int[1];
            int[] channels = new int[1];
            this.debugRenderContext.texture =
                    OpenGLUtil.createTextureFromMemoryImage(
                            imageFileBytes, width, height, channels);
            log(
                    CustomLogLevel.DEBUG,
                    "Loaded debug texture (%dx%d@%d)",
                    width[0],
                    height[0],
                    channels[0] * 8);
        } catch (IOException ex) {
            log(CustomLogLevel.ERROR, "Failed to load debug texture: %s", ex, ex.getMessage());
        }

        GL33.glBindTexture(GL33.GL_TEXTURE_2D, 0);
    }

    private void initText() {
        float[] vertices =
                new float[] {
                    1.0f, 1.0f,
                    0.0f, 1.0f,
                    1.0f, 0.0f,
                    0.0f, 0.0f
                };

        short[] indices = new short[] {0, 1, 2, 3};

        this.textRenderContext.shader =
                OpenGLUtil.makeShaderProgram(
                        "/shaders/gui/GUIText.vsh", "/shaders/gui/GUIText.fsh");

        this.textRenderContext.vao = GL33.glGenVertexArrays();
        this.textRenderContext.vbo = GL33.glGenBuffers();
        this.textRenderContext.ebo = GL33.glGenBuffers();
        this.textRenderContext.additionalBuffers.put("texCoords", GL33.glGenBuffers());
        this.textRenderContext.additionalBuffers.put("transform", GL33.glGenBuffers());
        this.textRenderContext.additionalBuffers.put("atlas", GL33.glGenBuffers());
        this.textRenderContext.additionalBuffers.put("color", GL33.glGenBuffers());

        int texCoordsBuffer = this.textRenderContext.additionalBuffers.get("texCoords");
        int transformBuffer = this.textRenderContext.additionalBuffers.get("transform");
        int atlasBuffer = this.textRenderContext.additionalBuffers.get("atlas");
        int colorBuffer = this.textRenderContext.additionalBuffers.get("color");

        int aPositionLocation =
                GL33.glGetAttribLocation(this.textRenderContext.shader, "aPosition");
        int aTexCoordsLocation =
                GL33.glGetAttribLocation(this.textRenderContext.shader, "aTexCoords");
        int aTransformMatrix =
                GL33.glGetAttribLocation(this.textRenderContext.shader, "aTransformMatrix");
        int aAtlasLocation = GL33.glGetAttribLocation(this.textRenderContext.shader, "aAtlas");
        int aColor = GL33.glGetAttribLocation(this.textRenderContext.shader, "aColor");

        GL33.glBindVertexArray(this.textRenderContext.vao);

        GL33.glBindBuffer(GL33.GL_ARRAY_BUFFER, this.textRenderContext.vbo);
        GL33.glBufferData(GL33.GL_ARRAY_BUFFER, vertices, GL33.GL_STATIC_DRAW);

        GL33.glBindBuffer(GL33.GL_ELEMENT_ARRAY_BUFFER, this.textRenderContext.ebo);
        GL33.glBufferData(GL33.GL_ELEMENT_ARRAY_BUFFER, indices, GL33.GL_STATIC_DRAW);

        GL33.glEnableVertexAttribArray(aPositionLocation);
        GL33.glVertexAttribPointer(aPositionLocation, 2, GL33.GL_FLOAT, false, 2 * Float.BYTES, 0);

        GL33.glBindBuffer(GL33.GL_ARRAY_BUFFER, texCoordsBuffer);
        for (int i = 0; i < 4; i++) {
            GL33.glEnableVertexAttribArray(aTexCoordsLocation + i);
            GL33.glVertexAttribPointer(
                    aTexCoordsLocation + i,
                    2,
                    GL33.GL_FLOAT,
                    false,
                    2 * 4 * Float.BYTES,
                    i * 2 * Float.BYTES);
        }

        GL33.glBindBuffer(GL33.GL_ARRAY_BUFFER, transformBuffer);
        GL33.glEnableVertexAttribArray(aTransformMatrix);
        GL33.glVertexAttribPointer(
                aTransformMatrix + 0, 4, GL33.GL_FLOAT, false, 4 * 4 * Float.BYTES, 0);

        GL33.glEnableVertexAttribArray(aTransformMatrix + 1);
        GL33.glVertexAttribPointer(
                aTransformMatrix + 1,
                4,
                GL33.GL_FLOAT,
                false,
                4 * 4 * Float.BYTES,
                4 * Float.BYTES);

        GL33.glEnableVertexAttribArray(aTransformMatrix + 2);
        GL33.glVertexAttribPointer(
                aTransformMatrix + 2,
                4,
                GL33.GL_FLOAT,
                false,
                4 * 4 * Float.BYTES,
                8 * Float.BYTES);

        GL33.glEnableVertexAttribArray(aTransformMatrix + 3);
        GL33.glVertexAttribPointer(
                aTransformMatrix + 3,
                4,
                GL33.GL_FLOAT,
                false,
                4 * 4 * Float.BYTES,
                12 * Float.BYTES);

        GL33.glBindBuffer(GL33.GL_ARRAY_BUFFER, atlasBuffer);
        GL33.glEnableVertexAttribArray(aAtlasLocation);
        GL33.glVertexAttribPointer(aAtlasLocation, 1, GL33.GL_INT, false, Integer.BYTES, 0);

        GL33.glBindBuffer(GL33.GL_ARRAY_BUFFER, colorBuffer);
        GL33.glEnableVertexAttribArray(aColor);
        GL33.glVertexAttribPointer(aColor, 4, GL33.GL_FLOAT, false, 4 * Float.BYTES, 0);

        // For every vertex
        GL33.glVertexAttribDivisor(aPositionLocation, 0);
        for (int i = 0; i < 4; i++) {
            GL33.glVertexAttribDivisor(aTexCoordsLocation + i, 1);
        }

        // For every instance
        GL33.glVertexAttribDivisor(aTransformMatrix, 1);
        GL33.glVertexAttribDivisor(aTransformMatrix + 1, 1);
        GL33.glVertexAttribDivisor(aTransformMatrix + 2, 1);
        GL33.glVertexAttribDivisor(aTransformMatrix + 3, 1);
        GL33.glVertexAttribDivisor(aAtlasLocation, 1);
        GL33.glVertexAttribDivisor(aColor, 1);

        GL33.glBindVertexArray(0);
        GL33.glBindBuffer(GL33.GL_ARRAY_BUFFER, 0);
        GL33.glBindBuffer(GL33.GL_ELEMENT_ARRAY_BUFFER, 0);
    }
}
