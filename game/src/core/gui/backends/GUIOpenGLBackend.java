package core.gui.backends;

import core.gui.GUIElement;
import core.gui.IGUIBackend;
import core.utils.logging.CustomLogLevel;
import core.utils.math.Matrix4f;
import core.utils.math.VectorI;

import org.lwjgl.opengl.GL33;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.logging.Logger;

public class GUIOpenGLBackend implements IGUIBackend {

    private static final Logger LOGGER = Logger.getLogger(GUIOpenGLBackend.class.getName());

    private int frameBufferHandle = 0;
    private int depthBufferHandle = 0;
    private int textureHandle = 0;
    private int GUIShaderProgramHandle = 0, GUICompleteShaderProgramHandle = 0;
    private boolean initialized = false;
    private VectorI size;
    private Matrix4f projection, view;

    private int textureVBO = 0;

    private int testVAO = 0;
    private int testVBO = 0;
    private int testEBO = 0;

    public GUIOpenGLBackend(VectorI size) {
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

        GL33.glBindRenderbuffer(GL33.GL_RENDERBUFFER, this.depthBufferHandle);
        GL33.glRenderbufferStorage(
                GL33.GL_RENDERBUFFER, GL33.GL_DEPTH_COMPONENT, this.size.get(0), this.size.get(1));
        GL33.glBindRenderbuffer(GL33.GL_RENDERBUFFER, 0); // Unbinding renderbuffer

        GL33.glBindTexture(GL33.GL_TEXTURE_2D, this.textureHandle);
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
        GL33.glBindTexture(GL33.GL_TEXTURE_2D, 0); // Unbinding texture
    }

    public void render(List<GUIElement> elements) {
        this.init();

        GL33.glBindFramebuffer(GL33.GL_FRAMEBUFFER, this.frameBufferHandle);
        GL33.glViewport(0, 0, this.size.get(0), this.size.get(1));

        elements.forEach(
                element -> {
                    // TODO: Render Elements
                });

        // TESTING
        GL33.glUseProgram(this.GUIShaderProgramHandle);
        GL33.glUniformMatrix4fv(
                GL33.glGetUniformLocation(this.GUIShaderProgramHandle, "uProjection"),
                false,
                this.projection.toOpenGL());
        GL33.glUniformMatrix4fv(
                GL33.glGetUniformLocation(this.GUIShaderProgramHandle, "uView"),
                false,
                this.view.toOpenGL());

        GL33.glBindVertexArray(this.testVAO);
        GL33.glDrawElements(GL33.GL_TRIANGLES, 6, GL33.GL_UNSIGNED_INT, 0);
        GL33.glBindVertexArray(0);

        System.out.println("Drawing");

        GL33.glBindFramebuffer(GL33.GL_FRAMEBUFFER, 0); // Unbind framebuffer -> draw to screen

        GL33.glUseProgram(this.GUICompleteShaderProgramHandle);
        GL33.glActiveTexture(GL33.GL_TEXTURE0);
        GL33.glBindTexture(GL33.GL_TEXTURE_2D, this.textureHandle);
        GL33.glUniform1ui(
                GL33.glGetUniformLocation(this.GUICompleteShaderProgramHandle, "uTexture"), 0);

        GL33.glBlendFunc(GL33.GL_SRC_ALPHA, GL33.GL_ONE_MINUS_SRC_ALPHA);
        GL33.glEnable(GL33.GL_BLEND);
        GL33.glBindBuffer(GL33.GL_ARRAY_BUFFER, this.textureVBO);
        GL33.glDrawArrays(GL33.GL_POINTS, 0, 1);

        GL33.glBindBuffer(GL33.GL_ARRAY_BUFFER, 0);
    }

