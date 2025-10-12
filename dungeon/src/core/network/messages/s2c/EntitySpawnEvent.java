package core.network.messages.s2c;

import core.Entity;
import core.components.DrawComponent;
import core.components.PlayerComponent;
import core.components.PositionComponent;
import core.network.messages.NetworkMessage;

public record EntitySpawnEvent(
    int entityId,
    PositionComponent positionComponent,
    DrawComponent drawComponent,
    boolean isPersistent,
    // For Heros:
    PlayerComponent playerComponent)
    implements NetworkMessage {

  public EntitySpawnEvent(
      int entityId,
      PositionComponent positionComponent,
      DrawComponent drawComponent,
      boolean isPersistent) {
    this(entityId, positionComponent, drawComponent, isPersistent, null);
  }

  public EntitySpawnEvent(Entity entity) {
    this(
        entity.id(),
        entity.fetch(PositionComponent.class).orElseThrow(),
        entity.fetch(DrawComponent.class).orElseThrow(),
        entity.isPersistent(),
        entity.fetch(PlayerComponent.class).orElse(null));
  }
}
