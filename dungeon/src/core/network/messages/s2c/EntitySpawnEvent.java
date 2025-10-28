package core.network.messages.s2c;

import core.Entity;
import core.components.DrawComponent;
import core.components.PlayerComponent;
import core.components.PositionComponent;
import core.network.messages.NetworkMessage;
import core.network.messages.c2s.RequestEntitySpawn;

/**
 * Server→client: spawn entity.
 *
 * <p>Expected max size: small (<= 256 bytes).
 *
 * <p>This should be sent in response to a {@link RequestEntitySpawn} message and if a new entity is
 * created on the server that the client needs to know about.
 *
 * @param entityId the entity's unique ID
 * @param positionComponent the entity's position component
 * @param drawComponent the entity's draw component
 * @param isPersistent whether the entity should be saved to the map
 * @param playerComponent the entity's player component, if it has one (null if not)
 */
public record EntitySpawnEvent(
    int entityId,
    PositionComponent positionComponent,
    DrawComponent drawComponent,
    boolean isPersistent,
    // For Heros:
    PlayerComponent playerComponent)
    implements NetworkMessage {

  /**
   * Constructor without PlayerComponent (for non-player entities).
   *
   * @param entityId the entity's unique ID
   * @param positionComponent the entity's position component
   * @param drawComponent the entity's draw component
   * @param isPersistent whether the entity should be saved to the map
   */
  public EntitySpawnEvent(
      int entityId,
      PositionComponent positionComponent,
      DrawComponent drawComponent,
      boolean isPersistent) {
    this(entityId, positionComponent, drawComponent, isPersistent, null);
  }

  /**
   * Constructor from Entity object.
   *
   * <p>This will throw {@link java.util.NoSuchElementException} if the entity does not have {@link
   * PositionComponent} or {@link DrawComponent}.
   *
   * @param entity the entity to create the event from
   */
  public EntitySpawnEvent(Entity entity) {
    this(
        entity.id(),
        entity.fetch(PositionComponent.class).orElseThrow(),
        entity.fetch(DrawComponent.class).orElseThrow(),
        entity.isPersistent(),
        entity.fetch(PlayerComponent.class).orElse(null));
  }
}
