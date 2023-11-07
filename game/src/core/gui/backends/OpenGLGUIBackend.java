package core.gui.backends;

import core.gui.GUIElement;
import core.gui.IGUIBackend;
import core.utils.logging.CustomLogLevel;
import core.utils.math.Matrix4f;
import core.utils.math.VectorI;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.*;
import org.lwjgl.stb.STBImage;
import org.lwjgl.stb.STBImageWrite;
import org.lwjgl.system.MemoryUtil;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OpenGLGUIBackend implements IGUIBackend {

    private static final Logger LOGGER = Logger.getLogger(OpenGLGUIBackend.class.getName());
    private final VectorI size;
    private final RenderStructure guiRenderContext = new RenderStructure();
    private final RenderStructure bufferRenderContext = new RenderStructure();
    private final RenderStructure debugRenderContext = new RenderStructure();
    private Matrix4f projection, view;

    public OpenGLGUIBackend(VectorI size) {
        this.size = size;
        this.init();
    }

    @Override
    public void resize(int width, int height) {
        this.size.set(0, width);
        this.size.set(1, height);

        this.projection =
                Matrix4f.orthographicProjection(0, this.size.get(0), this.size.get(1), 0, 0, 100);
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

    public void render(List<GUIElement> elements) {

        GL33.glClear(GL33.GL_DEPTH_BUFFER_BIT);
        { // Render to Framebuffer
            GL33.glBindFramebuffer(GL33.GL_FRAMEBUFFER, this.bufferRenderContext.frameBuffer);
            GL33.glViewport(0, 0, this.size.get(0), this.size.get(1));
            GL33.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
            GL33.glClear(GL33.GL_COLOR_BUFFER_BIT);

            // this.renderGUI(elements);
            this.renderDebug();

            GL33.glBindFramebuffer(GL33.GL_FRAMEBUFFER, 0);
        }

        this.renderBuffer();

        // Screenshot
        if (GLFW.glfwGetKey(GLFW.glfwGetCurrentContext(), GLFW.GLFW_KEY_F12) == GLFW.GLFW_PRESS) {
            this.saveFramebuffer();
        }
    }

    private void renderGUI(List<GUIElement> elements) {
        GLBlendState blendState = GLBlendState.capture();

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
        GLBlendState blendState = GLBlendState.capture();

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
        GLBlendState blendState = GLBlendState.capture();

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
        String versionstring = GL11.glGetString(GL11.GL_VERSION);

        if (versionstring == null) {
            throw new OpenGLException("Failed to get OpenGL version string");
        }

        int major, minor, patch;

        String regex = "(([0-9]+)\\.([0-9]+))(.([0-9]+))?";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(versionstring);
        if (matcher.find()) {
            String s = matcher.group();
            major = Integer.parseInt(matcher.group(2));
            minor = Integer.parseInt(matcher.group(3));
            patch = Integer.parseInt(matcher.group(5));
        } else {
            throw new OpenGLException("Failed to parse OpenGL version string");
        }

        GLCapabilities caps = GL.getCapabilities();
        if (!caps.OpenGL33) {
            throw new OpenGLException("OpenGL 3.3 or higher is required for this program to work.");
        }

        log(CustomLogLevel.DEBUG, "OpenGL Version: %d.%d.%d\n", major, minor, patch);

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
                this.makeShaderProgram("/shaders/gui/GUI.vsh", "/shaders/gui/GUI.fsh");
    }

    private void initBuffer() {

        // Init Shader
        this.bufferRenderContext.shader =
                this.makeShaderProgram(
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
        this.bufferRenderContext.texture = GL33.glGenTextures();
        GL33.glBindTexture(GL33.GL_TEXTURE_2D, this.bufferRenderContext.texture);
        GL33.glTexImage2D(
                GL33.GL_TEXTURE_2D,
                0,
                GL33.GL_RGBA,
                this.size.get(0),
                this.size.get(1),
                0,
                GL33.GL_RGBA,
                GL33.GL_UNSIGNED_BYTE,
                0);
        GL33.glTexParameteri(GL33.GL_TEXTURE_2D, GL33.GL_TEXTURE_MIN_FILTER, GL33.GL_NEAREST);
        GL33.glTexParameteri(GL33.GL_TEXTURE_2D, GL33.GL_TEXTURE_MAG_FILTER, GL33.GL_NEAREST);
        GL33.glTexParameteri(GL33.GL_TEXTURE_2D, GL33.GL_TEXTURE_WRAP_S, GL33.GL_CLAMP_TO_EDGE);
        GL33.glTexParameteri(GL33.GL_TEXTURE_2D, GL33.GL_TEXTURE_WRAP_T, GL33.GL_CLAMP_TO_EDGE);
        GL33.glTexParameterfv(
                GL33.GL_TEXTURE_2D,
                GL33.GL_TEXTURE_BORDER_COLOR,
                new float[] {0.0f, 0.0f, 0.0f, 0.0f});

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
                this.makeShaderProgram("/shaders/gui/GUIDebug.vsh", "/shaders/gui/GUIDebug.fsh");

        float[] vertices =
                new float[] {
                    0.5f, 0.5f, 1.0f, 0.0f, // top right
                    -0.5f, 0.5f, 0.0f, 0.0f, // top left
                    0.5f, -0.5f, 1.0f, 1.0f, // bottom right
                    -0.5f, -0.5f, 0.0f, 1.0f // bottom left
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

        this.debugRenderContext.texture = GL33.glGenTextures();
        GL33.glBindTexture(GL33.GL_TEXTURE_2D, this.debugRenderContext.texture);
        GL33.glTexParameteri(GL33.GL_TEXTURE_2D, GL33.GL_TEXTURE_MIN_FILTER, GL33.GL_NEAREST);
        GL33.glTexParameteri(GL33.GL_TEXTURE_2D, GL33.GL_TEXTURE_MAG_FILTER, GL33.GL_NEAREST);
        GL33.glTexParameteri(GL33.GL_TEXTURE_2D, GL33.GL_TEXTURE_WRAP_S, GL33.GL_CLAMP_TO_BORDER);
        GL33.glTexParameteri(GL33.GL_TEXTURE_2D, GL33.GL_TEXTURE_WRAP_T, GL33.GL_CLAMP_TO_BORDER);
        GL33.glTexParameterfv(
                GL33.GL_TEXTURE_2D,
                GL33.GL_TEXTURE_BORDER_COLOR,
                new float[] {0.0f, 0.0f, 0.0f, 0.0f});

        try {
            byte[] imageFileBytes = this.loadImage("/debug/debug.png");
            ByteBuffer bytes = ByteBuffer.allocateDirect(imageFileBytes.length);
            bytes.put(imageFileBytes);
            bytes.position(0);
            int[] width = new int[1];
            int[] height = new int[1];
            int[] channels = new int[1];
            ByteBuffer image = STBImage.stbi_load_from_memory(bytes, width, height, channels, 4);
            GL33.glTexImage2D(
                    GL33.GL_TEXTURE_2D,
                    0,
                    GL33.GL_RGBA,
                    width[0],
                    height[0],
                    0,
                    GL33.GL_RGBA,
                    GL33.GL_UNSIGNED_BYTE,
                    image);
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

    private CharSequence readShaderSource(String path) throws IOException {
        InputStream is = this.getClass().getResourceAsStream(path);
        if (is == null) {
            throw new IOException("Failed to load shader source from path: " + path);
        }
        InputStreamReader isr = new InputStreamReader(is, StandardCharsets.UTF_8);
        BufferedReader br = new BufferedReader(isr);
        StringBuilder builder = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) {
            builder.append(line).append("\n");
        }
        br.close();
        isr.close();
        is.close();
        return builder;
    }

    private byte[] loadImage(String path) throws IOException {
        InputStream is = this.getClass().getResourceAsStream(path);
        try (is) {
            if (is == null) {
                throw new IOException("Image not found at: " + path);
            }
            return is.readAllBytes();
        }
    }

    private int makeShaderProgram(String vertShaderPath, String fragShaderPath) {
        return this.makeShaderProgram(null, vertShaderPath, fragShaderPath);
    }

    private int makeShaderProgram(
            String geomShaderPath, String vertShaderPath, String fragShaderPath) {
        int geometryShaderHandle =
                geomShaderPath != null ? GL33.glCreateShader(GL33.GL_GEOMETRY_SHADER) : -1;
        int vertexShaderHandle =
                vertShaderPath != null ? GL33.glCreateShader(GL33.GL_VERTEX_SHADER) : -1;
        int fragmentShaderHandle =
                fragShaderPath != null ? GL33.glCreateShader(GL33.GL_FRAGMENT_SHADER) : -1;

        int shaderProgramHandle = GL33.glCreateProgram();
        try {
            if (geometryShaderHandle != -1) {
                GL33.glShaderSource(geometryShaderHandle, this.readShaderSource(geomShaderPath));
                this.compileShader(geometryShaderHandle);
                GL33.glAttachShader(shaderProgramHandle, geometryShaderHandle);
            }
            if (vertexShaderHandle != -1) {
                GL33.glShaderSource(vertexShaderHandle, this.readShaderSource(vertShaderPath));
                this.compileShader(vertexShaderHandle);
                GL33.glAttachShader(shaderProgramHandle, vertexShaderHandle);
            }
            if (fragmentShaderHandle != -1) {
                GL33.glShaderSource(fragmentShaderHandle, this.readShaderSource(fragShaderPath));
                this.compileShader(fragmentShaderHandle);
                GL33.glAttachShader(shaderProgramHandle, fragmentShaderHandle);
            }

            GL33.glLinkProgram(shaderProgramHandle);

            // Check if compilation was successful
            if (GL33.glGetProgrami(shaderProgramHandle, GL33.GL_LINK_STATUS) != GL33.GL_TRUE) {
                throw new OpenGLException(
                        "Failed to link shader program: "
                                + GL33.glGetProgramInfoLog(shaderProgramHandle));
            } else {
                log(
                        CustomLogLevel.DEBUG,
                        "Successfully linked shader program (%d)",
                        shaderProgramHandle);
            }
        } catch (IOException | OpenGLException ex) {
            log(CustomLogLevel.ERROR, "Failed to load & compile shader: %s", ex, ex.getMessage());
            GL33.glDeleteProgram(shaderProgramHandle);
            return -1;
        } finally {
            if (geometryShaderHandle != -1) GL33.glDeleteShader(geometryShaderHandle);
            if (vertexShaderHandle != -1) GL33.glDeleteShader(vertexShaderHandle);
            if (fragmentShaderHandle != -1) GL33.glDeleteShader(fragmentShaderHandle);
        }
        return shaderProgramHandle;
    }

    private void compileShader(int shaderHandle) throws OpenGLException {
        GL33.glCompileShader(shaderHandle);
        if (GL33.glGetShaderi(shaderHandle, GL33.GL_COMPILE_STATUS) != GL33.GL_TRUE) {
            throw new OpenGLException(
                    "Failed to compile shader: " + GL33.glGetShaderInfoLog(shaderHandle));
        }
    }

    private void saveFramebuffer() {
        int width = this.size.get(0);
        int height = this.size.get(1);

        // Save framebuffer to file
        GL33.glBindFramebuffer(GL33.GL_FRAMEBUFFER, this.bufferRenderContext.frameBuffer);
        ByteBuffer pixels = ByteBuffer.allocateDirect(width * height * 4);
        GL33.glReadPixels(0, 0, width, height, GL33.GL_RGBA, GL33.GL_UNSIGNED_BYTE, pixels);
        STBImageWrite.stbi_flip_vertically_on_write(true);
        STBImageWrite.stbi_write_png("GUIFrameBuffer.png", width, height, 4, pixels, 0);
        GL33.glBindFramebuffer(GL33.GL_FRAMEBUFFER, 0);
        log(CustomLogLevel.DEBUG, "Saved framebuffer to GUIFrameBuffer.png");

        // Save framebuffer texture to file
        GL33.glBindTexture(GL33.GL_TEXTURE_2D, this.bufferRenderContext.texture);
        ByteBuffer texturePixels = ByteBuffer.allocateDirect(width * height * 4);
        GL33.glGetTexImage(
                GL33.GL_TEXTURE_2D, 0, GL33.GL_RGBA, GL33.GL_UNSIGNED_BYTE, texturePixels);
        STBImageWrite.stbi_flip_vertically_on_write(true);
        STBImageWrite.stbi_write_png(
                "GUIFrameBufferTexture.png", width, height, 4, texturePixels, 0);
        GL33.glBindTexture(GL33.GL_TEXTURE_2D, 0);
        log(CustomLogLevel.DEBUG, "Saved framebuffer texture to GUIFrameBufferTexture.png");
    }

    public static class OpenGLException extends RuntimeException {
        public OpenGLException(String message) {
            super(message);
        }
    }

    private record GLBlendState(
            boolean enabled, int srcRGB, int dstRGB, int srcAlpha, int dstAlpha) {
        public static GLBlendState capture() {
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
            return new GLBlendState(enabled, srcRGB[0], dstRGB[0], srcAlpha[0], dstAlpha[0]);
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

    private static class RenderStructure {

        private final HashMap<String, Integer> uniformLocations = new HashMap<>();

        public int vao, vbo, ebo, frameBuffer, texture, shader;

        public RenderStructure() {
            this.vao = 0;
            this.vbo = 0;
            this.ebo = 0;
            this.frameBuffer = 0;
            this.texture = 0;
            this.shader = 0;
        }

        public int getUniformLocation(String name) {
            if (this.uniformLocations.containsKey(name)) {
                return this.uniformLocations.get(name);
            } else {
                int location = GL33.glGetUniformLocation(this.shader, name);
                this.uniformLocations.put(name, location);
                if (location == -1) {
                    log(
                            CustomLogLevel.WARNING,
                            "Uniform '%s' not found in shader program %d\n",
                            name,
                            this.shader);
                } else {
                    log(
                            CustomLogLevel.DEBUG,
                            "Found uniform '%s' in shader program %d@%d\n",
                            name,
                            this.shader,
                            location);
                }
                return location;
            }
        }
    }

    private static void log(Level level, String format, Object... args) {
        if (!format.endsWith("\n") && !format.endsWith("%n")) format += "\n";
        if (level == CustomLogLevel.ERROR
                || level == CustomLogLevel.SEVERE
                || level == Level.WARNING) {
            System.err.printf("[" + level.getName() + "] " + format, args);
        } else {
            System.out.printf("[" + level.getName() + "] " + format, args);
        }
        LOGGER.log(level, String.format(format, args));
    }

    private static void log(Level level, String format, Throwable throwable, Object... args) {
        if (!format.endsWith("\n") && !format.endsWith("%n")) format += "\n";
        format = "[" + level.getName() + "] " + format;
        if (level == CustomLogLevel.ERROR
                || level == CustomLogLevel.SEVERE
                || level == Level.WARNING) {
            System.err.printf("[" + level.getName() + "] " + format, args);
            throwable.printStackTrace(System.err);
        } else {
            System.out.printf("[" + level.getName() + "] " + format, args);
            throwable.printStackTrace(System.out);
        }
        LOGGER.log(level, String.format(format, args), throwable);
    }
}
