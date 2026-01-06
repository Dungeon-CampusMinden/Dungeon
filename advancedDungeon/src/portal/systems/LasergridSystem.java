package portal.systems;

import contrib.components.CollideComponent;
import contrib.components.SpikyComponent;
import contrib.utils.components.health.DamageType;
import core.Entity;
import core.System;
import core.components.DrawComponent;
import core.utils.components.MissingComponentException;
import portal.components.LasergridComponent;

/**
 * The LasergridSystem manages the activation and deactivation of laser grids.
 *
 * <p>This system processes all entities that have a {@link LasergridComponent}. For each such
 * entity, it checks whether the laser grid should be active. If active, the system ensures that the
 * corresponding entity visually displays the laser grid and becomes hazardous by adding a {@link
 * SpikyComponent} and a non-solid {@link CollideComponent}. If inactive, the system removes these
 * components and updates the visual representation to indicate deactivation.
 *
 * <p>The system thereby synchronizes the desired state of the laser grid (active/inactive) with
 * both its visual and gameplay effects (damage and collision).
 *
 * @see LasergridComponent
 * @see SpikyComponent
 * @see CollideComponent
 * @see DrawComponent
 */
public class LasergridSystem extends System {

  /** Constructs a new LasergridSystem that processes entities with a laser grid. */
  public LasergridSystem() {
    super(LasergridComponent.class);
  }

  /**
   * Executes the system logic for all filtered entities.
   *
   * <p>For each entity with a {@link LasergridComponent}, this method builds a data bundle and
   * applies the laser grid logic to synchronize its state with visuals and gameplay components.
   */
  @Override
  public void execute() {
    filteredEntityStream().map(this::buildDataObject).forEach(this::applyLaserLogic);
  }

  /**
   * Builds a data record with the entity and its relevant components. Throws {@link
   * MissingComponentException} if any required component is missing.
   *
   * @param entity the entity to process
   * @return a LaserSystemData record bundling entity and components
   */
  private LaserSystemData buildDataObject(Entity entity) {
    LasergridComponent laser =
        entity
            .fetch(LasergridComponent.class)
            .orElseThrow(() -> MissingComponentException.build(entity, LasergridComponent.class));

    DrawComponent draw =
        entity
            .fetch(DrawComponent.class)
            .orElseThrow(() -> MissingComponentException.build(entity, DrawComponent.class));

    SpikyComponent spiky = entity.fetch(SpikyComponent.class).orElse(null);

    return new LaserSystemData(entity, laser, draw, spiky);
  }

  /**
   * Applies the laser grid logic to the given data object.
   *
   * <p>If the laser grid is active, a visual activation signal is sent, and the entity is equipped
   * with a non-solid {@link CollideComponent} and a {@link SpikyComponent} that inflicts physical
   * damage. If the laser grid is inactive, a deactivation signal is sent, and the corresponding
   * components are removed to make the entity non-hazardous.
   *
   * @param data the LaserSystemData containing the entity and its components
   */
  private void applyLaserLogic(LaserSystemData data) {
    String currentState = data.draw().currentStateName();
    if (data.lasergrid().isActive()) {
      // activate laser grid
      if (currentState.equals("horizontal_off") || currentState.equals("vertical_off")) {
        data.draw().sendSignal("activate_laser_grid");
        CollideComponent colComp = new CollideComponent();
        colComp.isSolid(false);
        data.entity().add(colComp);
        data.entity().add(new SpikyComponent(9999, DamageType.PHYSICAL, 10));
      }
    } else {
      // deactivate laser grid
      if (currentState.equals("horizontal_on") || currentState.equals("vertical_on")) {
        data.draw().sendSignal("deactivate_laser_grid");
        data.entity().remove(SpikyComponent.class);
        data.entity().remove(CollideComponent.class);
      }
    }
  }

  private record LaserSystemData(
      Entity entity, LasergridComponent lasergrid, DrawComponent draw, SpikyComponent spiky) {}
}
