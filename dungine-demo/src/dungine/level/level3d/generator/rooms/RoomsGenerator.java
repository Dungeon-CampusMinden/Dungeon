package dungine.level.level3d.generator.rooms;

import dungine.level.level3d.Chunk;
import dungine.level.level3d.Level3D;
import dungine.level.level3d.block.DefaultFloorBlock;
import dungine.level.level3d.block.FloorBlock;
import dungine.level.level3d.block.FloorDamagedBlock;
import dungine.level.level3d.block.FloorHoleBlock;
import dungine.level.level3d.block.WallBlock;
import dungine.level.level3d.generator.IGenerator;
import dungine.level.level3d.utils.ChunkUtils;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joml.Random;
import org.joml.SimplexNoise;
import org.joml.Vector2f;
import org.joml.Vector2i;
import org.joml.Vector3f;
import org.joml.Vector3i;

/**
 * The `RoomsGenerator` class is responsible for generating rooms and hallways in a 3D level.
 * It uses a random seed to ensure reproducibility of the generated structures.
 * The class provides methods to generate rooms, separate overlapping rooms, connect rooms with hallways,
 * and build the rooms and hallways within the level.
 *
 * <p>Key functionalities include:</p>
 * <ul>
 *   <li>Generating a specified number of rooms with random sizes and positions within a given radius.</li>
 *   <li>Separating overlapping rooms to ensure no two rooms occupy the same space.</li>
 *   <li>Connecting rooms with hallways to create a navigable dungeon layout.</li>
 *   <li>Building the rooms and hallways by placing floor and wall blocks in the level.</li>
 * </ul>
 *
 * <p>Usage example:</p>
 * <pre>
 * {@code
 * Level3D level = new Level3D();
 * RoomsGenerator generator = new RoomsGenerator(level, 3467589736L);
 * generator.generate();
 * }
 * </pre>
 *
 *
 * @see Level3D
 * @see Chunk
 * @see FloorBlock
 * @see WallBlock
 * @see ChunkUtils
 * @see Logger
 */
public class RoomsGenerator implements IGenerator {

  private static final Logger LOGGER = LogManager.getLogger(RoomsGenerator.class);

  private Set<Room> rooms = new HashSet<>();
  private Vector2f meanSize = new Vector2f();
  private final float floorNoiseSeed;

  private Level3D level;
  private Random random;

  public RoomsGenerator(Level3D level, long seed) {
    this.level = level;
    this.random = new Random(seed);
    this.floorNoiseSeed = this.random.nextFloat();
  }

