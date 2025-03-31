package systems;

import components.TintViewDirectionComponent;
import core.Entity;
import core.System;
import core.utils.components.MissingComponentException;

/**
 * The TintTilesSystem is responsible for applying a tinting effect to tiles in the range of an
 * entity.
 *
 * <p>Entities with the {@link TintViewDirectionComponent} will be processed by this system.
 *
 * @see TintViewDirectionComponent
 * @see utils.components.ai.fight.StraightRangeAI StraightRangeAI
 */
public class TintTilesSystem extends System {

  /**
   * Creates a new TintTilesSystem.
   *
   * <p>This system processes all entities with a {@link TintViewDirectionComponent} and applies the
   * tinting effect to the tiles in the range of the entity.
   *
   * <p>It also removes the tinting effect when the entity is removed.
   */
  public TintTilesSystem() {
    super(TintViewDirectionComponent.class);

    onEntityRemove = this::removeTint;
  }

  /** Implements the functionality of the system. */
  @Override
  public void execute() {
    filteredEntityStream()
        .map(
            entity ->
                entity
                    .fetch(TintViewDirectionComponent.class)
                    .orElseThrow(
                        () ->
                            MissingComponentException.build(
                                entity, TintViewDirectionComponent.class)))
        .forEach(TintViewDirectionComponent::applyTint);
  }

  private void removeTint(Entity entity) {
    entity
        .fetch(TintViewDirectionComponent.class)
        .orElseThrow(
            () -> MissingComponentException.build(entity, TintViewDirectionComponent.class))
        .removeTint();
  }
}
