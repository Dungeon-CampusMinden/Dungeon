package dungine.level.level3d;

import de.fwatermann.dungine.graphics.Renderable;
import de.fwatermann.dungine.graphics.camera.Camera;
import de.fwatermann.dungine.graphics.shader.ShaderProgram;
import de.fwatermann.dungine.graphics.texture.atlas.TextureAtlas;
import de.fwatermann.dungine.resource.Resource;
import dungine.level.level3d.generator.IGenerator;
import java.util.HashMap;
import java.util.Map;
import org.joml.Vector3i;

/**
 * The `Level3D` class represents a 3D level in the game. It manages the chunks that make up the
 * level, handles the texture atlas for the level, and provides methods for generating and rendering
 * the level.
 *
 * <p>Key functionalities include:
 *
 * <ul>
 *   <li>Managing the chunks within the level, including loading and retrieving chunks by
 *       coordinates.
 *   <li>Handling the texture atlas used for rendering the level's textures.
 *   <li>Generating chunks using a specified generator.
 *   <li>Rendering the level by rendering each chunk within it.
 * </ul>
 *
 * <p>Usage example:
 *
 * <pre>{@code
 * Level3D level = new Level3D();
 * level.generator(new RoomsGenerator(level, 3467589736L));
 * level.render(camera);
 * }</pre>
 */
public class Level3D extends Renderable<Level3D> {

  private final Map<Vector3i, Chunk> chunks = new HashMap<>();

  protected TextureAtlas textureAtlas;
  private IGenerator generator;

  /** The seed used for generating the level. */
  public final long seed;

  /**
   * Create a new `Level3D` instance with the specified seed.
   *
   * @param seed The seed used for generating the level.
   */
  protected Level3D(long seed) {
    this.seed = seed;
    this.order = 0;
    this.textureAtlas = new TextureAtlas();
    this.textureAtlas.add(Resource.load("/textures/floor_1.png"));
    this.textureAtlas.add(Resource.load("/textures/floor_damaged.png"));
    this.textureAtlas.add(Resource.load("/textures/floor_hole.png"));
    this.textureAtlas.add(Resource.load("/textures/wall.png"));
  }

  /** Create a new `Level3D` instance with a seed based on the current time. */
  public Level3D() {
    this(System.currentTimeMillis());
  }

  /**
   * Get the chunk with the specified chunk coordinates, optionally loading it if it doesn't exist.
   *
   * @param x The x-coordinate of the chunk.
   * @param y The y-coordinate of the chunk.
   * @param z The z-coordinate of the chunk.
   * @param load Whether to load the chunk if it doesn't exist.
   * @return The chunk at the specified coordinates, or `null` if it doesn't exist and `load` is
   *     `false`.
   */
  public Chunk chunk(int x, int y, int z, boolean load) {
    if (!load) return this.chunks.get(new Vector3i(x, y, z));
    return this.chunks.computeIfAbsent(
        new Vector3i(x, y, z), (v) -> this.generator.generateChunk(v));
  }

  /**
   * Get the chunk at the specified coordinates, optionally loading it if it doesn't exist.
   *
   * @param x The x-coordinate of the chunk.
   * @param y The y-coordinate of the chunk.
   * @param z The z-coordinate of the chunk.
   * @param load Whether to load the chunk if it doesn't exist.
   * @return The chunk at the specified coordinates, or `null` if it doesn't exist and `load` is
   *     `false`.
   */
  public Chunk chunkByWorldCoordinates(int x, int y, int z, boolean load) {
    int chunkX = (int) Math.floor(x / (float) Chunk.CHUNK_SIZE_X);
    int chunkY = (int) Math.floor(y / (float) Chunk.CHUNK_SIZE_Y);
    int chunkZ = (int) Math.floor(z / (float) Chunk.CHUNK_SIZE_Z);
    return this.chunk(chunkX, chunkY, chunkZ, load);
  }

  /**
   * Get the generator used for generating the level.
   *
   * @return The generator used for generating the level.
   */
  public IGenerator generator() {
    return this.generator;
  }

  /**
   * Set the generator used for generating the level.
   *
   * @param generator The generator to use for generating the level.
   * @return This `Level3D` instance.
   */
  public Level3D generator(IGenerator generator) {
    this.generator = generator;
    return this;
  }

  @Override
  public void render(Camera<?> camera) {
    Chunk.initShader();
    this.textureAtlas.use(Chunk.SHADER);
    for (Chunk chunk : this.chunks.values()) {
      chunk.render(camera);
    }
  }

  @Override
  public void render(Camera<?> camera, ShaderProgram shader) {
    Chunk.initShader();
    this.textureAtlas.use(Chunk.SHADER);
    for (Chunk chunk : this.chunks.values()) {
      chunk.render(camera);
    }
  }

  /**
   * Get the number of chunks in the level.
   *
   * @return The number of chunks in the level.
   */
  public int chunkCount() {
    return this.chunks.size();
  }
}