  private void generateRooms(int numRooms, Vector2i minSize, Vector2i maxSize, int radius) {

    for(int i = 0; i < numRooms; i++) {
        Vector2i size = new Vector2i((int) (this.random.nextFloat() * (maxSize.x - minSize.x) + minSize.x),
            (int) (this.random.nextFloat() * (maxSize.y - minSize.y) + minSize.y));

        this.meanSize.add(size.x, size.y);

        float t = 2 * (float) Math.PI * this.random.nextFloat();
        float u = this.random.nextFloat() + this.random.nextFloat();
        float r = u > 1 ? 2 - u : u;

        Vector3i position = new Vector3i((int) (radius * r * Math.cos(t)), 0, (int) (radius * r * Math.sin(t)));
        position.sub(size.x / 2, 0, size.y / 2);

        Room room = new Room();
        room.position = position;
        room.size = size;

        this.rooms.add(room);

        LOGGER.debug("Generated room at {} with size {}", room.position, room.size);
    }

    this.meanSize.div(numRooms);

    for(Room room : this.rooms) {
      if(room.size.x >= this.meanSize.x && room.size.y >= this.meanSize.y) {
        room.mainRoom = true;
      }
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

  public void connectRooms() {
    for(Room room : this.rooms) {
      for(int i = 0; i < 2; i ++) {
        Room closestRoom = null;
        float closestDistance = Float.MAX_VALUE;
        for(Room otherRoom : this.rooms) {
          if(room == otherRoom) continue;
          float distance = room.position.distanceSquared(otherRoom.position);
          if(distance < closestDistance && !room.connectedRooms.contains(otherRoom)) {
            closestDistance = distance;
            closestRoom = otherRoom;
          }
        }
        if(closestRoom == null) continue;

        room.connectedRooms.add(closestRoom);
        closestRoom.connectedRooms.add(room);
      }
    }

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

  public void generate() {
    this.generateRooms(this.random.nextInt(40) + 10, new Vector2i(8, 8), new Vector2i(20, 20), 50);
    this.separateRooms();
    this.connectRooms();

    this.buildRooms();
    this.buildHallways();
  }

  private void buildRooms() {
    for(Room room : this.rooms) {
      for(int x = 0; x < room.size.x; x++) {
        for(int z = 0; z < room.size.y; z++) {
          Vector3i pos = new Vector3i(room.position.x + x, room.position.y, room.position.z + z);
          if(x == 0 || x == room.size.x -1 || z == 0 || z == room.size.y -1) {
            this.makeWall(pos.x, pos.y + 1, pos.z);
            this.makeWall(pos.x, pos.y + 2, pos.z);
            this.makeWall(pos.x, pos.y + 3, pos.z);
          } else {
            this.makeFloor(pos.x, pos.y, pos.z);
          }
        }
      }
    }
  }

  public void buildHallways() {

    List<Vector3i> hallwayFloor = new ArrayList<>();

    for(Room room : this.rooms) {
      for(Room connectedRoom : room.connectedRooms) {
        Vector3i start = new Vector3i(room.position.x + room.size.x / 2, 0, room.position.z + room.size.y / 2);
        Vector3i end = new Vector3i(connectedRoom.position.x + connectedRoom.size.x / 2, 0, connectedRoom.position.z + connectedRoom.size.y / 2);
        Vector3i current = new Vector3i(start);

        while(current.x != end.x || current.z != end.z) {
          if(current.x != end.x) {
            current.x += current.x < end.x ? 1 : -1;
          } else {
            current.z += current.z < end.z ? 1 : -1;
          }
          this.makeFloor(current.x, current.y, current.z);
          this.makeFloor(current.x+1, current.y, current.z);
          this.makeFloor(current.x, current.y, current.z+1);
          this.makeFloor(current.x+1, current.y, current.z+1);

          //Clear air above floor
          for(int i = 1; i < 3; i ++) {
            this.makeNull(current.x, current.y+i, current.z);
            this.makeNull(current.x+1, current.y+i, current.z);
            this.makeNull(current.x, current.y+i, current.z+1);
            this.makeNull(current.x+1, current.y+i, current.z+1);
          }

          hallwayFloor.add(new Vector3i(current));
          hallwayFloor.add(new Vector3i(current.x + 1, current.y, current.z));
          hallwayFloor.add(new Vector3i(current.x, current.y, current.z + 1));
          hallwayFloor.add(new Vector3i(current.x + 1, current.y, current.z + 1));
        }
      }
    }

    for(Vector3i pos : hallwayFloor) {
      if(!this.isFloor(pos.x + 1, pos.y, pos.z)) {
        this.makeWall(pos.x + 1, pos.y + 1, pos.z);
        this.makeWall(pos.x + 1, pos.y + 2, pos.z);
        this.makeWall(pos.x + 1, pos.y + 3, pos.z);
      }
      if(!this.isFloor(pos.x - 1, pos.y, pos.z)) {
        this.makeWall(pos.x - 1, pos.y + 1, pos.z);
        this.makeWall(pos.x - 1, pos.y + 2, pos.z);
        this.makeWall(pos.x - 1, pos.y + 3, pos.z);
      }
      if(!this.isFloor(pos.x, pos.y, pos.z + 1)) {
        this.makeWall(pos.x, pos.y + 1, pos.z + 1);
        this.makeWall(pos.x, pos.y + 2, pos.z + 1);
        this.makeWall(pos.x, pos.y + 3, pos.z + 1);
      }
      if(!this.isFloor(pos.x, pos.y, pos.z - 1)) {
        this.makeWall(pos.x, pos.y + 1, pos.z - 1);
        this.makeWall(pos.x, pos.y + 2, pos.z - 1);
        this.makeWall(pos.x, pos.y + 3, pos.z - 1);
      }
    }
  }

  private void makeFloor(int x, int y, int z) {
    Chunk chunk = this.level.chunkByWorldCoordinates(x, y, z, true);
    if(chunk == null) return;
    int variant = (int) (Math.floor(Math.abs(SimplexNoise.noise(x * 0.1f, z * 0.1f, this.floorNoiseSeed)) * 3));
    switch(variant) {
      case 1 -> {
        chunk.setBlock(new FloorDamagedBlock(chunk, ChunkUtils.worldToChunkRelative(x, y, z)));
      }
      case 2 -> {
        chunk.setBlock(new FloorHoleBlock(chunk, ChunkUtils.worldToChunkRelative(x, y, z)));
      }
      default -> {
        chunk.setBlock(new DefaultFloorBlock(chunk, ChunkUtils.worldToChunkRelative(x, y, z)));
      }
    }
  }

  private void makeWall(int x, int y, int z) {
    Chunk chunk = this.level.chunkByWorldCoordinates(x, y, z, true);
    if(chunk == null) return;
    chunk.setBlock(new WallBlock(chunk, ChunkUtils.worldToChunkRelative(x, y, z)));
  }

  private void makeNull(int x, int y, int z) {
    Chunk chunk = this.level.chunkByWorldCoordinates(x, y, z, false);
    if(chunk == null) return;
    chunk.removeBlock(ChunkUtils.worldToChunkRelative(x, y, z));
  }

  private boolean isFloor(int x, int y, int z) {
    Chunk chunk = this.level.chunkByWorldCoordinates(x, y, z, false);
    if(chunk == null) return false;
    return chunk.getBlockAt(ChunkUtils.worldToChunkRelative(x, y, z)) instanceof FloorBlock;
  }

  @Override
  public Chunk generateChunk(Vector3i chunkCoordinates) {
    return new Chunk(this.level, chunkCoordinates);
  }

  public Vector3f getStartPosition() {
    for(Room room : this.rooms) {
      if(room.mainRoom) {
        return new Vector3f(room.position.x + room.size.x / 2.0f, 1.5f, room.position.z + room.size.y / 2.0f);
      }
    }
    return new Vector3f(0, 1.5f, 0);
  }

  public static class Room {
    public Vector3i position;
    public Vector2i size;
    public List<Room> connectedRooms = new ArrayList<>();
    boolean mainRoom = false;
  }


}
