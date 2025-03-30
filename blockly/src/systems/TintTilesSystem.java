package systems;

import components.TintRangeComponent;
import core.Entity;
import core.System;
import core.utils.components.MissingComponentException;

/**
 * The TintTilesSystem is responsible for applying a tinting effect to tiles in the range of an
 * entity.
 *
 * <p>Entities with the {@link TintRangeComponent} will be processed by this system.
 *
 * @see TintRangeComponent
 * @see utils.components.ai.fight.StraightRangeAI StraightRangeAI
 */
public class TintTilesSystem extends System {

  /**
   * Creates a new TintTilesSystem.
   *
   * <p>This system processes all entities with a {@link TintRangeComponent} and applies the tinting
   * effect to the tiles in the range of the entity.
   *
   * <p>It also removes the tinting effect when the entity is removed.
   */
  public TintTilesSystem() {
    super(TintRangeComponent.class);

    onEntityRemove = this::removeTint;
  }

  /** Implements the functionality of the system. */
  @Override
  public void execute() {
    filteredEntityStream()
        .map(
            entity ->
                entity
                    .fetch(TintRangeComponent.class)
                    .orElseThrow(
                        () -> MissingComponentException.build(entity, TintRangeComponent.class)))
        .forEach(TintRangeComponent::applyTint);
  }

  private void removeTint(Entity entity) {
    entity
        .fetch(TintRangeComponent.class)
        .orElseThrow(() -> MissingComponentException.build(entity, TintRangeComponent.class))
        .removeTint();
  }
}
