package dungine.level.level3d.block;

import de.fwatermann.dungine.resource.Resource;
import dungine.level.level3d.Chunk;
import org.joml.Vector3i;

/**
 * The `DefaultFloorBlock` class represents a default floor block within a chunk in a 3D level. It
 * extends the `FloorBlock` class and provides a specific texture for the floor block.
 */
public class DefaultFloorBlock extends FloorBlock {

  /**
   * Constructs a new `DefaultFloorBlock` with the specified chunk and position within the chunk.
   * The block is initialized with a default floor texture.
   *
   * @param chunk The chunk to which the block belongs.
   * @param chunkPosition The position of the block within the chunk.
   */
  public DefaultFloorBlock(Chunk chunk, Vector3i chunkPosition) {
    super(chunk, chunkPosition, Resource.load("/textures/floor_1.png"));
  }

  @Override
  public boolean isSolid() {
    return true;
  }
}
