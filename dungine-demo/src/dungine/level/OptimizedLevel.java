package dungine.level;

import de.fwatermann.dungine.graphics.GLUsageHint;
import de.fwatermann.dungine.graphics.Renderable;
import de.fwatermann.dungine.graphics.camera.Camera;
import de.fwatermann.dungine.graphics.mesh.ArrayMesh;
import de.fwatermann.dungine.graphics.mesh.DataType;
import de.fwatermann.dungine.graphics.mesh.PrimitiveType;
import de.fwatermann.dungine.graphics.mesh.VertexAttribute;
import de.fwatermann.dungine.graphics.shader.Shader;
import de.fwatermann.dungine.graphics.shader.ShaderProgram;
import de.fwatermann.dungine.graphics.texture.atlas.TextureAtlas;
import de.fwatermann.dungine.resource.Resource;
import java.io.IOException;
import java.nio.ByteBuffer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joml.SimplexNoise;
import org.lwjgl.BufferUtils;

/**
 * The `OptimizedLevel` class represents an optimized 2D level in the game. It manages the tiles
 * that make up the level, handles the texture atlas for the level, and provides methods for
 * rendering the level.
 */
public class OptimizedLevel extends Renderable<OptimizedLevel> {

  private static final Logger LOGGER = LogManager.getLogger(OptimizedLevel.class);

  /** X-Size of the level. */
  public static final int LEVEL_SIZE_X = 32;

  /** Y-Size of the level. */
  public static final int LEVEL_SIZE_Y = 32;

  private static ShaderProgram DEFAULT_SHADER;

  private final TextureAtlas atlas;

  private final Tile[][] tiles = new Tile[LEVEL_SIZE_X][LEVEL_SIZE_Y];

  private ArrayMesh mesh;

  /** Create a new `OptimizedLevel` instance. */
  public OptimizedLevel() {
    this.order = 0;
    this.atlas = new TextureAtlas();
    this.atlas.add(Resource.load("/textures/floor_1.png"));
    this.atlas.add(Resource.load("/textures/floor_damaged.png"));
    this.atlas.add(Resource.load("/textures/floor_hole.png"));

    float seed = (float) Math.random();

    for (int x = 0; x < LEVEL_SIZE_X; x++) {
      for (int y = 0; y < LEVEL_SIZE_Y; y++) {
        float sx = x * 0.1f;
        float sy = y * 0.1f;

        int variant = (int) (Math.floor(Math.abs(SimplexNoise.noise(sx, sy, seed)) * 3));
        System.out.print(variant + ", ");
        this.tiles[x][y] = new Tile(x, y, variant);
      }
    }
  }

  @Override
  public void render(Camera<?> camera) {
    if (DEFAULT_SHADER == null) {
      try {
        Shader vertexShader =
            Shader.loadShader(Resource.load("/shaders/level.vsh"), Shader.ShaderType.VERTEX_SHADER);
        Shader geometryShader =
            Shader.loadShader(
                Resource.load("/shaders/level.gsh"), Shader.ShaderType.GEOMETRY_SHADER);
        Shader fragmentShader =
            Shader.loadShader(
                Resource.load("/shaders/level.fsh"), Shader.ShaderType.FRAGMENT_SHADER);
        DEFAULT_SHADER = new ShaderProgram(vertexShader, geometryShader, fragmentShader);
      } catch (IOException ex) {
        LOGGER.error("Could not load shaders for level rendering", ex);
      }
    }
    this.render(camera, DEFAULT_SHADER);
  }

  @Override
  public void render(Camera<?> camera, ShaderProgram shader) {
    if (this.mesh == null) {
      this.mesh =
          new ArrayMesh(
              BufferUtils.createByteBuffer(LEVEL_SIZE_X * LEVEL_SIZE_Y * 4 * 4),
              PrimitiveType.POINTS,
              GLUsageHint.DRAW_STATIC,
              new VertexAttribute(3, DataType.FLOAT, "aPosition"),
              new VertexAttribute(1, DataType.UNSIGNED_INT, "aAtlasEntry"));
      this.createMesh();
    }
    this.mesh.transformation(this.position(), this.rotation(), this.scaling());
    shader.bind();
    this.atlas.use(shader);
    this.mesh.render(camera, shader);
    shader.unbind();
  }

  private void createMesh() {
    for (int x = 0; x < LEVEL_SIZE_X; x++) {
      for (int y = 0; y < LEVEL_SIZE_Y; y++) {
        this.updateMesh(x, y);
      }
    }
  }

  private void updateMesh(int x, int y) {
    ByteBuffer buffer = this.mesh.vertexBuffer();
    buffer.position((x * LEVEL_SIZE_Y + y) * 4 * 4);
    buffer.putFloat(x);
    buffer.putFloat(0.0f);
    buffer.putFloat(y);
    buffer.putInt(this.tiles[x][y].entry());
    buffer.position(0);
    this.mesh.markVerticesDirty();
  }

  /** Class representing a tile in the level. */
  public static class Tile {

    private final int x;
    private final int y;
    private final int entry;

    /**
     * Create a new `Tile` instance.
     *
     * @param x X-coordinate of the tile
     * @param y Y-coordinate of the tile
     * @param entry Atlas entry of the tile
     */
    public Tile(int x, int y, int entry) {
      this.x = x;
      this.y = y;
      this.entry = entry;
    }

    /**
     * Get the X-coordinate of the tile.
     *
     * @return X-coordinate of the tile
     */
    public int x() {
      return this.x;
    }

    /**
     * Get the Y-coordinate of the tile.
     *
     * @return Y-coordinate of the tile
     */
    public int y() {
      return this.y;
    }

    /**
     * Get the atlas entry of the tile.
     *
     * @return Atlas entry of the tile
     */
    public int entry() {
      return this.entry;
    }
  }
}
