package core.gui.backends;

import core.gui.IGUIBackend;
import core.utils.math.VectorI;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL33;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class GUIOpenGLBackend implements IGUIBackend {

    private int frameBufferHandle = 0;
    private int textureHandle = 0;
    private int shaderProgramHandle = 0;
    private boolean initialized = false;

    private VectorI size;

    public GUIOpenGLBackend(VectorI size) {
        this.size = size;
        this.init();
    }

    private void init() {
        if (this.initialized) return;

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

        GL33.glFramebufferTexture2D(
                GL33.GL_FRAMEBUFFER,
                GL33.GL_COLOR_ATTACHMENT0,
                GL33.GL_TEXTURE_2D,
                this.textureHandle,
                0);
        GL33.glDrawBuffers(GL33.GL_COLOR_ATTACHMENT0);

        if (GL33.glCheckFramebufferStatus(GL33.GL_FRAMEBUFFER) != GL33.GL_FRAMEBUFFER_COMPLETE) {
            System.err.println("Failed to create GUI framebuffer!");
            GLFW.glfwTerminate();
            System.exit(-1);
        }

        // Prepare shader program
        this.shaderProgramHandle = GL33.glCreateProgram();
        int vertexShaderHandle = GL33.glCreateShader(GL33.GL_VERTEX_SHADER);
        int fragmentShaderHandle = GL33.glCreateShader(GL33.GL_FRAGMENT_SHADER);
        int geometryShaderHandle = GL33.glCreateShader(GL33.GL_GEOMETRY_SHADER);
        try {
            GL33.glShaderSource(vertexShaderHandle, this.readShaderSource("shaders/gui/gui.vsh"));
            GL33.glShaderSource(fragmentShaderHandle, this.readShaderSource("shaders/gui/gui.fsh"));
            GL33.glShaderSource(geometryShaderHandle, this.readShaderSource("shaders/gui/gui.gsh"));
        } catch (IOException ex) {
            System.err.println("Failed to load gui shader source!");
            GLFW.glfwTerminate();
            System.exit(-1);
        }
        this.initialized = true;
    }

    private void render() {
        this.init();

        GL33.glBindFramebuffer(GL33.GL_FRAMEBUFFER, this.frameBufferHandle);
        GL33.glViewport(0, 0, this.size.get(0), this.size.get(1));
    }

    private CharSequence readShaderSource(String path) throws IOException {
        InputStream is = this.getClass().getResourceAsStream(path);
        if (is == null) {
            throw new IOException("Failed to load shader source from path: " + path);
        }
        InputStreamReader isr = new InputStreamReader(is);
        int length = isr.read();
        char[] buffer = new char[length];
        if (isr.read(buffer) != -1) {
            throw new IOException("Failed to read shader source completely: " + path);
        }
        isr.close();
        is.close();
        return new String(buffer);
    }

    @Override
    public void resize(int width, int height) {
        this.size.set(0, width);
        this.size.set(1, height);
    }
}
