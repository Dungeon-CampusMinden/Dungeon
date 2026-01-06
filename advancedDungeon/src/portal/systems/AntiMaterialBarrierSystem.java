package portal.systems;

import contrib.components.CollideComponent;
import core.Entity;
import core.System;
import core.components.DrawComponent;
import core.utils.components.MissingComponentException;
import portal.antiMaterialBarrier.AntiMaterialBarrier;
import portal.components.AntiMaterialBarrierComponent;

/**
 * The AntiMaterialBarrierSystem manages the activation and deactivation of anti-material barriers.
 *
 * <p>This system processes all entities that have an {@link AntiMaterialBarrierComponent}. For each
 * such entity, it checks whether the barrier should be active. If active, the system ensures that
 * the entity visually displays the barrier and applies the desired gameplay logic (e.g., blocking
 * projectiles). If inactive, the system updates the visuals and removes related gameplay effects.
 *
 * <p>The actual gameplay logic is handled in the {@link #applyBarrierLogic(BarrierSystemData)}
 * method, which can be customized.
 *
 * @see AntiMaterialBarrierComponent
 * @see CollideComponent
 * @see DrawComponent
 */
public class AntiMaterialBarrierSystem extends System {

  /**
   * Constructs a new AntiMaterialBarrierSystem that processes entities with an anti-material
   * barrier.
   */
  public AntiMaterialBarrierSystem() {
    super(AntiMaterialBarrierComponent.class);
  }

  /**
   * Executes the system logic for all filtered entities.
   *
   * <p>For each entity with an {@link AntiMaterialBarrierComponent}, this method builds a data
   * bundle and applies the barrier logic to synchronize its state with visuals and gameplay
   * components.
   */
  @Override
  public void execute() {
    filteredEntityStream().map(this::buildDataObject).forEach(this::applyBarrierLogic);
  }

  /**
   * Builds a data record with the entity and its relevant components. Throws {@link
   * MissingComponentException} if any required component is missing.
   *
   * @param entity the entity to process
   * @return a BarrierSystemData record bundling entity and components
   */
  private BarrierSystemData buildDataObject(Entity entity) {
    AntiMaterialBarrierComponent barrier =
        entity
            .fetch(AntiMaterialBarrierComponent.class)
            .orElseThrow(
                () -> MissingComponentException.build(entity, AntiMaterialBarrierComponent.class));

    DrawComponent draw =
        entity
            .fetch(DrawComponent.class)
            .orElseThrow(() -> MissingComponentException.build(entity, DrawComponent.class));

    CollideComponent collide = entity.fetch(CollideComponent.class).orElse(null);

    return new BarrierSystemData(entity, barrier, draw, collide);
  }

  /**
   * Applies the anti-material barrier logic to the given data object.
   *
   * <p>If the barrier is active, a visual activation signal is sent and the desired gameplay logic
   * is applied. If inactive, a deactivation signal is sent and the related components are updated
   * or removed.
   *
   * <p><b>Note:</b> The specific logic should be implemented here based on the intended gameplay
   * behavior of the anti-material barrier.
   *
   * @param data the BarrierSystemData containing the entity and its components
   */
  private void applyBarrierLogic(BarrierSystemData data) {
    String currentState = data.draw().currentStateName();
    if (data.barrier().isActive()) {
      if (currentState.equals("horizontal_off") || currentState.equals("vertical_off")) {
        data.draw().sendSignal("activate_anti_barrier");

        CollideComponent colComp = AntiMaterialBarrier.getCollideComponent();
        data.entity().add(colComp);
      }
    } else {
      if (currentState.equals("horizontal_on") || currentState.equals("vertical_on")) {
        data.draw().sendSignal("deactivate_anti_barrier");
        data.entity().remove(CollideComponent.class);
      }
    }
  }

  private record BarrierSystemData(
      Entity entity,
      AntiMaterialBarrierComponent barrier,
      DrawComponent draw,
      CollideComponent collide) {}
}
