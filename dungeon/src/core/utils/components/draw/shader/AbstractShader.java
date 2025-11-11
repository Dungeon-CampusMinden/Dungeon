package core.utils.components.draw.shader;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Disposable;
import java.util.*;

/**
 * Base abstract class for defining custom shader passes in the ECS rendering pipeline. Handles
 * lazy, static compilation of the ShaderProgram and binding of uniforms.
 */
public abstract class AbstractShader implements Disposable {

  // Map to cache compiled ShaderPrograms (static, shared across all instances)
  // Key: Combined path (vertPath + "|" + fragPath)
  private static final Map<String, ShaderProgram> programCache = new HashMap<>();

  protected final String vertPath;
  protected final String fragPath;
  protected ShaderProgram program;

  private int upscaling = 1;
  private boolean enabled = true;

  /**
   * Constructs an AbstractShader using the paths to the vertex and fragment shader files.
   *
   * @param vertPath The file path to the vertex shader.
   * @param fragPath The file path to the fragment shader.
   */
  public AbstractShader(String vertPath, String fragPath) {
    this.vertPath = vertPath;
    this.fragPath = fragPath;
  }

  /**
   * Gets the padding required for this shader effect.
   *
   * @return The padding in pixels.
   */
  public abstract int padding();

  /**
   * Gets the minimum upscaling required for this shader effect.
   *
   * @return The upscaling factor (1 = no upscaling, 2 = 2x upscaling, etc.).
   */
  public int upscaling() {
    return upscaling;
  }

  /**
   * Sets the minimum upscaling required for this shader effect.
   *
   * <p>When chaining, set this field last, since it chains an AbstractShader, not the specific
   * subclass.
   *
   * @param upscaling The upscaling factor (1 = no upscaling, 2 = 2x upscaling, etc.).
   * @return The shader instance for chaining.
   * @throws IllegalArgumentException if the upscaling factor is less than 1.
   */
  public AbstractShader upscaling(int upscaling) {
    if (upscaling < 1) {
      throw new IllegalArgumentException("Upscaling must be at least 1");
    }
    this.upscaling = upscaling;
    return this;
  }

  /**
   * Instructs SpriteBatch to use this shader program and binds all custom uniforms.
   *
   * @param batch The SpriteBatch instance.
   * @param actualUpscale The actual upscaling factor currently applied to the render target.
   */
  public void bind(SpriteBatch batch, int actualUpscale) {
    ensureCompiled();
    batch.setShader(program);

    List<UniformBinding> bindings = getUniforms(actualUpscale);
    if (bindings != null) {
      for (UniformBinding binding : bindings) {
        binding.bind(program);
      }
    }
  }

  /**
   * Resets the batch shader to null after the pass is complete.
   *
   * @param batch The SpriteBatch instance.
   */
  public void unbind(SpriteBatch batch) {
    batch.setShader(null);
  }

  /**
   * Checks if the shader is enabled.
   *
   * @return True if enabled, false otherwise.
   */
  public boolean enabled() {
    return enabled;
  }

  /**
   * Sets whether the shader is enabled.
   *
   * @param enabled True to enable, false to disable.
   * @return The shader instance for chaining.
   */
  public AbstractShader enabled(boolean enabled) {
    this.enabled = enabled;
    return this;
  }

  /**
   * Compiles the shader program lazily if it hasn't been compiled yet. Must be called before
   * binding.
   */
  protected void ensureCompiled() {
    if (program != null) {
      return;
    }

    String cacheKey = vertPath + "|" + fragPath;
    program = programCache.get(cacheKey);

    if (program == null) {
      String vertexShader = Gdx.files.internal(vertPath).readString();
      String fragmentShader = Gdx.files.internal(fragPath).readString();

      ShaderProgram.pedantic = false;
      ShaderProgram newProgram = new ShaderProgram(vertexShader, fragmentShader);

      if (!newProgram.isCompiled()) {
        throw new IllegalStateException(
            "Shader compilation failed for "
                + vertPath
                + " & "
                + fragPath
                + "\nLog: "
                + newProgram.getLog());
      }

      program = newProgram;
      programCache.put(cacheKey, program);
    }
  }

  /**
   * Abstract method for subclasses to define their unique uniform bindings.
   *
   * @param actualUpscale The actual upscaling factor being used.
   * @return A list of UniformBinding objects to apply.
   */
  protected abstract List<UniformBinding> getUniforms(int actualUpscale);

  /** Clears the instance reference to the ShaderProgram. */
  @Override
  public void dispose() {
    this.program = null;
  }

  /**
   * Interface to represent a uniform value and its binding logic. This decouples the shader binding
   * logic from the value storage.
   */
  protected interface UniformBinding {
    /**
     * Gets the name of the uniform.
     *
     * @return The name of the uniform in the shader.
     */
    String name();

    /**
     * Binds the uniform value to the shader program.
     *
     * @param program The ShaderProgram to bind to.
     */
    void bind(ShaderProgram program);
  }

  /**
   * Binds a float uniform.
   *
   * @param name The uniform name in the shader.
   * @param value The float value to bind.
   */
  protected record FloatUniform(String name, float value) implements UniformBinding {
    @Override
    public void bind(ShaderProgram program) {
      program.setUniformf(name, value);
    }
  }

  /**
   * Binds a boolean uniform.
   *
   * @param name The uniform name in the shader.
   * @param value The boolean value to bind.
   */
  protected record BoolUniform(String name, boolean value) implements UniformBinding {
    @Override
    public void bind(ShaderProgram program) {
      program.setUniformi(name, value ? 1 : 0);
    }
  }

  /**
   * Binds a Vector2 uniform.
   *
   * @param name The uniform name in the shader.
   * @param value The Vector2 value to bind.
   */
  protected record Vector2Uniform(String name, Vector2 value) implements UniformBinding {
    @Override
    public void bind(ShaderProgram program) {
      program.setUniformf(name, value);
    }
  }

  /**
   * Binds a texture uniform.
   *
   * @param name The uniform name in the shader.
   * @param texture The Texture object to bind.
   * @param unit OpenGL texture unit (must be >= 1, 0 is reserved for SpriteBatch).
   */
  protected record TextureUniform(String name, Texture texture, int unit)
      implements UniformBinding {
    /**
     * Binds a texture uniform to a specified texture unit.
     *
     * @param name The uniform name in the shader.
     * @param texture The Texture object to bind.
     * @param unit OpenGL texture unit (must be >= 1, 0 is reserved for SpriteBatch).
     */
    public TextureUniform {
      if (unit < 1) {
        throw new IllegalArgumentException(
            "Texture unit for custom uniforms must be 1 or greater.");
      }
    }

    @Override
    public void bind(ShaderProgram program) {
      // Activate this texture in OpenGL
      Gdx.gl.glActiveTexture(unit);
      texture.bind(unit);
      program.setUniformi(name, unit);

      // Set back to original texture for SpriteBatch
      Gdx.gl.glActiveTexture(GL20.GL_TEXTURE0);
    }
  }

  /**
   * Binds a Color uniform.
   *
   * @param name The uniform name in the shader.
   * @param value The Color value to bind.
   */
  protected record ColorUniform(String name, Color value) implements UniformBinding {
    @Override
    public void bind(ShaderProgram program) {
      program.setUniformf(name, value);
    }
  }
}
