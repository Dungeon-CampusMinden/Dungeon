package dungine.systems;

import de.fwatermann.dungine.ecs.ECS;
import de.fwatermann.dungine.ecs.System;
import dungine.components.VelocityComponent;
import dungine.level.level3d.Chunk;
import dungine.level.level3d.Level3D;
import dungine.level.level3d.block.Block;
import dungine.level.level3d.utils.ChunkUtils;
import org.joml.Vector3f;
import org.joml.Vector3i;

public class VelocitySystem extends System<VelocitySystem> {

  private long lastExecution = 0L;

  public Level3D level;

  @Override
  public void update(ECS ecs) {
    float tmpTime = 0.0f;
    if (this.lastExecution != 0L) {
      tmpTime = (java.lang.System.nanoTime() - this.lastExecution) / 1000000000.0f;
    }
    this.lastExecution = java.lang.System.nanoTime();
    float deltaTime = tmpTime;

    ecs.forEachEntity(
        (entity) -> {
          VelocityComponent velocity = entity.component(VelocityComponent.class).get();

          velocity.currentVelocity.mul(0.85f);
          velocity.currentVelocity.add(velocity.force);
          velocity.force.zero();

          float speed = velocity.currentVelocity.length();
          if (speed > velocity.maxSpeed) {
            velocity.currentVelocity.normalize().mul(velocity.maxSpeed);
          } else if (speed < 0.01f) {
            velocity.currentVelocity.zero();
          }

          Vector3f newPos =
              entity
                  .position()
                  .add(velocity.currentVelocity.mul(deltaTime, new Vector3f()), new Vector3f());
          // Check if accessible
          if (this.level != null) {
            Vector3i pos =
                new Vector3i(
                    (int) Math.floor(newPos.x),
                    (int) Math.floor(newPos.y),
                    (int) Math.floor(newPos.z));
            Vector3i chunkPos = ChunkUtils.worldToChunkRelative(pos);
            Chunk chunk = this.level.chunkByWorldCoordinates(pos.x, pos.y, pos.z, false);
            if (chunk == null) {
              return;
            }
            Block block = chunk.getBlockAt(chunkPos);
            if (block != null && block.isSolid()) {
              return;
            }
          }
          entity.position().set(newPos);
        },
        VelocityComponent.class);
  }
}
