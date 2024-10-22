package dungine.level.level3d;

import de.fwatermann.dungine.graphics.camera.Camera;
import de.fwatermann.dungine.graphics.texture.atlas.TextureAtlas;
import de.fwatermann.dungine.resource.Resource;
import dungine.level.level3d.generator.IGenerator;
import java.util.HashMap;
import java.util.Map;
import org.joml.Vector3i;

public class Level3D {

  private final Map<Vector3i, Chunk> chunks = new HashMap<>();

  protected TextureAtlas textureAtlas;
  private IGenerator generator;

  public final long seed;

  protected Level3D(long seed) {
    this.seed = seed;
    this.textureAtlas = new TextureAtlas();
    this.textureAtlas.add(Resource.load("/textures/floor_1.png"));
    this.textureAtlas.add(Resource.load("/textures/wall.png"));
  }

  public Level3D() {
    this(System.currentTimeMillis());
  }

  public Chunk chunk(int x, int y, int z, boolean load) {
    if(!load) return this.chunks.get(new Vector3i(x, y, z));
    return this.chunks.computeIfAbsent(new Vector3i(x, y, z), (v) -> this.generator.generateChunk(v));
  }

  public Chunk chunkByWorldCoordinates(int x, int y, int z, boolean load) {
    int chunkX = (int) Math.floor(x / (float) Chunk.CHUNK_SIZE_X);
    int chunkY = (int) Math.floor(y / (float) Chunk.CHUNK_SIZE_Y);
    int chunkZ = (int) Math.floor(z / (float) Chunk.CHUNK_SIZE_Z);
    return this.chunk(chunkX, chunkY, chunkZ, load);
  }

  public IGenerator generator() {
    return this.generator;
  }

  public Level3D generator(IGenerator generator) {
    this.generator = generator;
    return this;
  }

  public void render(Camera<?> camera) {
    Chunk.initShader();
    this.textureAtlas.use(Chunk.SHADER);
    for(Chunk chunk : this.chunks.values()) {
      chunk.render(camera);
    }
  }

  public int chunkCount() {
    return this.chunks.size();
  }

}
