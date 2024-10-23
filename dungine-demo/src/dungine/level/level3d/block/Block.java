package dungine.level.level3d.block;

import de.fwatermann.dungine.resource.Resource;
import dungine.level.level3d.Chunk;
import org.joml.Vector3i;

/**
 * The `Block` class represents a block within a chunk in a 3D level. It is an abstract class that
 * provides the basic properties and methods for blocks, such as their position within the chunk and
 * their texture.
 */
public abstract class Block {

  private Chunk chunk;
  private Vector3i chunkPosition;
  private Resource currentTexture;

  /**
   * Constructs a new `Block` with the specified chunk, position within the chunk, and texture.
   *
   * @param chunk The chunk to which the block belongs.
   * @param chunkPosition The position of the block within the chunk.
   * @param texture The texture of the block.
   */
  public Block(Chunk chunk, Vector3i chunkPosition, Resource texture) {
    this.chunkPosition = chunkPosition;
    this.chunk = chunk;
    this.currentTexture = texture;
  }

  /**
   * Defines if the tile is solid. A solid block cannot be passed through by a player.
   *
   * @return True if the tile is solid, false otherwise.
   */
  public abstract boolean isSolid();

  /**
   * Gets the chunk to which the block belongs.
   *
   * @return The chunk containing the block.
   */
  public Chunk chunk() {
    return this.chunk;
  }

  /**
   * Gets the position of the block within the chunk.
   *
   * @return The position of the block within the chunk.
   */
  public Vector3i chunkPosition() {
    return this.chunkPosition;
  }

  /**
   * Gets the texture resource for the specified face of the block.
   *
   * @param face The face of the block.
   * @return The texture resource for the specified face.
   */
  public Resource getFaceResource(BlockFace face) {
    return this.currentTexture;
  }

  /**
   * Updates the block and optionally its neighboring blocks.
   *
   * @param updateNeighbours If true, updates the neighboring blocks as well.
   */
  public final void update(boolean updateNeighbours) {
    this.chunk.update(this.chunkPosition);
  }
}
