package de.fwatermann.dungine.graphics.shader;

import de.fwatermann.dungine.exception.OpenGLException;
import de.fwatermann.dungine.graphics.camera.Camera;
import de.fwatermann.dungine.graphics.camera.CameraPerspective;
import de.fwatermann.dungine.graphics.scene.model.Material;
import de.fwatermann.dungine.graphics.texture.animation.Animation;
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
import org.lwjgl.opengl.ARBSeparateShaderObjects;
import org.lwjgl.opengl.GL;
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

  /**
   * Flag to force to not use separate shader objects. This can be useful for debugging purposes or
   * when separate shader objects are not supported by the OpenGL implementation.
   */
  public static boolean FORCE_NO_SEPARAT_SHADER_OBJECT = false;

  private static final Logger LOGGER = LogManager.getLogger(ShaderProgram.class);

  private int glHandle;
  private final Shader[] shaders;
  private final Map<String, Integer> uniformLocations = new HashMap<>();
  private final Map<String, Integer> attributeLocations = new HashMap<>();
  private final Map<String, Integer> uniformBlockIndices = new HashMap<>();
  private ShaderProgramConfiguration configuration = new ShaderProgramConfiguration();

  /**
   * Creates a new shader program with the specified shaders.
   * @param shaders the shaders to use in the program
   */
  public ShaderProgram(Shader... shaders) {
    this.shaders = shaders;
    this.glInit();
  }

  private static boolean supportsSSO() {
    return !FORCE_NO_SEPARAT_SHADER_OBJECT && GL.getCapabilities().GL_ARB_separate_shader_objects;
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
   *
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

  /**
   * Returns the index of the uniform block with the specified name.
   * @param name the name of the uniform block
   * @return the index of the uniform block
   */
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
    if (this.bound()) {
      GL33.glUniform1i(this.getUniformLocation(name), value);
    } else if (supportsSSO()) {
      ARBSeparateShaderObjects.glProgramUniform1i(
          this.glHandle, this.getUniformLocation(name), value);
    } else {
      throw new OpenGLException(
          "Cannot set uniform value when shader program is not bound and separate shader objects are not supported!");
    }
  }

  /**
   * Sets the uniform with the specified name to the specified value.
   *
   * @param name the name of the uniform
   * @param value the value of the uniform
   */
  public void setUniform1f(String name, float value) {
    if (this.bound()) {
      GL33.glUniform1f(this.getUniformLocation(name), value);
    } else if (supportsSSO()) {
      ARBSeparateShaderObjects.glProgramUniform1f(
          this.glHandle, this.getUniformLocation(name), value);
    } else {
      throw new OpenGLException(
          "Cannot set uniform value when shader program is not bound and separate shader objects are not supported!");
    }
  }

  /**
   * Sets the uniform with the specified name to the specified values.
   *
   * @param name the name of the uniform
   * @param values the values of the uniform
   */
  public void setUniform1iv(String name, int[] values) {
    if (this.bound()) {
      GL33.glUniform1iv(this.getUniformLocation(name), values);
    } else if (supportsSSO()) {
      ARBSeparateShaderObjects.glProgramUniform1iv(
          this.glHandle, this.getUniformLocation(name), values);
    } else {
      throw new OpenGLException(
          "Cannot set uniform value when shader program is not bound and separate shader objects are not supported!");
    }
  }

  /**
   * Sets the uniform with the specified name to the specified value.
   *
   * @param name the name of the uniform
   * @param x the x component of the value
   * @param y the y component of the value
   */
  public void setUniform2f(String name, float x, float y) {
    if (this.bound()) {
      GL33.glUniform2f(this.getUniformLocation(name), x, y);
    } else if (supportsSSO()) {
      ARBSeparateShaderObjects.glProgramUniform2f(
          this.glHandle, this.getUniformLocation(name), x, y);
    } else {
      throw new OpenGLException(
          "Cannot set uniform value when shader program is not bound and separate shader objects are not supported!");
    }
  }

  /**
   * Sets the uniform with the specified name to the specified value.
   *
   * @param name the name of the uniform
   * @param value the value of the uniform
   */
  public void setUniform2f(String name, Vector2f value) {
    if (this.bound()) {
      GL33.glUniform2f(this.getUniformLocation(name), value.x, value.y);
    } else if (supportsSSO()) {
      ARBSeparateShaderObjects.glProgramUniform2f(
          this.glHandle, this.getUniformLocation(name), value.x, value.y);
    } else {
      throw new OpenGLException(
          "Cannot set uniform value when shader program is not bound and separate shader objects are not supported!");
    }
  }

  /**
   * Sets the uniform with the specified name to the specified values.
   * @param name the name of the uniform
   * @param x the x component of the value
   * @param y the y component of the value
   */
  public void setUniform2iv(String name, int x, int y) {
    if (this.bound()) {
      GL33.glUniform2i(this.getUniformLocation(name), x, y);
    } else if (supportsSSO()) {
      ARBSeparateShaderObjects.glProgramUniform2i(
          this.glHandle, this.getUniformLocation(name), x, y);
    } else {
      throw new OpenGLException(
          "Cannot set uniform value when shader program is not bound and separate shader objects are not supported!");
    }
  }

  /**
   * Sets the uniform with the specified name to the specified value.
   * @param name the name of the uniform
   * @param value the value of the uniform
   */
  public void setUniform2i(String name, Vector2i value) {
    if (this.bound()) {
      GL33.glUniform2i(this.getUniformLocation(name), value.x, value.y);
    } else if (supportsSSO()) {
      ARBSeparateShaderObjects.glProgramUniform2i(
          this.glHandle, this.getUniformLocation(name), value.x, value.y);
    } else {
      throw new OpenGLException(
          "Cannot set uniform value when shader program is not bound and separate shader objects are not supported!");
    }
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
    if (this.bound()) {
      GL33.glUniform3f(this.getUniformLocation(name), x, y, z);
    } else if (supportsSSO()) {
      ARBSeparateShaderObjects.glProgramUniform3f(
          this.glHandle, this.getUniformLocation(name), x, y, z);
    } else {
      throw new OpenGLException(
          "Cannot set uniform value when shader program is not bound and separate shader objects are not supported!");
    }
  }

  /**
   * Sets the uniform with the specified name to the specified value.
   *
   * @param name the name of the uniform
   * @param value the value of the uniform
   */
  public void setUniform3f(String name, Vector3f value) {
    if (this.bound()) {
      GL33.glUniform3f(this.getUniformLocation(name), value.x, value.y, value.z);
    } else if (supportsSSO()) {
      ARBSeparateShaderObjects.glProgramUniform3f(
          this.glHandle, this.getUniformLocation(name), value.x, value.y, value.z);
    } else {
      throw new OpenGLException(
          "Cannot set uniform value when shader program is not bound and separate shader objects are not supported!");
    }
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
    if (this.bound()) {
      GL33.glUniform3i(this.getUniformLocation(name), x, y, z);
    } else if (supportsSSO()) {
      ARBSeparateShaderObjects.glProgramUniform3i(
          this.glHandle, this.getUniformLocation(name), x, y, z);
    } else {
      throw new OpenGLException(
          "Cannot set uniform value when shader program is not bound and separate shader objects are not supported!");
    }
  }

  /**
   * Sets the uniform with the specified name to the specified value.
   *
   * @param name the name of the uniform
   * @param value the value of the uniform
   */
  public void setUniform3i(String name, Vector3i value) {
    if (this.bound()) {
      GL33.glUniform3i(this.getUniformLocation(name), value.x, value.y, value.z);
    } else if (supportsSSO()) {
      ARBSeparateShaderObjects.glProgramUniform3i(
          this.glHandle, this.getUniformLocation(name), value.x, value.y, value.z);
    } else {
      throw new OpenGLException(
          "Cannot set uniform value when shader program is not bound and separate shader objects are not supported!");
    }
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
    if (this.bound()) {
      GL33.glUniform4f(this.getUniformLocation(name), x, y, z, w);
    } else if (supportsSSO()) {
      ARBSeparateShaderObjects.glProgramUniform4f(
          this.glHandle, this.getUniformLocation(name), x, y, z, w);
    } else {
      throw new OpenGLException(
          "Cannot set uniform value when shader program is not bound and separate shader objects are not supported!");
    }
  }

  /**
   * Sets the uniform with the specified name to the specified value.
   *
   * @param name the name of the uniform
   * @param value the value of the uniform
   */
  public void setUniform4f(String name, Vector4f value) {
    if (this.bound()) {
      GL33.glUniform4f(this.getUniformLocation(name), value.x, value.y, value.z, value.w);
    } else if (supportsSSO()) {
      ARBSeparateShaderObjects.glProgramUniform4f(
          this.glHandle, this.getUniformLocation(name), value.x, value.y, value.z, value.w);
    } else {
      throw new OpenGLException(
          "Cannot set uniform value when shader program is not bound and separate shader objects are not supported!");
    }
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
    if (this.bound()) {
      GL33.glUniform4i(this.getUniformLocation(name), x, y, z, w);
    } else if (supportsSSO()) {
      ARBSeparateShaderObjects.glProgramUniform4i(
          this.glHandle, this.getUniformLocation(name), x, y, z, w);
    } else {
      throw new OpenGLException(
          "Cannot set uniform value when shader program is not bound and separate shader objects are not supported!");
    }
  }

  /**
   * Sets the uniform with the specified name to the specified value.
   *
   * @param name the name of the uniform
   * @param value the value of the uniform
   */
  public void setUniform4i(String name, Vector4i value) {
    if (this.bound()) {
      GL33.glUniform4i(this.getUniformLocation(name), value.x, value.y, value.z, value.w);
    } else if (supportsSSO()) {
      ARBSeparateShaderObjects.glProgramUniform4i(
          this.glHandle, this.getUniformLocation(name), value.x, value.y, value.z, value.w);
    } else {
      throw new OpenGLException(
          "Cannot set uniform value when shader program is not bound and separate shader objects are not supported!");
    }
  }

  /**
   * Sets the uniform with the specified name to the specified value.
   *
   * @param name the name of the uniform
   * @param matrix the value of the uniform
   */
  public void setUniformMatrix4fv(String name, float[] matrix) {
    if (this.bound()) {
      GL33.glUniformMatrix4fv(this.getUniformLocation(name), false, matrix);
    } else if (supportsSSO()) {
      ARBSeparateShaderObjects.glProgramUniformMatrix4fv(
          this.glHandle, this.getUniformLocation(name), false, matrix);
    } else {
      throw new OpenGLException(
          "Cannot set uniform value when shader program is not bound and separate shader objects are not supported!");
    }
  }

  /**
   * Sets the uniform with the specified name to the specified value.
   *
   * @param name the name of the uniform
   * @param matrix the value of the uniform
   */
  public void setUniformMatrix4f(String name, Matrix4f matrix) {
    if (this.bound()) {
      GL33.glUniformMatrix4fv(this.getUniformLocation(name), false, matrix.get(new float[16]));
    } else if (supportsSSO()) {
      ARBSeparateShaderObjects.glProgramUniformMatrix4fv(
          this.glHandle, this.getUniformLocation(name), false, matrix.get(new float[16]));
    } else {
      throw new OpenGLException(
          "Cannot set uniform value when shader program is not bound and separate shader objects are not supported!");
    }
  }

  /**
   * Sets the uniform with the specified name to the specified value.
   *
   * @param name the name of the uniform
   * @param matrix the value of the uniform
   * @param transpose whether the matrix should be transposed
   */
  public void setUniformMatrix4fv(String name, float[] matrix, boolean transpose) {
    if (this.bound()) {
      GL33.glUniformMatrix4fv(this.getUniformLocation(name), transpose, matrix);
    } else if (supportsSSO()) {
      ARBSeparateShaderObjects.glProgramUniformMatrix4fv(
          this.glHandle, this.getUniformLocation(name), transpose, matrix);
    } else {
      throw new OpenGLException(
          "Cannot set uniform value when shader program is not bound and separate shader objects are not supported!");
    }
  }

  /**
   * Sets the uniform with the specified name to the specified value.
   *
   * @param name the name of the uniform
   * @param matrix the value of the uniform
   * @param transpose whether the matrix should be transposed
   */
  public void setUniformMatrix4f(String name, Matrix4f matrix, boolean transpose) {
    if (this.bound()) {
      GL33.glUniformMatrix4fv(this.getUniformLocation(name), transpose, matrix.get(new float[16]));
    } else if (supportsSSO()) {
      ARBSeparateShaderObjects.glProgramUniformMatrix4fv(
          this.glHandle, this.getUniformLocation(name), transpose, matrix.get(new float[16]));
    } else {
      throw new OpenGLException(
          "Cannot set uniform value when shader program is not bound and separate shader objects are not supported!");
    }
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
    if (camera == null) return;
    this.setUniform3f(this.configuration.uniformCameraPosition, camera.position());
    this.setUniformMatrix4f(this.configuration.uniformViewMatrix, camera.viewMatrix());
    this.setUniformMatrix4f(this.configuration.uniformProjectionMatrix, camera.projectionMatrix());
    if (camera instanceof CameraPerspective cp) {
      this.setUniform1f(this.configuration.uniformCameraPerspectiveNearPlane, cp.nearPlane());
      this.setUniform1f(this.configuration.uniformCameraPerspectiveFarPlane, cp.farPlane());
    }
  }

  /**
   * Uses the specified animation by binding it to this shader program.
   *
   * @param animation the animation to use
   */
  public void useAnimation(Animation animation) {
    animation.bind(this);
  }

  /**
   * Uses the specified animation by binding it to this shader program with the given texture unit.
   *
   * @param animation the animation to use
   * @param textureUnit the texture unit to bind the animation to
   */
  public void useAnimation(Animation animation, int textureUnit) {
    animation.bind(this, textureUnit);
  }

  /**
   * Uses the specified material by setting its properties as uniforms in this shader program.
   * @param material the material to use
   */
  public void useMaterial(Material material) {
    this.setUniform4f(this.configuration.material.diffuseColor(), material.diffuseColor);
    this.setUniform4f(this.configuration.material.ambientColor(), material.ambientColor);
    this.setUniform4f(this.configuration.material.specularColor(), material.specularColor);
    this.setUniform1i(this.configuration.material.diffuseTexture(), 0);
    this.setUniform1i(this.configuration.material.ambientTexture(), 1);
    this.setUniform1i(this.configuration.material.specularTexture(), 2);
    this.setUniform1i(this.configuration.material.normalTexture(), 3);
    this.setUniform1i(this.configuration.material.flags(), material.flags);
  }

  /**
   * Returns the configuration of this shader program.
   *
   * @return the configuration of this shader program
   */
  public ShaderProgramConfiguration configuration() {
    return this.configuration;
  }

  /**
   * Sets the configuration of this shader program.
   *
   * @param configuration the configuration of this shader program
   * @return the current instance of this shader program
   */
  public ShaderProgram configuration(ShaderProgramConfiguration configuration) {
    this.configuration = configuration;
    return this;
  }

  @Override
  public void dispose() {
    GL33.glDeleteProgram(this.glHandle);
  }
}
