package dungine.level.level3d.block;

import de.fwatermann.dungine.resource.Resource;
import dungine.level.level3d.Chunk;
import org.joml.Vector3i;

public class WallBlock extends Block {

  public WallBlock(Chunk chunk, Vector3i chunkPosition) {
    super(chunk, chunkPosition, Resource.load("/textures/wall.png"));
  }

  @Override
  public boolean isSolid() {
    return true;
  }
}
