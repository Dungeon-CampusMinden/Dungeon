package dungine.level.level3d.block;

import de.fwatermann.dungine.resource.Resource;
import dungine.level.level3d.Chunk;
import org.joml.Vector3i;

public abstract class Block {

  private Chunk chunk;
  private Vector3i chunkPosition;
  private Resource currentTexture;

  public Block(Chunk chunk, Vector3i chunkPosition, Resource texture) {
    this.chunkPosition = chunkPosition;
    this.chunk = chunk;
    this.currentTexture = texture;
  }

  /**
   * Defines if the tile is solid. Solid tiles can not be walked on.
   *
   * @return True if the tile is solid, false otherwise.
   */
  public abstract boolean isSolid();

  public Chunk chunk() {
    return this.chunk;
  }

  public Vector3i chunkPosition() {
    return this.chunkPosition;
  }

  public Resource getFaceResource(BlockFace face) {
    return this.currentTexture;
  }

  public final void update(boolean updateNeighbours) {
    this.chunk.update(this.chunkPosition);
  }
}
