package dungine.level.level3d;

import de.fwatermann.dungine.graphics.GLUsageHint;
import de.fwatermann.dungine.graphics.camera.Camera;
import de.fwatermann.dungine.graphics.mesh.ArrayMesh;
import de.fwatermann.dungine.graphics.mesh.DataType;
import de.fwatermann.dungine.graphics.mesh.Mesh;
import de.fwatermann.dungine.graphics.mesh.PrimitiveType;
import de.fwatermann.dungine.graphics.mesh.VertexAttribute;
import de.fwatermann.dungine.graphics.shader.Shader;
import de.fwatermann.dungine.graphics.shader.ShaderProgram;
import de.fwatermann.dungine.resource.Resource;
import dungine.level.level3d.block.Block;
import dungine.level.level3d.block.BlockFace;
import dungine.level.level3d.utils.ChunkUtils;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Optional;
import org.joml.Vector3f;
import org.joml.Vector3i;
import org.joml.Vector3ic;
import org.lwjgl.BufferUtils;

/**
 * The `Chunk` class represents a segment of a 3D level in the game. It is responsible for managing
 * the blocks within the chunk, handling the chunk's mesh, and rendering the chunk.
 *
 * <p>Key functionalities include:</p>
 * <ul>
 *   <li>Managing the blocks within the chunk, including setting, removing, and retrieving blocks.</li>
 *   <li>Rebuilding the chunk's mesh to reflect changes in the blocks.</li>
 *   <li>Rendering the chunk using a shader program.</li>
 *   <li>Handling chunk coordinates and converting world positions to chunk-relative positions.</li>
 * </ul>
 *
 * <p>Usage example:</p>
 * <pre>
 * {@code
 * Level3D level = new Level3D();
 * Vector3i chunkCoordinates = new Vector3i(0, 0, 0);
 * Chunk chunk = new Chunk(level, chunkCoordinates);
 * chunk.setBlock(new Block(...));
 * chunk.render(camera);
 * }
 * </pre>
 */
public class Chunk {

  /*
   Vertex size:
   3+1 byte: Position (+1 padding)
   6 short: Texture indices/face

   = 4 + 12 = 16 bytes/vertex
  */

  public static ShaderProgram SHADER;

  private final ByteBuffer vertices;
  private Mesh<?> mesh;

  public static final int CHUNK_SIZE_X = 16;
  public static final int CHUNK_SIZE_Y = 16;
  public static final int CHUNK_SIZE_Z = 16;

  private final Block[][][] blocks = new Block[CHUNK_SIZE_X][CHUNK_SIZE_Y][CHUNK_SIZE_Z];
  private final Vector3i position;
  private final Level3D level;

  public Chunk(Level3D level, Vector3i chunkCoordinates) {
    this.position = chunkCoordinates;
    this.vertices = BufferUtils.createByteBuffer(CHUNK_SIZE_X * CHUNK_SIZE_Y * CHUNK_SIZE_Z * 16);
    for(int i = 0; i < CHUNK_SIZE_X * CHUNK_SIZE_Y * CHUNK_SIZE_Z * 16; i++) {
      this.vertices.put((byte) 0);
    }
    this.level = level;
    this.rebuild();
  }

  public static void initShader() {
    if(SHADER != null) return;
    try {
      Shader vertexShader = Shader.loadShader(Resource.load("/shaders/level3d/chunk.vsh"), Shader.ShaderType.VERTEX_SHADER);
      Shader geometryShader = Shader.loadShader(Resource.load("/shaders/level3d/chunk.gsh"), Shader.ShaderType.GEOMETRY_SHADER);
      Shader fragmentShader = Shader.loadShader(Resource.load("/shaders/level3d/chunk.fsh"), Shader.ShaderType.FRAGMENT_SHADER);
      SHADER = new ShaderProgram(vertexShader, geometryShader, fragmentShader);
    } catch(IOException ex) {
      throw new RuntimeException("Failed to load chunk shader", ex);
    }
  }

  public Block getBlockAt(int x, int y, int z) {
    return this.blocks[x][y][z];
  }