    private void init() {
        if (this.initialized) return;

        this.projection =
                Matrix4f.orthographicProjection(0, this.size.get(0), this.size.get(1), 0, 0, 100);
        this.view = Matrix4f.identity();

        this.frameBufferHandle = GL33.glGenFramebuffers();
        this.textureHandle = GL33.glGenTextures();

        // Preparing texture
        GL33.glBindTexture(GL33.GL_TEXTURE_2D, this.textureHandle);
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
        GL33.glTexParameteri(GL33.GL_TEXTURE_2D, GL33.GL_TEXTURE_WRAP_S, GL33.GL_CLAMP_TO_BORDER);
        GL33.glTexParameteri(GL33.GL_TEXTURE_2D, GL33.GL_TEXTURE_WRAP_T, GL33.GL_CLAMP_TO_BORDER);
        GL33.glTexParameterfv(
                GL33.GL_TEXTURE_2D,
                GL33.GL_TEXTURE_BORDER_COLOR,
                new float[] {0.0f, 0.0f, 0.0f, 0.0f});
        GL33.glBindTexture(GL33.GL_TEXTURE_2D, 0); // Unbinding texture

        // Preparing depth buffer
        this.depthBufferHandle = GL33.glGenRenderbuffers();
        GL33.glBindRenderbuffer(GL33.GL_RENDERBUFFER, this.depthBufferHandle);
        GL33.glRenderbufferStorage(
                GL33.GL_RENDERBUFFER, GL33.GL_DEPTH_COMPONENT, this.size.get(0), this.size.get(1));
        GL33.glFramebufferRenderbuffer(
                GL33.GL_FRAMEBUFFER,
                GL33.GL_DEPTH_ATTACHMENT,
                GL33.GL_RENDERBUFFER,
                this.depthBufferHandle);
        GL33.glBindRenderbuffer(GL33.GL_RENDERBUFFER, 0); // Unbinding renderbuffer

        GL33.glFramebufferTexture2D(
                GL33.GL_FRAMEBUFFER,
                GL33.GL_COLOR_ATTACHMENT0,
                GL33.GL_TEXTURE_2D,
                this.textureHandle,
                0);
        GL33.glDrawBuffers(GL33.GL_COLOR_ATTACHMENT0);

        if (GL33.glCheckFramebufferStatus(GL33.GL_FRAMEBUFFER) != GL33.GL_FRAMEBUFFER_COMPLETE) {
            throw new OpenGLException("Failed to create framebuffer");
        }

        // Prepare GUIShaderProgram
        try {
            this.GUIShaderProgramHandle = GL33.glCreateProgram();
            int vertexShaderHandle = GL33.glCreateShader(GL33.GL_VERTEX_SHADER);
            int fragmentShaderHandle = GL33.glCreateShader(GL33.GL_FRAGMENT_SHADER);

            LOGGER.log(
                    CustomLogLevel.DEBUG,
                    "Loading & compiling shader source of /shaders/gui/GUI.vsh");
            GL33.glShaderSource(vertexShaderHandle, this.readShaderSource("/shaders/gui/GUI.vsh"));
            this.compileShader(vertexShaderHandle);

            LOGGER.log(
                    CustomLogLevel.DEBUG,
                    "Loading & compiling shader source of /shaders/gui/GUI.fsh");
            GL33.glShaderSource(
                    fragmentShaderHandle, this.readShaderSource("/shaders/gui/GUI.fsh"));
            this.compileShader(fragmentShaderHandle);

            GL33.glAttachShader(this.GUIShaderProgramHandle, vertexShaderHandle);
            GL33.glAttachShader(this.GUIShaderProgramHandle, fragmentShaderHandle);
            GL33.glLinkProgram(this.GUIShaderProgramHandle);

            // Check if compilation was successful
            if (GL33.glGetProgrami(this.GUIShaderProgramHandle, GL33.GL_LINK_STATUS)
                    != GL33.GL_TRUE) {
                throw new OpenGLException(
                        "Failed to link shader program: "
                                + GL33.glGetProgramInfoLog(this.GUIShaderProgramHandle));
            }
        } catch (IOException | OpenGLException ex) {
            LOGGER.log(
                    CustomLogLevel.ERROR,
                    "Failed to load & compile (complete) shader: " + ex.getMessage());
        }

        try {
            this.GUICompleteShaderProgramHandle = GL33.glCreateProgram();
            int vertexShaderHandle = GL33.glCreateShader(GL33.GL_VERTEX_SHADER);
            int fragmentShaderHandle = GL33.glCreateShader(GL33.GL_FRAGMENT_SHADER);
            int geometryShaderHandle = GL33.glCreateShader(GL33.GL_GEOMETRY_SHADER);

            LOGGER.log(
                    CustomLogLevel.DEBUG,
                    "Loading & compiling shader source of /shaders/gui/GUIComplete.vsh");
            GL33.glShaderSource(
                    vertexShaderHandle, this.readShaderSource("/shaders/gui/GUIComplete.vsh"));
            this.compileShader(vertexShaderHandle);

            LOGGER.log(
                    CustomLogLevel.DEBUG,
                    "Loading & compiling shader source of /shaders/gui/GUIComplete.fsh");
            GL33.glShaderSource(
                    fragmentShaderHandle, this.readShaderSource("/shaders/gui/GUIComplete.fsh"));
            this.compileShader(fragmentShaderHandle);

            LOGGER.log(
                    CustomLogLevel.DEBUG,
                    "Loading & compiling shader source of /shaders/gui/GUIComplete.gsh");
            GL33.glShaderSource(
                    geometryShaderHandle, this.readShaderSource("/shaders/gui/GUIComplete.gsh"));
            this.compileShader(geometryShaderHandle);

            GL33.glAttachShader(this.GUICompleteShaderProgramHandle, vertexShaderHandle);
            GL33.glAttachShader(this.GUICompleteShaderProgramHandle, fragmentShaderHandle);
            GL33.glAttachShader(this.GUICompleteShaderProgramHandle, geometryShaderHandle);
            GL33.glLinkProgram(this.GUICompleteShaderProgramHandle);

            // Check if compilation was successful
            if (GL33.glGetProgrami(this.GUICompleteShaderProgramHandle, GL33.GL_LINK_STATUS)
                    != GL33.GL_TRUE) {
                throw new OpenGLException(
                        "Failed to link shader program: "
                                + GL33.glGetProgramInfoLog(this.GUICompleteShaderProgramHandle));
            }
        } catch (IOException ex) {
            LOGGER.log(
                    CustomLogLevel.ERROR,
                    "Failed to load & compile (complete) shader: " + ex.getMessage());
        }

        this.textureVBO = GL33.glGenBuffers();
        GL33.glBindBuffer(GL33.GL_ARRAY_BUFFER, this.textureVBO);
        GL33.glBufferData(GL33.GL_ARRAY_BUFFER, new float[] {0, 0, 0}, GL33.GL_STATIC_DRAW);

        this.initialized = true;

        this.testVAO = GL33.glGenVertexArrays();
        GL33.glBindVertexArray(this.testVAO);
        this.testVBO = GL33.glGenBuffers();
        GL33.glBindBuffer(GL33.GL_ARRAY_BUFFER, this.testVBO);
        this.testEBO = GL33.glGenBuffers();
        GL33.glBindBuffer(GL33.GL_ELEMENT_ARRAY_BUFFER, this.testEBO);

        float[] testData =
                new float[] {
                    100, 50, 0.0f, // top right
                    100, 100, 0.0f, // bottom right
                    50, 100, 0.0f, // bottom left
                    50, 50, 0.0f // top left
                };

        GL33.glBufferData(GL33.GL_ARRAY_BUFFER, testData, GL33.GL_STATIC_DRAW);
        GL33.glBufferData(
                GL33.GL_ELEMENT_ARRAY_BUFFER,
                new int[] {
                    3, 1, 0,
                    3, 2, 1
                },
                GL33.GL_STATIC_DRAW);

        GL33.glEnableVertexAttribArray(0);
        GL33.glVertexAttribPointer(0, 3, GL33.GL_FLOAT, false, 3 * Float.BYTES, 0);

        GL33.glBindVertexArray(0);
        GL33.glBindBuffer(GL33.GL_ARRAY_BUFFER, 0);
        GL33.glBindBuffer(GL33.GL_ELEMENT_ARRAY_BUFFER, 0);
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

    private void compileShader(int shaderHandle) throws OpenGLException {
        GL33.glCompileShader(shaderHandle);
        if (GL33.glGetShaderi(shaderHandle, GL33.GL_COMPILE_STATUS) != GL33.GL_TRUE) {
            throw new OpenGLException(
                    "Failed to compile shader: " + GL33.glGetShaderInfoLog(shaderHandle));
        }
    }

    public static class OpenGLException extends RuntimeException {
        public OpenGLException(String message) {
            super(message);
        }
    }
}
