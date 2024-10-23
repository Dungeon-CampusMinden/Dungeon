package dungine.level.level3d.block;

import de.fwatermann.dungine.resource.Resource;
import dungine.level.level3d.Chunk;
import org.joml.Vector3i;

/**
 * The `FloorBlock` class represents a floor block within a chunk in a 3D level. It extends the
 * `Block` class and provides specific functionality for floor blocks.
 */
public abstract class FloorBlock extends Block {

  /**
   * Constructs a new `FloorBlock` with the specified chunk, position within the chunk, and texture.
   *
   * @param chunk The chunk to which the block belongs.
   * @param chunkPosition The position of the block within the chunk.
   * @param resource The texture of the block.
   */
  public FloorBlock(Chunk chunk, Vector3i chunkPosition, Resource resource) {
    super(chunk, chunkPosition, resource);
  }

  @Override
  public boolean isSolid() {
    return true;
  }
}
