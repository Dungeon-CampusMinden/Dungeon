package systems;

import components.LeverComponent;
import core.Entity;
import core.System;
import core.utils.components.MissingComponentException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * The LeverSystem class is responsible for managing the state of levers in the game. It listens for
 * changes in the state of levers and executes the appropriate command when a lever is toggled.
 *
 * @see entities.LeverFactory LeverFactory
 * @see components.LeverComponent LeverComponent
 * @see utils.ICommand ICommand
 */
public class LeverSystem extends System {

  private final Map<Entity, Boolean> leverStates;

  /**
   * Constructs a new LeverSystem.
   *
   * @see entities.LeverFactory LeverFactory
   * @see components.LeverComponent LeverComponent
   */
  public LeverSystem() {
    super(LeverComponent.class);
    this.leverStates = new HashMap<>();
  }

  /**
   * Clears the lever states.
   *
   * <p>This method is called when the level is changed to clean up the lever states. As those lever
   * states are not needed anymore.
   *
   * @see core.Game#userOnLevelLoad(Consumer) onLevelLoad
   */
  public void clear() {
    this.leverStates.clear();
  }

  @Override
  public void execute() {
    this.filteredEntityStream()
        .forEach(
            entity -> {
              LeverComponent lever =
                  entity
                      .fetch(LeverComponent.class)
                      .orElseThrow(
                          () -> MissingComponentException.build(entity, LeverComponent.class));
              if (this.leverStates.containsKey(entity)) {
                if (this.leverStates.get(entity) != lever.isOn()) {
                  if (lever.isOn()) {
                    lever.command().execute();
                  } else {
                    lever.command().undo();
                  }
                  this.leverStates.put(entity, lever.isOn());
                }
              } else {
                this.leverStates.put(entity, lever.isOn());
              }
            });
  }
}
