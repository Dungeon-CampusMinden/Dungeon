package core.network.messages.s2c;

import contrib.components.CharacterClassComponent;
import core.Entity;
import core.components.DrawComponent;
import core.components.PlayerComponent;
import core.components.PositionComponent;
import core.network.messages.NetworkMessage;
import core.network.messages.c2s.RequestEntitySpawn;
import core.utils.components.draw.DrawComponentFactory;
import core.utils.components.draw.DrawInfoData;

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
 * @param drawInfo the entity's draw info (data-only, render thread builds component)
 * @param isPersistent whether the entity should be saved to the map
 * @param playerComponent the entity's player component, if it has one (null if not)
 * @param characterClassId the entity's character class ID, if it has one (0 if not)
 * @see CharacterClassComponent CharacterClassComponent for mapping characterClassId to
 *     CharacterClass
 */
public record EntitySpawnEvent(
    int entityId,
    PositionComponent positionComponent,
    DrawInfoData drawInfo,
    boolean isPersistent,
    // For Player entities (Hero):
    PlayerComponent playerComponent,
    byte characterClassId)
    implements NetworkMessage {

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
        DrawComponentFactory.toDrawInfo(entity.fetch(DrawComponent.class).orElseThrow()),
        entity.isPersistent(),
        entity.fetch(PlayerComponent.class).orElse(null),
        entity
            .fetch(CharacterClassComponent.class)
            .map(ccc -> (byte) ccc.characterClass().ordinal())
            .orElse((byte) 0));
  }
}
