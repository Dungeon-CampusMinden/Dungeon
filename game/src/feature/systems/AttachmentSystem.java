package feature.systems;

import engine.Entity;
import engine.Game;
import engine.System;
import engine.components.PositionComponent;
import feature.components.AttachmentComponent;
import feature.utils.EntityUtils;

/**
 * Moves every entity with an {@link AttachmentComponent} to the position of its target plus the
 * offset, and removes it once the target has left the game.
 *
 * <p>Runs after the {@link engine.systems.MoveSystem} and the {@link CollisionSystem}, so attached
 * entities follow the final position of their target in the current tick.
 */
public class AttachmentSystem extends System {

  /** Creates a new AttachmentSystem. */
  public AttachmentSystem() {
    super(AuthoritativeSide.SERVER, AttachmentComponent.class, PositionComponent.class);
  }

  @Override
  public void execute() {
    filteredEntityStream().forEach(this::follow);
  }

  private void follow(Entity entity) {
    AttachmentComponent ac = entity.fetch(AttachmentComponent.class).orElseThrow();
    if (!Game.existInGame(ac.target())) {
      Game.remove(entity);
      return;
    }
    entity
        .fetch(PositionComponent.class)
        .orElseThrow()
        .position(EntityUtils.getPosition(ac.target()).translate(ac.offset()));
  }
}
