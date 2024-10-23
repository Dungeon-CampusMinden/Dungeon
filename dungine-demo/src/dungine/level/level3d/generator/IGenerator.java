package dungine.level.level3d.generator;

import dungine.level.level3d.Chunk;
import org.joml.Vector3i;

/**
 * The `IGenerator` interface represents a generator for chunks in a 3D level. It provides a method
 * to generate a chunk based on the specified chunk coordinates.
 */
public interface IGenerator {

  /**
   * Generates a chunk based on the specified chunk coordinates.
   *
   * @param chunkCoordinates The coordinates of the chunk to generate.
   * @return The generated chunk.
   */
  public Chunk generateChunk(Vector3i chunkCoordinates);
}
