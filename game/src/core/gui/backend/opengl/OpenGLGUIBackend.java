package core.gui.backend.opengl;

import static core.gui.backend.opengl.OpenGLUtil.log;

import core.Assets;
import core.gui.GUIElement;
import core.gui.IGUIBackend;
import core.utils.logging.CustomLogLevel;
import core.utils.math.Matrix4f;
import core.utils.math.VectorI;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.*;
import org.lwjgl.system.MemoryUtil;

import java.io.*;
import java.util.*;

public class OpenGLGUIBackend implements IGUIBackend {

    private static final Map<Assets.Images, OpenGLImage> LOADED_IMAGES = new HashMap<>();
    private final VectorI size;
    private final OpenGLRenderStructure guiRenderContext = new OpenGLRenderStructure();
    private final OpenGLRenderStructure bufferRenderContext = new OpenGLRenderStructure();
    private final OpenGLRenderStructure debugRenderContext = new OpenGLRenderStructure();
    private Matrix4f projection, view;

    public OpenGLGUIBackend(VectorI size) {
        this.size = size;
        this.init();
    }

    @Override
    public void resize(int width, int height) {
        this.size.x(width);
        this.size.y(height);

        this.projection =
                Matrix4f.orthographicProjection(0, this.size.x(), this.size.y(), 0, 0, 100);
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
    }

    @Override
    public void render(List<GUIElement> elements) {

        GL33.glClear(GL33.GL_DEPTH_BUFFER_BIT);
        { // Render to Framebuffer
            GL33.glBindFramebuffer(GL33.GL_FRAMEBUFFER, this.bufferRenderContext.frameBuffer);
            GL33.glViewport(0, 0, this.size.x(), this.size.y());
            GL33.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
            GL33.glClear(GL33.GL_COLOR_BUFFER_BIT);

            // this.renderGUI(elements);
            this.renderDebug();

            GL33.glBindFramebuffer(GL33.GL_FRAMEBUFFER, 0);
        }

        this.renderBuffer();

        // Screenshot
        if (GLFW.glfwGetKey(GLFW.glfwGetCurrentContext(), GLFW.GLFW_KEY_F2) == GLFW.GLFW_PRESS) {
            String path = OpenGLUtil.screenshot();
            OpenGLUtil.log(CustomLogLevel.INFO, "Saved screenshot to %s", path);
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
                                OpenGLUtil.createTexture(imageBytes, width, height, channels);
                        return new OpenGLImage(
                                image, width[0], height[0], channels[0], textureHandle);
                    } catch (IOException ex) {
                        log(CustomLogLevel.ERROR, "Failed to load image: %s", ex, ex.getMessage());
                        return null;
                    }
                });
    }

    private void renderGUI(List<GUIElement> elements) {
        OpenGLBlendState blendState = OpenGLBlendState.capture();

        GL33.glUseProgram(this.guiRenderContext.shader);
        GL33.glUniformMatrix4fv(
                this.guiRenderContext.getUniformLocation("uProjection"),
                false,
                this.projection.toOpenGL());
        GL33.glUniformMatrix4fv(
                this.guiRenderContext.getUniformLocation("uView"), false, this.view.toOpenGL());

        elements.forEach(
                element -> {
                    // TODO: Render Elements
                });

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
                this.projection.toOpenGL());
        GL33.glUniformMatrix4fv(
                this.debugRenderContext.getUniformLocation("uView"), false, this.view.toOpenGL());

        GL33.glActiveTexture(GL33.GL_TEXTURE0);
        GL33.glBindTexture(GL33.GL_TEXTURE_2D, this.debugRenderContext.texture);
        GL33.glUniform1i(this.debugRenderContext.getUniformLocation("uTexture"), 0);

        GL33.glBindVertexArray(this.debugRenderContext.vao);
        GL33.glDrawElements(GL33.GL_TRIANGLE_STRIP, 4, GL33.GL_UNSIGNED_SHORT, 0);
        GL33.glBindVertexArray(0);

        blendState.apply();
    }

    private void init() {
        this.initOpenGLDebugging();

        this.projection = Matrix4f.identity();
        this.view = Matrix4f.identity();

        this.initGUI();
        this.initBuffer();
        this.initDebug();
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
        GL33.glDrawBuffers(new int[] {GL33.GL_COLOR_ATTACHMENT0});

        // Check if framebuffer is complete
        if (GL33.glCheckFramebufferStatus(GL33.GL_FRAMEBUFFER) != GL33.GL_FRAMEBUFFER_COMPLETE) {
            throw new OpenGLException("Failed to create framebuffer");
        }

        // Unbind texture and framebuffer
        GL33.glBindFramebuffer(GL33.GL_FRAMEBUFFER, 0);
        GL33.glBindTexture(GL33.GL_TEXTURE_2D, 0);
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
                    OpenGLUtil.createTexture(imageFileBytes, width, height, channels);
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
}
