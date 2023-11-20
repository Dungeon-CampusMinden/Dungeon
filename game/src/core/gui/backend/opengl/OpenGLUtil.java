package core.gui.backend.opengl;

import core.gui.util.Logging;
import core.utils.logging.CustomLogLevel;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL33;
import org.lwjgl.stb.STBImage;
import org.lwjgl.stb.STBImageWrite;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Utility class for OpenGL. */
public class OpenGLUtil {

    private static final SimpleDateFormat SIMPLE_DATE_FORMAT =
            new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss.SSS");

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
                Logging.log(
                        CustomLogLevel.DEBUG,
                        "Successfully linked shader program (%d)",
                        shaderProgramHandle);
            }
        } catch (IOException | OpenGLException ex) {
            Logging.log(
                    CustomLogLevel.ERROR,
                    "Failed to load & compile shader: %s",
                    ex,
                    ex.getMessage());
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
        int[] viewport = new int[4];
        GL33.glGetIntegerv(GL33.GL_VIEWPORT, viewport);
        int width = viewport[2];
        int height = viewport[3];
        ByteBuffer buffer = ByteBuffer.allocateDirect(width * height * 4);
        GL33.glReadPixels(0, 0, width, height, GL33.GL_RGBA, GL33.GL_UNSIGNED_BYTE, buffer);

        File screenshotDirectory = new File("screenshots");
        if (!screenshotDirectory.exists()) {
            screenshotDirectory.mkdir();
        }
        String fileName =
                screenshotDirectory.getAbsolutePath()
                        + File.separator
                        + SIMPLE_DATE_FORMAT.format(System.currentTimeMillis())
                        + ".png";

        STBImageWrite.stbi_flip_vertically_on_write(true);
        STBImageWrite.stbi_write_png(fileName, width, height, 4, buffer, width * 4);
        return fileName;
    }

    public static int[] getOpenGLVersion() {
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
        return new int[] {major, minor, patch};
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
    public static int createTextureFromMemoryImage(
            byte[] imageFileBytes, int[] width, int[] height, int[] channels) {
        ByteBuffer bytes = ByteBuffer.allocateDirect(imageFileBytes.length);
        bytes.put(imageFileBytes);
        bytes.position(0);
        STBImage.stbi_set_flip_vertically_on_load(true);
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
     * Create a texture from a byte array (bitmap).
     *
     * @param bitmap Byte array containing the bitmap.
     * @param width Width of the bitmap.
     * @param height Height of the bitmap.
     * @param channels Number of channels in the bitmap.
     * @param magFilter Magnification filter.
     * @param minFilter Minification filter.
     * @param wrapS Wrapping behavior for texture coordinate S.
     * @param wrapT Wrapping behavior for texture coordinate T.
     * @return Texture handle.
     */
    public static int createTextureFromMemoryBitmap(
            byte[] bitmap,
            int width,
            int height,
            int channels,
            int magFilter,
            int minFilter,
            int wrapS,
            int wrapT) {

        ByteBuffer buffer = ByteBuffer.allocateDirect(bitmap.length);
        buffer.put(bitmap);
        buffer.position(0);

        int textureHandle = GL33.glGenTextures();
        GL33.glBindTexture(GL33.GL_TEXTURE_2D, textureHandle);

        switch (channels) {
            case 1:
                GL33.glTexImage2D(
                        GL33.GL_TEXTURE_2D,
                        0,
                        GL33.GL_RED,
                        width,
                        height,
                        0,
                        GL33.GL_RED,
                        GL33.GL_UNSIGNED_BYTE,
                        buffer);
                break;
            case 2:
                GL33.glTexImage2D(
                        GL33.GL_TEXTURE_2D,
                        0,
                        GL33.GL_RG,
                        width,
                        height,
                        0,
                        GL33.GL_RG,
                        GL33.GL_UNSIGNED_BYTE,
                        buffer);
            case 3:
                GL33.glTexImage2D(
                        GL33.GL_TEXTURE_2D,
                        0,
                        GL33.GL_RGB,
                        width,
                        height,
                        0,
                        GL33.GL_RGB,
                        GL33.GL_UNSIGNED_BYTE,
                        buffer);
                break;
            case 4:
                GL33.glTexImage2D(
                        GL33.GL_TEXTURE_2D,
                        0,
                        GL33.GL_RGBA,
                        width,
                        height,
                        0,
                        GL33.GL_RGBA,
                        GL33.GL_UNSIGNED_BYTE,
                        buffer);
                break;
            default:
                throw new RuntimeException("Unsupported number of channels: " + channels);
        }
        GL33.glTexParameteri(GL33.GL_TEXTURE_2D, GL33.GL_TEXTURE_MAG_FILTER, magFilter);
        GL33.glTexParameteri(GL33.GL_TEXTURE_2D, GL33.GL_TEXTURE_MIN_FILTER, minFilter);
        GL33.glTexParameteri(GL33.GL_TEXTURE_2D, GL33.GL_TEXTURE_WRAP_S, wrapS);
        GL33.glTexParameteri(GL33.GL_TEXTURE_2D, GL33.GL_TEXTURE_WRAP_T, wrapT);

        GL33.glBindTexture(GL33.GL_TEXTURE_2D, 0);

        return textureHandle;
    }

    /**
     * Create a texture from a byte array (bitmap).
     *
     * <p>Uses GL_NEAREST for magnification and minification filter, and GL_CLAMP_TO_EDGE for
     * wrapping.
     *
     * @param bitmap Byte array containing the bitmap.
     * @param width Width of the bitmap.
     * @param height Height of the bitmap.
     * @param channels Number of channels in the bitmap.
     * @return Texture handle.
     */
    public static int createTextureFromMemoryBitmap(
            byte[] bitmap, int width, int height, int channels) {
        return createTextureFromMemoryBitmap(
                bitmap,
                width,
                height,
                channels,
                GL33.GL_NEAREST,
                GL33.GL_NEAREST,
                GL33.GL_CLAMP_TO_EDGE,
                GL33.GL_CLAMP_TO_EDGE);
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
}