  public Block getBlockAt(Vector3i position) {
    return this.getBlockAt(position.x, position.y, position.z);
  }

  public Block getBlockAtWorldPosition(int x, int y, int z) {
    return this.getBlockAt(ChunkUtils.worldToChunkRelative(x, y, z));
  }

  public Block getBlockAtWorldPosition(Vector3i position) {
    return this.getBlockAtWorldPosition(position.x, position.y, position.z);
  }

  public Block getBlockAtWorldPosition(float x, float y, float z) {
    return this.getBlockAtWorldPosition(
        (int) Math.floor(x), (int) Math.floor(y), (int) Math.floor(z));
  }

  public Block getBlockAtWorldPosition(Vector3f pos) {
    return this.getBlockAtWorldPosition(pos.x, pos.y, pos.z);
  }

  public void removeBlock(int x, int y, int z) {
    this.blocks[x][y][z] = null;
    this.updateMesh(x, y, z, true);
  }

  public void removeBlock(Vector3i position) {
    this.removeBlock(position.x, position.y, position.z);
  }

  public void setBlock(Block block) {
    Vector3i chunkPos = block.chunkPosition();
    this.blocks[chunkPos.x][chunkPos.y][chunkPos.z] = block;
    this.updateMesh(chunkPos.x, chunkPos.y, chunkPos.z, true);
  }

  public void update(Vector3i position) {
    this.updateMesh(position.x, position.y, position.z, false);
  }

  public void update(int x, int y, int z) {
    this.updateMesh(x, y, z, false);
  }

  public Vector3ic chunkCoordinates() {
    return this.position;
  }

  public void rebuild() {
    this.rebuild(false);
  }

  /**
   * Rebuild the chunk mesh.
   * @param updateNeighbours Whether to update the neighbouring chunks
   */
  public void rebuild(boolean updateNeighbours) {
    this.vertices.clear();
    for (int x = 0; x < CHUNK_SIZE_X; x++) {
      for (int y = 0; y < CHUNK_SIZE_Y; y++) {
        for (int z = 0; z < CHUNK_SIZE_Z; z++) {
          this.updateMesh(x, y, z, false);
        }
      }
    }
    if(updateNeighbours) {
      Optional.ofNullable(this.level.chunk(this.position.x + 1, this.position.y, this.position.z, false))
        .ifPresent(Chunk::rebuild);
      Optional.ofNullable(this.level.chunk(this.position.x - 1, this.position.y, this.position.z, false))
        .ifPresent(Chunk::rebuild);
      Optional.ofNullable(this.level.chunk(this.position.x, this.position.y + 1, this.position.z, false))
        .ifPresent(Chunk::rebuild);
      Optional.ofNullable(this.level.chunk(this.position.x, this.position.y - 1, this.position.z, false))
        .ifPresent(Chunk::rebuild);
      Optional.ofNullable(this.level.chunk(this.position.x, this.position.y, this.position.z + 1, false))
        .ifPresent(Chunk::rebuild);
      Optional.ofNullable(this.level.chunk(this.position.x, this.position.y, this.position.z - 1, false))
        .ifPresent(Chunk::rebuild);
    }
  }

