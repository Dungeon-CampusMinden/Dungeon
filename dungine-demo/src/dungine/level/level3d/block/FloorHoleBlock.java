package dungine.level.level3d.block;

import de.fwatermann.dungine.resource.Resource;
import dungine.level.level3d.Chunk;
import org.joml.Vector3i;

/**
 * The `FloorHoleBlock` class represents a floor block with a hole within a chunk in a 3D level. It
 * extends the `FloorBlock` class and provides a specific texture for the floor block with a hole.
 */
public class FloorHoleBlock extends FloorBlock {

  /**
   * Constructs a new `FloorHoleBlock` with the specified chunk and position within the chunk. The
   * block is initialized with a hole texture.
   *
   * @param chunk The chunk to which the block belongs.
   * @param chunkPosition The position of the block within the chunk.
   */
  public FloorHoleBlock(Chunk chunk, Vector3i chunkPosition) {
    super(chunk, chunkPosition, Resource.load("/textures/floor_hole.png"));
  }

  @Override
  public boolean isSolid() {
    return true;
  }
}
