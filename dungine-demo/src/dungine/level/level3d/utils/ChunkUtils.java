package dungine.level.level3d.utils;

import dungine.level.level3d.Chunk;
import org.joml.Math;
import org.joml.Vector3f;
import org.joml.Vector3i;

public class ChunkUtils {

  /**
   * Get the coordinates of the chunk that contains the given position.
   * @param x X coordinate
   * @param y Y coordinate
   * @param z Z coordinate
   * @return Chunk coordinates
   */
  public static Vector3i toChunkCoordinates(int x, int y, int z) {
    int chunkX = (int) Math.floor((float) x / Chunk.CHUNK_SIZE_X);
    int chunkY = (int) Math.floor((float) y / Chunk.CHUNK_SIZE_Y);
    int chunkZ = (int) Math.floor((float) z / Chunk.CHUNK_SIZE_Z);
    return new Vector3i(chunkX, chunkY, chunkZ);
  }

  /**
   * Get the coordinates of the chunk that contains the given position.
   * @param position Position
   * @return Chunk coordinates
   */
  public static Vector3i toChunkCoordinates(Vector3f position) {
    return toChunkCoordinates(position.x, position.y, position.z);
  }

  /**
   * Get the coordinates of the chunk that contains the given position.
   * @param position Position
   * @return Chunk coordinates
   */
  public static Vector3i toChunkCoordinates(Vector3i position) {
    return toChunkCoordinates(position.x, position.y, position.z);
  }

  /**
   * Get the coordinates of the chunk that contains the given position.
   * @param x X coordinate
   * @param y Y coordinate
   * @param z Z coordinate
   * @return Chunk coordinates
   */
  public static Vector3i toChunkCoordinates(float x, float y, float z) {
    return toChunkCoordinates((int)Math.floor(x), (int)Math.floor(y), (int)Math.floor(z));
  }

  /**
   * Get the coordinates of the chunk that contains the given position.
   * @param x X coordinate
   * @param y Y coordinate
   * @param z Z coordinate
   * @return Chunk coordinates
   */
  public static Vector3i worldToChunkRelative(int x, int y, int z) {
    int chunkX = (x % Chunk.CHUNK_SIZE_X);
    if(chunkX != 0 && x < 0) chunkX += Chunk.CHUNK_SIZE_X;
    int chunkY = (y % Chunk.CHUNK_SIZE_Y);
    if(chunkY != 0 && y < 0) chunkY += Chunk.CHUNK_SIZE_Y;
    int chunkZ = (z % Chunk.CHUNK_SIZE_Z);
    if(chunkZ != 0 && z < 0) chunkZ += Chunk.CHUNK_SIZE_Z;
    return new Vector3i(chunkX, chunkY, chunkZ);
  }

  /**
   * Get the coordinates of the chunk that contains the given position.
   * @param x X coordinate
   * @param y Y coordinate
   * @param z Z coordinate
   * @return Chunk coordinates
   */
  public static Vector3i worldToChunkRelative(float x, float y, float z) {
    return worldToChunkRelative((int) x, (int) y, (int) z);
  }

  /**
   * Get the coordinates of the chunk that contains the given position.
   * @param position Position
   * @return Chunk coordinates
   */
  public static Vector3i worldToChunkRelative(Vector3i position) {
    return worldToChunkRelative(position.x, position.y, position.z);
  }

  /**
   * Get the coordinates of the chunk that contains the given position.
   * @param position Position
   * @return Chunk coordinates
   */
  public static Vector3i worldToChunkRelative(Vector3f position) {
    return worldToChunkRelative(position.x, position.y, position.z);
  }



}
