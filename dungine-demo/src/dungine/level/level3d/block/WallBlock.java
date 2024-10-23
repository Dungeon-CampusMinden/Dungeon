package dungine.level.level3d.block;

import de.fwatermann.dungine.resource.Resource;
import dungine.level.level3d.Chunk;
import org.joml.Vector3i;

/**
 * The `WallBlock` class represents a wall block within a chunk in a 3D level. It extends the
 * `Block` class and provides a specific texture for the wall block.
 */
public class WallBlock extends Block {

  /**
   * Constructs a new `WallBlock` with the specified chunk and position within the chunk. The block
   * is initialized with a wall texture.
   *
   * @param chunk The chunk to which the block belongs.
   * @param chunkPosition The position of the block within the chunk.
   */
  public WallBlock(Chunk chunk, Vector3i chunkPosition) {
    super(chunk, chunkPosition, Resource.load("/textures/wall.png"));
  }

  @Override
  public boolean isSolid() {
    return true;
  }
}
