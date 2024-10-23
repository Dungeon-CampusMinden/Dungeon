package dungine.level.level3d.generator;

import dungine.level.level3d.Chunk;
import org.joml.Vector3i;

public interface IGenerator {

  public Chunk generateChunk(Vector3i chunkCoordinates);
}
