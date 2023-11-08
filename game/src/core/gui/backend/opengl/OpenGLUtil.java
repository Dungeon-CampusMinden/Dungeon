package core.gui.backend.opengl;

import core.utils.logging.CustomLogLevel;

import org.lwjgl.opengl.GL33;
import org.lwjgl.stb.STBImage;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import java.util.logging.Logger;

/** Utility class for OpenGL. */
public class OpenGLUtil {

    private static final Logger LOGGER = Logger.getLogger(OpenGLUtil.class.getName());

    /**
     * Load a shader source from a resource file.
     *
     * @param path Path to the resource file.
     * @return Shader source as a CharSequence.
     * @throws IOException If the resource file could not be loaded.
     */
    public static CharSequence readShaderSource(String path) throws IOException {
        InputStream is = OpenGLUtil.class.getResourceAsStream(path);
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

    /**
     * Create a shader program from a geometry shader, vertex shader and fragment shader.
     *
     * @param geometryShader Geometry shader path.
     * @param vertexShader Vertex shader path.
     * @param fragmentShader Fragment shader path.
     * @return Shader program handle.
     */
    public static int makeShaderProgram(
            String geometryShader, String vertexShader, String fragmentShader) {
        int geometryShaderHandle =
                geometryShader != null ? GL33.glCreateShader(GL33.GL_GEOMETRY_SHADER) : -1;
        int vertexShaderHandle =
                vertexShader != null ? GL33.glCreateShader(GL33.GL_VERTEX_SHADER) : -1;
        int fragmentShaderHandle =
                fragmentShader != null ? GL33.glCreateShader(GL33.GL_FRAGMENT_SHADER) : -1;

        int shaderProgramHandle = GL33.glCreateProgram();
        try {
            if (geometryShaderHandle != -1) {
                GL33.glShaderSource(
                        geometryShaderHandle, OpenGLUtil.readShaderSource(geometryShader));
                OpenGLUtil.compileShader(geometryShaderHandle);
                GL33.glAttachShader(shaderProgramHandle, geometryShaderHandle);
            }
            if (vertexShaderHandle != -1) {
                GL33.glShaderSource(vertexShaderHandle, OpenGLUtil.readShaderSource(vertexShader));
                OpenGLUtil.compileShader(vertexShaderHandle);
                GL33.glAttachShader(shaderProgramHandle, vertexShaderHandle);
            }
            if (fragmentShaderHandle != -1) {
                GL33.glShaderSource(
                        fragmentShaderHandle, OpenGLUtil.readShaderSource(fragmentShader));
                OpenGLUtil.compileShader(fragmentShaderHandle);
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

    /**
     * Create a shader program from a vertex shader and fragment shader.
     *
     * @param vertexShader Vertex shader path.
     * @param fragmentShader Fragment shader path.
     * @return Shader program handle.
     */
    public static int makeShaderProgram(String vertexShader, String fragmentShader) {
        return makeShaderProgram(null, vertexShader, fragmentShader);
    }

    /**
     * Compile a shader.
     *
     * @param shaderHandle Shader handle.
     * @throws OpenGLException If the shader failed to compile.
     */
    public static void compileShader(int shaderHandle) throws OpenGLException {
        GL33.glCompileShader(shaderHandle);
        if (GL33.glGetShaderi(shaderHandle, GL33.GL_COMPILE_STATUS) != GL33.GL_TRUE) {
            throw new OpenGLException(
                    "Failed to compile shader: " + GL33.glGetShaderInfoLog(shaderHandle));
        }
    }

    /**
     * Takes a screenshot and saves it to a file.
     *
     * @return Path to the screenshot file.
     */
    public static String screenshot() {
        // TODO: Implement screenshot functionality
        return null;
    }

    /**
     * Load a resource file as a byte array.
     *
     * @param path Path to the resource file.
     * @return Resource file as a byte array.
     * @throws IOException If the resource file could not be loaded.
     */
    public static byte[] loadResource(String path) throws IOException {
        InputStream is = OpenGLUtil.class.getResourceAsStream(path);
        try (is) {
            if (is == null) {
                throw new IOException("Image not found at: " + path);
            }
            return is.readAllBytes();
        }
    }

    /**
     * Create a texture from a byte array.
     *
     * @param imageFileBytes Image file as a byte array.
     * @param width Returns width of the image.
     * @param height Returns height of the image.
     * @param channels Returns number of channels in the image.
     * @return Texture handle.
     */
    public static int createTexture(
            byte[] imageFileBytes, int[] width, int[] height, int[] channels) {
        ByteBuffer bytes = ByteBuffer.allocateDirect(imageFileBytes.length);
        bytes.put(imageFileBytes);
        bytes.position(0);
        ByteBuffer rawImage = STBImage.stbi_load_from_memory(bytes, width, height, channels, 4);
        if (rawImage == null) {
            throw new RuntimeException("Failed to load image: " + STBImage.stbi_failure_reason());
        }

        int textureHandle = GL33.glGenTextures();
        GL33.glBindTexture(GL33.GL_TEXTURE_2D, textureHandle);
        GL33.glTexImage2D(
                GL33.GL_TEXTURE_2D,
                0,
                GL33.GL_RGBA,
                width[0],
                height[0],
                0,
                GL33.GL_RGBA,
                GL33.GL_UNSIGNED_BYTE,
                rawImage);
        GL33.glTexParameteri(GL33.GL_TEXTURE_2D, GL33.GL_TEXTURE_MIN_FILTER, GL33.GL_NEAREST);
        GL33.glTexParameteri(GL33.GL_TEXTURE_2D, GL33.GL_TEXTURE_MAG_FILTER, GL33.GL_NEAREST);
        GL33.glTexParameteri(GL33.GL_TEXTURE_2D, GL33.GL_TEXTURE_WRAP_S, GL33.GL_CLAMP_TO_EDGE);
        GL33.glTexParameteri(GL33.GL_TEXTURE_2D, GL33.GL_TEXTURE_WRAP_T, GL33.GL_CLAMP_TO_EDGE);

        return textureHandle;
    }

    /**
     * Create an empty texture with the specified width and height.
     *
     * @param width Width of the empty texture.
     * @param height Height of the empty texture.
     * @return Texture handle.
     */
    public static int createEmptyTexture(int width, int height) {
        int textureHandle = GL33.glGenTextures();
        GL33.glBindTexture(GL33.GL_TEXTURE_2D, textureHandle);
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
        GL33.glTexParameteri(GL33.GL_TEXTURE_2D, GL33.GL_TEXTURE_MIN_FILTER, GL33.GL_NEAREST);
        GL33.glTexParameteri(GL33.GL_TEXTURE_2D, GL33.GL_TEXTURE_MAG_FILTER, GL33.GL_NEAREST);
        GL33.glTexParameteri(GL33.GL_TEXTURE_2D, GL33.GL_TEXTURE_WRAP_S, GL33.GL_CLAMP_TO_EDGE);
        GL33.glTexParameteri(GL33.GL_TEXTURE_2D, GL33.GL_TEXTURE_WRAP_T, GL33.GL_CLAMP_TO_EDGE);
        return textureHandle;
    }

    /**
     * Log a message to the console and the logger.
     *
     * @param level Log level.
     * @param format Message format.
     * @param args Message arguments.
     */
    public static void log(Level level, String format, Object... args) {
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

    /**
     * Log a message to the console and the logger.
     *
     * @param level Log level.
     * @param format Message format.
     * @param throwable Throwable to log.
     * @param args Message arguments.
     */
    public static void log(Level level, String format, Throwable throwable, Object... args) {
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
