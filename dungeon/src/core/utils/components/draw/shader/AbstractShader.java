package core.utils.components.draw.shader;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
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

  public AbstractShader(String vertPath, String fragPath) {
    this.vertPath = vertPath;
    this.fragPath = fragPath;
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

  /**
   * Gets the padding required for this shader effect.
   *
   * @return The padding in pixels.
   */
  public abstract int getPadding();

  /**
   * Gets the minimum upscaling required for this shader effect.
   *
   * <p>When effects need to draw "in between" pixels (e.g. a smaller outline than 1 pixel),
   * upscaling the render target is necessary.
   *
   * @return The upscaling factor (1 = no upscaling, 2 = 2x upscaling, etc.)
   */
  public int upscaling() {
    return upscaling;
  }

  /**
   * Gets the minimum upscaling required for this shader effect.
   *
   * <p>When effects need to draw "in between" pixels (e.g. a smaller outline than 1 pixel),
   * upscaling the render target is necessary.
   *
   * <p>When chaining, set this field last, since it chains an AbstractShader, not the specific
   * subclass.
   *
   * @param upscaling The upscaling factor (1 = no upscaling, 2 = 2x upscaling, etc.)
   * @return The shader instance for chaining.
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
   * Clears the instance reference. Note: Actual GPU resource disposal is complex due to static
   * caching and should be handled by a dedicated cleanup routine at application shutdown.
   */
  @Override
  public void dispose() {
    this.program = null;
  }

  /** Static method for application-wide cleanup of all cached ShaderPrograms. */
  public static void disposeAllStaticPrograms() {
    for (ShaderProgram p : programCache.values()) {
      p.dispose();
    }
    programCache.clear();
  }

  /**
   * Interface to represent a uniform value and its binding logic. This decouples the shader binding
   * logic from the value storage.
   */
  protected interface UniformBinding {
    String name();

    void bind(ShaderProgram program);
  }

  /** Binds a float uniform. */
  protected record FloatUniform(String name, float value) implements UniformBinding {
    @Override
    public void bind(ShaderProgram program) {
      program.setUniformf(name, value);
    }
  }

  /** Binds a boolean uniform as an integer (1 for true, 0 for false). */
  protected record BoolUniform(String name, boolean value) implements UniformBinding {
    @Override
    public void bind(ShaderProgram program) {
      program.setUniformi(name, value ? 1 : 0);
    }
  }

  /** Binds a Vector2 uniform. */
  protected record Vector2Uniform(String name, Vector2 value) implements UniformBinding {
    @Override
    public void bind(ShaderProgram program) {
      program.setUniformf(name, value);
    }
  }

  /**
   * Binds a texture uniform to a specified texture unit.
   *
   * @param unit OpenGL texture unit (must be >= 1, 0 is reserved for SpriteBatch)
   */
  protected record TextureUniform(String name, Texture texture, int unit)
      implements UniformBinding {
    @Override
    public void bind(ShaderProgram program) {
      // Activate this texture in OpenGL
      Gdx.gl.glActiveTexture(unit);
      texture.bind(unit);
      program.setUniformi(name, unit);

      // Set back to original texture for SpriteBatch
      Gdx.gl.glActiveTexture(0);
    }
  }

  protected record ColorUniform(String name, Color value) implements UniformBinding {
    @Override
    public void bind(ShaderProgram program) {
      program.setUniformf(name, value);
    }
  }
}
