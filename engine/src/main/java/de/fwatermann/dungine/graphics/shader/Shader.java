package de.fwatermann.dungine.graphics.shader;

import de.fwatermann.dungine.exception.OpenGLException;
import de.fwatermann.dungine.resource.Resource;
import de.fwatermann.dungine.utils.Disposable;
import java.io.IOException;
import java.nio.ByteBuffer;
import org.lwjgl.opengl.GL33;
import org.lwjgl.opengl.GL40;
import org.lwjgl.opengl.GL43;

/**
 * Represents a shader. This class is used to create and manage OpenGL shaders. Multiple shaders can
 * be combined into a {@link ShaderProgram}.
 */
public class Shader implements Disposable {

  /**
   * Enumeration of shader types supported by this class. Each shader type is associated with its OpenGL shader type constant,
   * and the minimum OpenGL version required to support that shader type.
   */
  public enum ShaderType {
    VERTEX_SHADER(GL33.GL_VERTEX_SHADER, 3, 3),
    FRAGMENT_SHADER(GL33.GL_FRAGMENT_SHADER, 3, 3),
    GEOMETRY_SHADER(GL33.GL_GEOMETRY_SHADER, 3, 3),
    TESS_CONTROL_SHADER(GL40.GL_TESS_CONTROL_SHADER, 4, 0),
    TESS_EVALUATION_SHADER(GL40.GL_TESS_EVALUATION_SHADER, 4, 0),
    COMPUTE_SHADER(GL43.GL_COMPUTE_SHADER, 4, 3);
    // ...

    private final int glType;
    private final int major;
    private final int minor;

    ShaderType(int glType, int major, int minor) {
      this.glType = glType;
      this.major = major;
      this.minor = minor;
    }

    /**
     * Checks if the current OpenGL context meets the minimum version requirement for this shader type.
     * Throws an OpenGLException if the requirement is not met.
     */
    private void compatible() {
      int glMajor = GL33.glGetInteger(GL33.GL_MAJOR_VERSION);
      int glMinor = GL33.glGetInteger(GL33.GL_MINOR_VERSION);
      if (glMajor < this.major || (glMajor == this.major && glMinor < this.minor)) {
        throw new OpenGLException(
            String.format(
                "OpenGL version %d.%d or higher is required for this shader type (%s).",
                this.major, this.minor, this.name()));
      }
    }
  }

  /**
   * Loads a shader from a resource. The resource is read into a ByteBuffer, and then a new Shader instance is created
   * with the shader code and type specified.
   *
   * @param resource The resource containing the shader code.
   * @param shaderType The type of the shader to be loaded.
   * @return A new Shader instance with the specified source code and shader type.
   * @throws IOException If an I/O error occurs while reading the shader code from the resource.
   */
  public static Shader loadShader(Resource resource, ShaderType shaderType) throws IOException {
    ByteBuffer data = resource.readBytes();
    if (data.hasArray()) {
      return new Shader(new String(data.array()), shaderType);
    } else {
      data.position(0);
      byte[] bytes = new byte[data.capacity()];
      data.get(bytes);
      return new Shader(new String(bytes), shaderType);
    }
  }

  private final String sourceCode;
  private int shaderType = 0;
  private int glHandle = 0;

  /**
   * Constructs a new Shader with the specified source code and shader type.
   * This constructor also checks if the current OpenGL context is compatible with the shader type
   * and initializes the shader in OpenGL.
   *
   * @param sourceCode The source code of the shader.
   * @param shaderType The type of the shader.
   */
  public Shader(String sourceCode, ShaderType shaderType) {
    this.sourceCode = sourceCode;
    this.shaderType = shaderType.glType;
    shaderType.compatible();
    this.initGL();
  }

  private void initGL() {
    this.glHandle = GL33.glCreateShader(this.shaderType);
    GL33.glShaderSource(this.glHandle, this.sourceCode);
    GL33.glCompileShader(this.glHandle);
    if (GL33.glGetShaderi(this.glHandle, GL33.GL_COMPILE_STATUS) == GL33.GL_FALSE) {
      throw new OpenGLException(
          "Failed to compile shader: " + GL33.glGetShaderInfoLog(this.glHandle));
    }
  }

  /**
   * Get the OpenGL handle of this shader.
   *
   * @return the OpenGL handle
   */
  public int glHandle() {
    return this.glHandle;
  }

  /** Disposes of this shader. */
  @Override
  public void dispose() {
    GL33.glDeleteShader(this.glHandle);
  }
}