  /**
   * Update the mesh for the block at the given position.
   * @param x X coordinate
   * @param y Y coordinate
   * @param z Z coordinate
   * @param updateNeighbours Whether to update the neighbours of the block
   */
  private void updateMesh(int x, int y, int z, boolean updateNeighbours) {
    this.vertices.position((x * CHUNK_SIZE_Y * CHUNK_SIZE_Z + y * CHUNK_SIZE_Z + z) * 16);
    Block block = this.blocks[x][y][z];
    if (block == null) {
      this.vertices.putInt(0).putInt(0).putInt(0).putInt(0);
      this.vertices.position(0);
      return;
    }
    byte faceMask = this.checkVisibleFaces(x, y, z);
    if (faceMask == 0) {
      this.vertices.putInt(0).putInt(0).putInt(0).putInt(0);
      this.vertices.position(0);
      return;
    }
    this.vertices.put((byte) x).put((byte) y).put((byte) z).put(faceMask);
    this.vertices.putShort(
        (short) this.level.textureAtlas.getIndex(block.getFaceResource(BlockFace.UP)));
    this.vertices.putShort(
        (short) this.level.textureAtlas.getIndex(block.getFaceResource(BlockFace.DOWN)));
    this.vertices.putShort(
        (short) this.level.textureAtlas.getIndex(block.getFaceResource(BlockFace.NORTH)));
    this.vertices.putShort(
        (short) this.level.textureAtlas.getIndex(block.getFaceResource(BlockFace.SOUTH)));
    this.vertices.putShort(
        (short) this.level.textureAtlas.getIndex(block.getFaceResource(BlockFace.WEST)));
    this.vertices.putShort(
        (short) this.level.textureAtlas.getIndex(block.getFaceResource(BlockFace.EAST)));
    this.vertices.position(0);

    if(this.mesh != null) {
      this.mesh.markVerticesDirty();
    }

    if(updateNeighbours) {
      if (x + 1 < CHUNK_SIZE_X) {
        this.updateMesh(x + 1, y, z, false);
      } else {
        Optional.ofNullable(this.level.chunk(this.position.x + 1, this.position.y, this.position.z, false))
            .ifPresent(chunk -> chunk.updateMesh(0, y, z, false));
      }
      if (x - 1 >= 0) {
        this.updateMesh(x - 1, y, z, false);
      } else {
        Optional.ofNullable(this.level.chunk(this.position.x - 1, this.position.y, this.position.z, false))
            .ifPresent(chunk -> chunk.updateMesh(CHUNK_SIZE_X - 1, y, z, false));
      }
      if (y + 1 < CHUNK_SIZE_Y) {
        this.updateMesh(x, y + 1, z, false);
      } else {
        Optional.ofNullable(this.level.chunk(this.position.x, this.position.y + 1, this.position.z, false))
            .ifPresent(chunk -> chunk.updateMesh(x, 0, z, false));
      }
      if (y - 1 >= 0) {
        this.updateMesh(x, y - 1, z, false);
      } else {
        Optional.ofNullable(this.level.chunk(this.position.x, this.position.y - 1, this.position.z, false))
            .ifPresent(chunk -> chunk.updateMesh(x, CHUNK_SIZE_Y - 1, z, false));
      }
      if (z + 1 < CHUNK_SIZE_Z) {
        this.updateMesh(x, y, z + 1, false);
      } else {
        Optional.ofNullable(this.level.chunk(this.position.x, this.position.y, this.position.z + 1, false))
            .ifPresent(chunk -> chunk.updateMesh(x, y, 0, false));
      }
      if (z - 1 >= 0) {
        this.updateMesh(x, y, z - 1, false);
      } else {
        Optional.ofNullable(
                this.level.chunk(this.position.x, this.position.y, this.position.z - 1, false))
            .ifPresent(chunk -> chunk.updateMesh(x, y, CHUNK_SIZE_Z - 1, false));
      }
    }

  }

