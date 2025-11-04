package core.utils.components.draw.shader;

import com.badlogic.gdx.Gdx;
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

  // --- Instance Members ---
  protected final String vertPath;
  protected final String fragPath;
  protected float timeElapsed = 0f;

  // The compiled program instance for this specific pair of files
  protected ShaderProgram program;

  // --- Core Logic ---

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
      // System.out.println("Compiling shader: " + vertPath + " & " + fragPath);

      // Read shader source from files
      String vertexShader = Gdx.files.internal(vertPath).readString();
      String fragmentShader = Gdx.files.internal(fragPath).readString();

      // Compile the program, setting pedantic = false
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
   * @param deltaTime The time elapsed since the last frame.
   * @return A list of UniformBinding objects to apply.
   */
  protected abstract List<UniformBinding> getUniforms(float deltaTime);

  /**
   * Gets the padding required for this shader effect.
   *
   * @return The padding in pixels.
   */
  public abstract float getPadding();

  /**
   * Instructs SpriteBatch to use this shader program and binds all custom uniforms.
   *
   * @param batch The SpriteBatch instance.
   * @param deltaTime The time elapsed since the last frame.
   */
  public void bind(SpriteBatch batch, float deltaTime) {
    ensureCompiled();
    batch.setShader(program);

    List<UniformBinding> bindings = getUniforms(deltaTime);
    if (bindings != null) {
      for (UniformBinding binding : bindings) {
        binding.bind(program);
      }
    }

    // Bind u_time uniform for all shaders
    program.setUniformf("u_time", timeElapsed);
    timeElapsed += deltaTime;
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
    String getName();

    void bind(ShaderProgram program);
  }

  /** Binds a float uniform. */
  protected static class FloatUniform implements UniformBinding {
    private final String name;
    private final float value;

    public FloatUniform(String name, float value) {
      this.name = name;
      this.value = value;
    }

    @Override
    public String getName() {
      return name;
    }

    @Override
    public void bind(ShaderProgram program) {
      program.setUniformf(name, value);
    }
  }

  /** Binds a Vector2 uniform. */
  protected static class Vector2Uniform implements UniformBinding {
    private final String name;
    private final Vector2 value;

    public Vector2Uniform(String name, Vector2 value) {
      this.name = name;
      this.value = value;
    }

    @Override
    public String getName() {
      return name;
    }

    @Override
    public void bind(ShaderProgram program) {
      program.setUniformf(name, value);
    }
  }

  /** Binds a texture uniform to a specified texture unit. */
  protected static class TextureUniform implements UniformBinding {
    private final String name;
    private final Texture texture;
    private final int unit; // OpenGL texture unit (must be >= 1, 0 is reserved for SpriteBatch)

    /**
     * @param name The uniform name in the shader.
     * @param texture The texture to bind.
     * @param unit The texture unit index (e.g., 1, 2, 3...).
     */
    public TextureUniform(String name, Texture texture, int unit) {
      this.name = name;
      this.texture = texture;
      this.unit = unit;
    }

    @Override
    public String getName() {
      return name;
    }

    @Override
    public void bind(ShaderProgram program) {
      // 1. Activate and bind the texture to the specified unit
      texture.bind(unit);
      // 2. Set the shader uniform to the corresponding texture unit index
      program.setUniformi(name, unit);
    }
  }
}
