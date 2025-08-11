package contrib.systems;

import contrib.components.LeverComponent;
import contrib.components.PressurePlateComponent;
import core.Entity;
import core.System;
import core.utils.components.MissingComponentException;

/**
 * The PressurePlateSystem manages the interaction between pressure plates and levers.
 *
 * <p>This system processes all entities that have both {@link PressurePlateComponent} and {@link
 * LeverComponent}. For each such entity, it checks if the pressure plate is currently triggered
 * (i.e., the total mass on the plate meets or exceeds the trigger threshold). If triggered and the
 * lever is off, the lever is toggled on. Conversely, if not triggered and the lever is on, it is
 * toggled off.
 *
 * <p>The logic to accumulate and remove mass on the pressure plate is expected to be handled
 * elsewhere, for example via collision event handlers.
 *
 * @see PressurePlateComponent
 * @see LeverComponent
 */
public class PressurePlateSystem extends System {

  /**
   * Constructs a new PressurePlateSystem that processes entities with pressure plates and levers.
   */
  public PressurePlateSystem() {
    super(PressurePlateComponent.class, LeverComponent.class);
  }

  /**
   * Executes the system logic for all filtered entities.
   *
   * <p>For each entity with both pressure plate and lever components, toggles the lever state
   * according to whether the pressure plate is triggered.
   */
  @Override
  public void execute() {
    filteredEntityStream().map(this::buildDataObject).forEach(this::handleData);
  }

  /**
   * Processes the entity and its components bundled in {@link PPData}. Toggles the lever on if the
   * pressure plate is triggered and lever is off, and toggles it off if the pressure plate is not
   * triggered and lever is on.
   *
   * @param data the PPData containing the entity and its components
   */
  private void handleData(PPData data) {
    boolean triggered = data.pressurePlate().isTriggered();
    LeverComponent lever = data.lever();

    if (triggered && !lever.isOn()) {
      lever.toggle();
    } else if (!triggered && lever.isOn()) {
      lever.toggle();
    }
  }

  /**
   * Builds a data record with the entity and its required components. Throws {@link
   * MissingComponentException} if any component is missing.
   *
   * @param e the entity to process
   * @return a PPData record bundling entity and components
   */
  private PPData buildDataObject(Entity e) {
    PressurePlateComponent pressurePlate =
        e.fetch(PressurePlateComponent.class)
            .orElseThrow(() -> MissingComponentException.build(e, PressurePlateComponent.class));

    LeverComponent lever =
        e.fetch(LeverComponent.class)
            .orElseThrow(() -> MissingComponentException.build(e, LeverComponent.class));

    return new PPData(e, pressurePlate, lever);
  }

  /**
   * Record bundling entity and its components for processing in PressurePlateSystem.
   *
   * @param entity the entity
   * @param pressurePlate the pressure plate component
   * @param lever the lever component
   */
  private record PPData(
      Entity entity, PressurePlateComponent pressurePlate, LeverComponent lever) {}
}