  /**
   * Check which faces of the block at the given position are visible.
   * @param x X coordinate
   * @param y Y coordinate
   * @param z Z coordinate
   * @return Bitmask of visible faces
   */
  private byte checkVisibleFaces(int x, int y, int z) {
    byte mask = 0;

    // EAST
    if (x + 1 < CHUNK_SIZE_X) {
      if (this.blocks[x + 1][y][z] == null || !this.blocks[x + 1][y][z].isSolid())
        mask |= BlockFace.EAST.bitMask();
    } else {
      Chunk chunk = this.level.chunk(this.position.x + 1, this.position.y, this.position.z, false);
      if (chunk != null) {
        if (chunk.getBlockAt(0, y, z) == null || !chunk.getBlockAt(0, y, z).isSolid())
          mask |= BlockFace.EAST.bitMask();
      } else mask |= BlockFace.EAST.bitMask();
    }

    // WEST
    if (x - 1 >= 0) {
      if (this.blocks[x - 1][y][z] == null || !this.blocks[x - 1][y][z].isSolid())
        mask |= BlockFace.WEST.bitMask();
    } else {
      Chunk chunk = this.level.chunk(this.position.x - 1, this.position.y, this.position.z, false);
      if (chunk != null) {
        if (chunk.getBlockAt(CHUNK_SIZE_X - 1, y, z) == null || !chunk.getBlockAt(CHUNK_SIZE_X - 1, y, z).isSolid())
          mask |= BlockFace.WEST.bitMask();
      } else mask |= BlockFace.WEST.bitMask();
    }

    // NORTH
    if (z + 1 < CHUNK_SIZE_Z) {
      if (this.blocks[x][y][z + 1] == null || !this.blocks[x][y][z + 1].isSolid())
        mask |= BlockFace.NORTH.bitMask();
    } else {
      Chunk chunk = this.level.chunk(this.position.x, this.position.y, this.position.z + 1, false);
      if (chunk != null) {
        if (chunk.getBlockAt(x, y, 0) == null || !chunk.getBlockAt(x, y, 0).isSolid())
          mask |= BlockFace.NORTH.bitMask();
      } else mask |= BlockFace.NORTH.bitMask();
    }

    // SOUTH
    if (z - 1 >= 0) {
      if (this.blocks[x][y][z - 1] == null || !this.blocks[x][y][z - 1].isSolid())
        mask |= BlockFace.SOUTH.bitMask();
    } else {
      Chunk chunk = this.level.chunk(this.position.x, this.position.y, this.position.z - 1, false);
      if (chunk != null) {
        if (chunk.getBlockAt(x, y, CHUNK_SIZE_Z - 1) == null || !chunk.getBlockAt(x, y, CHUNK_SIZE_Z - 1).isSolid())
          mask |= BlockFace.SOUTH.bitMask();
      } else mask |= BlockFace.SOUTH.bitMask();
    }

    // UP
    if (y + 1 < CHUNK_SIZE_Y) {
      if (this.blocks[x][y + 1][z] == null || !this.blocks[x][y + 1][z].isSolid())
        mask |= BlockFace.UP.bitMask();
    } else {
      Chunk chunk = this.level.chunk(this.position.x, this.position.y + 1, this.position.z, false);
      if (chunk != null) {
        if (chunk.getBlockAt(x, 0, z) == null || !chunk.getBlockAt(x, 0, z).isSolid())
          mask |= BlockFace.UP.bitMask();
      } else mask |= BlockFace.UP.bitMask();
    }

    // DOWN
    if (y - 1 >= 0) {
      if (this.blocks[x][y - 1][z] == null || !this.blocks[x][y - 1][z].isSolid())
        mask |= BlockFace.DOWN.bitMask();
    } else {
      Chunk chunk = this.level.chunk(this.position.x, this.position.y - 1, this.position.z, false);
      if (chunk != null) {
        if (chunk.getBlockAt(x, CHUNK_SIZE_Y - 1, z) == null || !chunk.getBlockAt(x, CHUNK_SIZE_Y - 1, z).isSolid())
          mask |= BlockFace.DOWN.bitMask();
      } else mask |= BlockFace.DOWN.bitMask();
    }
    return mask;
  }

  private void initMesh() {
    this.mesh =
        new ArrayMesh(
            this.vertices,
            PrimitiveType.POINTS,
            GLUsageHint.DRAW_STATIC,
            new VertexAttribute(3, DataType.UNSIGNED_BYTE, "aPosition"),
            new VertexAttribute(1, DataType.UNSIGNED_BYTE, "aFaces"),
            new VertexAttribute(3, DataType.UNSIGNED_INT, "aAtlasEntries"));
    this.mesh.position(new Vector3f(this.position.x * CHUNK_SIZE_X, this.position.y * CHUNK_SIZE_Y, this.position.z * CHUNK_SIZE_Z));
  }

  public void render(Camera<?> camera) {
    if(this.mesh == null) {
      this.initMesh();
    }
    this.mesh.render(camera, SHADER);
  }

}
