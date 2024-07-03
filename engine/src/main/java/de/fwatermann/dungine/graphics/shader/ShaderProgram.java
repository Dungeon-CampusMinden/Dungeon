package de.fwatermann.dungine.graphics.shader;

import de.fwatermann.dungine.exception.OpenGLException;
import de.fwatermann.dungine.graphics.Camera;
import de.fwatermann.dungine.utils.Disposable;
import de.fwatermann.dungine.utils.GLUtils;
import java.util.HashMap;
import java.util.Map;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.opengl.GL30;
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

  private int glHandle;
  private final Shader[] shaders;
  private final Map<String, Integer> uniformLocations = new HashMap<>();
  private final Map<String, Integer> attributeLocations = new HashMap<>();

  public ShaderProgram(Shader... shaders) {
    this.shaders = shaders;
    this.glInit();
  }

  private void glInit() {
    this.glHandle = GL30.glCreateProgram();
    for (Shader shader : this.shaders) {
      GL30.glAttachShader(this.glHandle, shader.glHandle());
    }
    GL30.glLinkProgram(this.glHandle);
    if (GL30.glGetProgrami(this.glHandle, GL30.GL_LINK_STATUS) == GL30.GL_FALSE) {
      throw new OpenGLException(
          "Failed to link shader program: " + GL30.glGetProgramInfoLog(this.glHandle));
    } else {
      System.out.println("Successfully linked shader program: " + this.glHandle);
    }
    GLUtils.checkGLError();
  }

  /** Binds this shader program. */
  public void bind() {
    GL30.glUseProgram(this.glHandle);
  }

  /** Unbinds this shader program. */
  public void unbind() {
    GL30.glUseProgram(0);
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
          return GL30.glGetUniformLocation(this.glHandle, k);
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
          return GL30.glGetAttribLocation(this.glHandle, k);
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
   * Sets the uniform with the specified name to the specified value.
   *
   * @param name the name of the uniform
   * @param x the x component of the value
   * @param y the y component of the value
   */
  public void setUniform2fv(String name, float x, float y) {
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
   * Sets the view and projection matrices of the specified camera as uniforms in this shader
   * program. The specified names are used for the view and projection matrices.
   *
   * @param camera the camera to use
   * @param viewMatrixName the name of the view matrix uniform
   * @param projectionMatrixName the name of the projection matrix uniform
   */
  public void useCamera(Camera camera, String viewMatrixName, String projectionMatrixName) {
    this.setUniformMatrix4f(viewMatrixName, camera.viewMatrix());
    this.setUniformMatrix4f(projectionMatrixName, camera.projectionMatrix());
  }

  /**
   * Sets the view and projection matrices of the specified camera as uniforms in this shader
   * program. The default uniform "uView" and "uProjection" are used for the view and projection
   * matrices.
   *
   * @param camera the camera to use
   */
  public void useCamera(Camera camera) {
    this.useCamera(camera, "uView", "uProjection");
  }

  @Override
  public void dispose() {
    GL30.glDeleteProgram(this.glHandle);
  }
}
