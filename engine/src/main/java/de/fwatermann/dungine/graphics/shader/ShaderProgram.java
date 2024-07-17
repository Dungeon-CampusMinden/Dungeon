package de.fwatermann.dungine.graphics.shader;

import de.fwatermann.dungine.exception.OpenGLException;
import de.fwatermann.dungine.graphics.camera.Camera;
import de.fwatermann.dungine.utils.Disposable;
import de.fwatermann.dungine.utils.GLUtils;
import java.util.HashMap;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector2i;
import org.joml.Vector3f;
import org.joml.Vector3i;
import org.joml.Vector4f;
import org.joml.Vector4i;
import org.lwjgl.opengl.GL33;

/**
 * Represents an OpenGL Shader Program, encapsulating the functionality for creating, managing, and
 * using shaders. This class provides a high-level interface for working with OpenGL shader
 * programs, including compiling shaders, linking them into a program, and setting uniform
 * variables. It supports various types of uniform variables, such as integers, floats, vectors, and
 * matrices, allowing for a wide range of graphical effects and transformations. The class also
 * implements the Disposable interface to ensure proper resource management by allowing for the
 * cleanup of the OpenGL program when it is no longer needed.
 */
public class ShaderProgram implements Disposable {

  private static final Logger LOGGER = LogManager.getLogger(ShaderProgram.class);

  private int glHandle;
  private final Shader[] shaders;
  private final Map<String, Integer> uniformLocations = new HashMap<>();
  private final Map<String, Integer> attributeLocations = new HashMap<>();
  private final Map<String, Integer> uniformBlockIndices = new HashMap<>();

  private ShaderProgramConfiguration configuration = new ShaderProgramConfiguration();

  public ShaderProgram(Shader... shaders) {
    this.shaders = shaders;
    this.glInit();
  }

  private void glInit() {
    this.glHandle = GL33.glCreateProgram();
    for (Shader shader : this.shaders) {
      GL33.glAttachShader(this.glHandle, shader.glHandle());
    }
    GL33.glLinkProgram(this.glHandle);
    if (GL33.glGetProgrami(this.glHandle, GL33.GL_LINK_STATUS) == GL33.GL_FALSE) {
      throw new OpenGLException(
          "Failed to link shader program: " + GL33.glGetProgramInfoLog(this.glHandle));
    } else {
      System.out.println("Successfully linked shader program: " + this.glHandle);
    }
    GLUtils.checkError();
  }

  /** Binds this shader program. */
  public void bind() {
    GL33.glUseProgram(this.glHandle);
  }

  /** Unbinds this shader program. */
  public void unbind() {
    GL33.glUseProgram(0);
  }

  /**
   * Checks if this shader program is currently bound.
   * @return true if this shader program is currently bound, false otherwise
   */
  public boolean bound() {
    return GL33.glGetInteger(GL33.GL_CURRENT_PROGRAM) == this.glHandle;
  }

  /**
   * Returns the OpenGL handle of this shader program.
   *
   * @return the OpenGL handle of this shader program
   */
  public int glHandle() {
    return this.glHandle;
  }

  /**
   * Returns the location of the uniform with the specified name.
   *
   * @param name the name of the uniform
   * @return the location of the uniform
   */
  public int getUniformLocation(String name) {
    return this.uniformLocations.computeIfAbsent(
        name,
        k -> {
          int loc = GL33.glGetUniformLocation(this.glHandle, k);
          if (loc == -1) {
            LOGGER.warn("Uniform '{}' not found in shader program", k);
          }
          return loc;
        });
  }

  /**
   * Returns the location of the attribute with the specified name.
   *
   * @param name the name of the attribute
   * @return the location of the attribute
   */
  public int getAttributeLocation(String name) {
    return this.attributeLocations.computeIfAbsent(
        name,
        k -> {
          int loc = GL33.glGetAttribLocation(this.glHandle, k);
          if (loc == -1) {
            LOGGER.warn("Attribute '{}' not found in shader program", k);
          }
          return loc;
        });
  }

  public int getUniformBlockIndex(String name) {
    return this.uniformBlockIndices.computeIfAbsent(
        name,
        k -> {
          int index = GL33.glGetUniformBlockIndex(this.glHandle, k);
          if (index == -1) {
            LOGGER.warn("Uniform block '{}' not found in shader program", k);
          }
          return index;
        });
  }

  /**
   * Sets the uniform with the specified name to the specified value.
   *
   * @param name the name of the uniform
   * @param value the value of the uniform
   */
  public void setUniform1i(String name, int value) {
    GL33.glUniform1i(this.getUniformLocation(name), value);
  }

  /**
   * Sets the uniform with the specified name to the specified value.
   *
   * @param name the name of the uniform
   * @param value the value of the uniform
   */
  public void setUniform1f(String name, float value) {
    GL33.glUniform1f(this.getUniformLocation(name), value);
  }

  /**
   * Sets the uniform with the specified name to the specified values.
   *
   * @param name the name of the uniform
   * @param values the values of the uniform
   */
  public void setUniform1iv(String name, int[] values) {
    GL33.glUniform1iv(this.getUniformLocation(name), values);
  }

  /**
   * Sets the uniform with the specified name to the specified value.
   *
   * @param name the name of the uniform
   * @param x the x component of the value
   * @param y the y component of the value
   */
  public void setUniform2f(String name, float x, float y) {
    GL33.glUniform2f(this.getUniformLocation(name), x, y);
  }

  /**
   * Sets the uniform with the specified name to the specified value.
   *
   * @param name the name of the uniform
   * @param value the value of the uniform
   */
  public void setUniform2f(String name, Vector2f value) {
    GL33.glUniform2f(this.getUniformLocation(name), value.x, value.y);
  }

