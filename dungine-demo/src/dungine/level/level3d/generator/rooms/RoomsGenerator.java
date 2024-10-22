/*
Implementation des Algorithmus zur Generierung von Räumen in einem 3D-Level. Inspiriert
von https://www.gamedeveloper.com/programming/procedural-dungeon-generation-algorithm
*/

package dungine.level.level3d.generator.rooms;

import dungine.level.level3d.Chunk;
import dungine.level.level3d.Level3D;
import dungine.level.level3d.block.FloorBlock;
import dungine.level.level3d.block.WallBlock;
import dungine.level.level3d.generator.IGenerator;
import dungine.level.level3d.utils.ChunkUtils;
import java.util.HashSet;
import java.util.Set;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joml.Random;
import org.joml.Vector2i;
import org.joml.Vector3i;

public class RoomsGenerator implements IGenerator {

  private static final Logger LOGGER = LogManager.getLogger(RoomsGenerator.class);

  private Set<Room> rooms = new HashSet<>();

  private Level3D level;

  public RoomsGenerator(Level3D level) {
    this.level = level;
  }

  private void generateRooms(int numRooms, Vector2i minSize, Vector2i maxSize, int radius, long seed) {

    Random random = new Random(seed);


    for(int i = 0; i < numRooms; i++) {
        Vector2i size = new Vector2i((int) (random.nextFloat() * (maxSize.x - minSize.x) + minSize.x),
            (int) (random.nextFloat() * (maxSize.y - minSize.y) + minSize.y));

        float t = 2 * (float) Math.PI * random.nextFloat();
        float u = random.nextFloat() + random.nextFloat();
        float r = u > 1 ? 2 - u : u;

        Vector3i position = new Vector3i((int) (radius * r * Math.cos(t)), 0, (int) (radius * r * Math.sin(t)));
        position.sub(size.x / 2, 0, size.y / 2);

        Room room = new Room();
        room.position = position;
        room.size = size;

        this.rooms.add(room);

        LOGGER.debug("Generated room at {} with size {}", room.position, room.size);
    }
  }

  private void separateRooms() {
    boolean overlapping;
    int iterations = 0;
    int maxIterations = 1000;

    do {
      overlapping = false;
      for(Room roomA : this.rooms) {
        for(Room roomB : this.rooms) {
          if(roomA == roomB) continue;
          if(this.isOverlapping(roomA, roomB)) {
            overlapping = true;
            Vector2i overlap = this.calculateOverlap(roomA, roomB);
            if(overlap.x != 0) {
              roomA.position.x += overlap.x / 2;
              roomB.position.x -= overlap.x / 2;
            }
            if(overlap.y != 0) {
              roomA.position.z += overlap.y / 2;
              roomB.position.z -= overlap.y / 2;
            }
          }
        }
      }
      iterations ++;
    } while(overlapping && iterations < maxIterations);
  }

  private boolean isOverlapping(Room roomA, Room roomB) {
    Vector3i posA = roomA.position;
    Vector3i posB = roomB.position;
    Vector2i sizeA = roomA.size;
    Vector2i sizeB = roomB.size;

    // Check for overlap in the x and z directions (ignoring y since it's a 2D plane)
    boolean overlapX = posA.x < posB.x + sizeB.x && posA.x + sizeA.x > posB.x;
    boolean overlapZ = posA.z < posB.z + sizeB.y && posA.z + sizeA.y > posB.z;

    return overlapX && overlapZ;
  }

  private Vector2i calculateOverlap(Room roomA, Room roomB) {
    Vector3i posA = roomA.position;
    Vector3i posB = roomB.position;
    Vector2i sizeA = roomA.size;
    Vector2i sizeB = roomB.size;

    // Calculate overlap amount in the x direction
    int overlapX = Math.min(posA.x + sizeA.x, posB.x + sizeB.x) - Math.max(posA.x, posB.x);

    // Calculate overlap amount in the z direction
    int overlapZ = Math.min(posA.z + sizeA.y, posB.z + sizeB.y) - Math.max(posA.z, posB.z);

    // If no overlap, set overlap to 0
    if (overlapX <= 0) overlapX = 0;
    if (overlapZ <= 0) overlapZ = 0;

    return new Vector2i(overlapX, overlapZ);
  }

  public void generate(int rooms, long seed) {
    this.generateRooms(rooms, new Vector2i(5, 5), new Vector2i(20, 20), 50, seed);
    this.separateRooms();

    for(Room room : this.rooms) {
      for(int x = 0; x < room.size.x; x++) {
        for(int z = 0; z < room.size.y; z++) {
          Vector3i pos = new Vector3i(room.position.x + x, room.position.y, room.position.z + z);
          Vector3i inChunkPos = ChunkUtils.worldToChunkRelative(pos);
          Chunk chunk = this.level.chunkByWorldCoordinates (room.position.x + x, room.position.y, room.position.z + z, true);
          if(chunk == null) continue;
          if(x == 0 || x == room.size.x -1 || z == 0 || z == room.size.y -1) {
            chunk.setBlock(new WallBlock(chunk, inChunkPos.add(0, 1, 0, new Vector3i())));
            chunk.setBlock(new WallBlock(chunk, inChunkPos.add(0, 2, 0, new Vector3i())));
            chunk.setBlock(new WallBlock(chunk, inChunkPos.add(0, 3, 0, new Vector3i())));
          } else {
            chunk.setBlock(new FloorBlock(chunk, inChunkPos));
          }
        }
      }
    }
  }

  @Override
  public Chunk generateChunk(Vector3i chunkCoordinates) {
    return new Chunk(this.level, chunkCoordinates);
  }

  public static class Room {
    public Vector3i position;
    public Vector2i size;
  }


}
