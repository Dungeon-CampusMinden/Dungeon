package dungine.level.level3d.block;

import de.fwatermann.dungine.resource.Resource;
import dungine.level.level3d.Chunk;
import org.joml.Vector3i;

public class FloorDamagedBlock extends FloorBlock {

  public FloorDamagedBlock(Chunk chunk, Vector3i chunkPosition) {
    super(chunk, chunkPosition, Resource.load("/textures/floor_damaged.png"));
  }

  @Override
  public boolean isSolid() {
    return true;
  }
}