  public void setUniform2iv(String name, int x, int y) {
    GL33.glUniform2i(this.getUniformLocation(name), x, y);
  }

  public void setUniform2i(String name, Vector2i value) {
    GL33.glUniform2i(this.getUniformLocation(name), value.x, value.y);
  }

  /**
   * Sets the uniform with the specified name to the specified value.
   *
   * @param name the name of the uniform
   * @param x the x component of the value
   * @param y the y component of the value
   * @param z the z component of the value
   */
  public void setUniform3fv(String name, float x, float y, float z) {
    GL33.glUniform3f(this.getUniformLocation(name), x, y, z);
  }

  /**
   * Sets the uniform with the specified name to the specified value.
   *
   * @param name the name of the uniform
   * @param value the value of the uniform
   */
  public void setUniform3f(String name, Vector3f value) {
    GL33.glUniform3f(this.getUniformLocation(name), value.x, value.y, value.z);
  }

  /**
   * Sets the uniform with the specified name to the specified value.
   *
   * @param name the name of the uniform
   * @param x the x component of the value
   * @param y the y component of the value
   * @param z the z component of the value
   */
  public void setUniform3iv(String name, int x, int y, int z) {
    GL33.glUniform3i(this.getUniformLocation(name), x, y, z);
  }

  /**
   * Sets the uniform with the specified name to the specified value.
   *
   * @param name the name of the uniform
   * @param value the value of the uniform
   */
  public void setUniform3i(String name, Vector3i value) {
    GL33.glUniform3i(this.getUniformLocation(name), value.x, value.y, value.z);
  }

  /**
   * Sets the uniform with the specified name to the specified value.
   *
   * @param name the name of the uniform
   * @param x the x component of the value
   * @param y the y component of the value
   * @param z the z component of the value
   * @param w the w component of the value
   */
  public void setUniform4fv(String name, float x, float y, float z, float w) {
    GL33.glUniform4f(this.getUniformLocation(name), x, y, z, w);
  }

  /**
   * Sets the uniform with the specified name to the specified value.
   *
   * @param name the name of the uniform
   * @param value the value of the uniform
   */
  public void setUniform4f(String name, Vector4f value) {
    GL33.glUniform4f(this.getUniformLocation(name), value.x, value.y, value.z, value.w);
  }

  /**
   * Sets the uniform with the specified name to the specified value.
   *
   * @param name the name of the uniform
   * @param x the x component of the value
   * @param y the y component of the value
   * @param z the z component of the value
   * @param w the w component of the value
   */
  public void setUniform4iv(String name, int x, int y, int z, int w) {
    GL33.glUniform4i(this.getUniformLocation(name), x, y, z, w);
  }

  /**
   * Sets the uniform with the specified name to the specified value.
   *
   * @param name the name of the uniform
   * @param value the value of the uniform
   */
  public void setUniform4i(String name, Vector4i value) {
    GL33.glUniform4i(this.getUniformLocation(name), value.x, value.y, value.z, value.w);
  }

  /**
   * Sets the uniform with the specified name to the specified value.
   *
   * @param name the name of the uniform
   * @param matrix the value of the uniform
   */
  public void setUniformMatrix4fv(String name, float[] matrix) {
    GL33.glUniformMatrix4fv(this.getUniformLocation(name), false, matrix);
  }

  /**
   * Sets the uniform with the specified name to the specified value.
   *
   * @param name the name of the uniform
   * @param matrix the value of the uniform
   */
  public void setUniformMatrix4f(String name, Matrix4f matrix) {
    GL33.glUniformMatrix4fv(this.getUniformLocation(name), false, matrix.get(new float[16]));
  }

  /**
   * Sets the uniform with the specified name to the specified value.
   *
   * @param name the name of the uniform
   * @param matrix the value of the uniform
   * @param transpose whether the matrix should be transposed
   */
  public void setUniformMatrix4fv(String name, float[] matrix, boolean transpose) {
    GL33.glUniformMatrix4fv(this.getUniformLocation(name), transpose, matrix);
  }

  /**
   * Sets the uniform with the specified name to the specified value.
   *
   * @param name the name of the uniform
   * @param matrix the value of the uniform
   * @param transpose whether the matrix should be transposed
   */
  public void setUniformMatrix4f(String name, Matrix4f matrix, boolean transpose) {
    GL33.glUniformMatrix4fv(this.getUniformLocation(name), transpose, matrix.get(new float[16]));
  }

  /**
   * Sets the binding of the uniform block with the specified name to the specified binding.
   *
   * @param name the name of the uniform block
   * @param binding the binding of the uniform block
   */
  public void setUniformBlockBinding(String name, int binding) {
    GL33.glUniformBlockBinding(this.glHandle, this.getUniformBlockIndex(name), binding);
  }

  /**
   * Sets the view and projection matrices of the specified camera as uniforms in this shader
   * program. The uniform names for the view and projection matrices are specified in this shader
   * programs configuration. {@link ShaderProgramConfiguration} {@link #configuration()}
   *
   * @param camera the camera to use
   */
  public void useCamera(Camera<?> camera) {
    this.setUniformMatrix4f(this.configuration.uniformViewMatrix, camera.viewMatrix());
    this.setUniformMatrix4f(this.configuration.uniformProjectionMatrix, camera.projectionMatrix());
  }

  /**
   * Returns the configuration of this shader program.
   *
   * @return the configuration of this shader program
   */
  public ShaderProgramConfiguration configuration() {
    return this.configuration;
  }

  @Override
  public void dispose() {
    GL33.glDeleteProgram(this.glHandle);
  }
}
