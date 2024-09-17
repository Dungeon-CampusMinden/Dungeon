package de.fwatermann.dungine.graphics.shader;

import de.fwatermann.dungine.exception.OpenGLException;
import de.fwatermann.dungine.resource.Resource;
import de.fwatermann.dungine.utils.Disposable;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.opengl.GL33;
import org.lwjgl.opengl.GL40;
import org.lwjgl.opengl.GL43;

/**
 * Represents a shader. This class is used to create and manage OpenGL shaders. Multiple shaders can
 * be combined into a {@link ShaderProgram}.
 */
public class Shader implements Disposable {

  private static final Logger LOGGER = LogManager.getLogger(Shader.class);

  private static final String includeRegex =
      "^\\h*(?<keyword>#include)\\h+((\"?(?<path1>.+)\")|(?<path2>.+))$";
  private static final Pattern includePattern = Pattern.compile(includeRegex, Pattern.MULTILINE);

  private static final String versionRegex = "^\\h*(?<keyword>#version)\\h+.*$";
  private static final Pattern versionPattern = Pattern.compile(versionRegex, Pattern.MULTILINE);

  private static final String compileErrorRegex = "^(?<file>[0-9]+)\\((?<line>[0-9]+)\\)(?<message>.*)$";
  private static final Pattern compileErrorPattern = Pattern.compile(compileErrorRegex, Pattern.MULTILINE);

  /**
   * Enumeration of shader types supported by this class. Each shader type is associated with its
   * OpenGL shader type constant, and the minimum OpenGL version required to support that shader
   * type.
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
     * Checks if the current OpenGL context meets the minimum version requirement for this shader
     * type. Throws an OpenGLException if the requirement is not met.
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
   * Loads a shader from a resource. The resource is read into a ByteBuffer, and then a new Shader
   * instance is created with the shader code and type specified. If <code>#include "path"</code> is
   * used in this shader source it will be replaced with the contents of the file at the provided
   * path.
   *
   * @param resource The resource containing the shader code.
   * @param shaderType The type of the shader to be loaded.
   * @return A new Shader instance with the specified source code and shader type.
   * @throws IOException If an I/O error occurs while reading the shader code from the resource.
   */
  public static Shader loadShader(Resource resource, ShaderType shaderType) throws IOException {
    String sourceCode = loadSourceCode(resource);

    // Parse the source code to add includes
    Matcher matcher = includePattern.matcher(sourceCode);
    while (matcher.find()) {
      String includePath = matcher.group("path1");
      if (includePath == null) {
        includePath = matcher.group("path2");
      }
      LOGGER.debug("Including: {} into {}", includePath, resource);
      Resource includeResource = resource.resolveRelative(includePath);
      if (includeResource == null) {
        throw new IOException("Failed to resolve include path: " + includePath);
      }
      String includeSourceCode = loadSourceCode(includeResource);
      includeSourceCode = removeVersionDirective(includeSourceCode);
      sourceCode = sourceCode.replace(matcher.group(), includeSourceCode);
      matcher = includePattern.matcher(sourceCode);
    }

    return new Shader(sourceCode, shaderType);
  }

  private static String removeVersionDirective(String sourceCode) {
    Matcher matcher = versionPattern.matcher(sourceCode);
    if (matcher.find()) {
      sourceCode = sourceCode.replace(matcher.group(), "");
    }
    return sourceCode;
  }

  private static String loadSourceCode(Resource resource) throws IOException {
    ByteBuffer data = resource.readBytes();
    if (data.hasArray()) {
      return new String(data.array());
    } else {
      data.position(0);
      byte[] bytes = new byte[data.capacity()];
      data.get(bytes);
      return new String(bytes);
    }
  }

  private final String sourceCode;
  private int shaderType = 0;
  private int glHandle = 0;

  /**
   * Constructs a new Shader with the specified source code and shader type. This constructor also
   * checks if the current OpenGL context is compatible with the shader type and initializes the
   * shader in OpenGL.
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
      String errorLog = GL33.glGetShaderInfoLog(this.glHandle);

      StringBuilder errorOutput = new StringBuilder();

      Matcher matcher = compileErrorPattern.matcher(errorLog);
      while (matcher.find()) {
        int file = Integer.parseInt(matcher.group("file"));
        int line = Integer.parseInt(matcher.group("line"));
        String message = matcher.group("message");
        String[] lines = this.sourceCode.split("\n");
        errorOutput
            .append(file)
            .append(".")
            .append(line)
            .append(": ")
            .append(message)
            .append("\n");

        for(int i = Math.max(0, line - 3); i < Math.min(lines.length, line + 3); i ++) {
          String sourceCodeLine = lines[i];
          if(i == line - 1) {
            errorOutput.append(">> ");
          } else {
            errorOutput.append("   ");
          }
          errorOutput.append(i + 1).append(": ").append(sourceCodeLine).append("\n");
        }
      }
      LOGGER.error(errorLog);
      throw new OpenGLException("Failed to compile shader: \n" + errorOutput);
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
