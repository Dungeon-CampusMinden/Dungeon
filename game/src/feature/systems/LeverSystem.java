package feature.systems;

import engine.Entity;
import engine.System;
import engine.components.DrawComponent;
import engine.utils.components.MissingComponentException;
import feature.components.LeverComponent;
import feature.entities.LeverFactory;
import feature.utils.ICommand;
import feature.utils.IEntityCommand;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * The LeverSystem class is responsible for managing the state of levers in the game. It listens for
 * changes in the state of levers and executes the appropriate command when a lever is toggled.
 *
 * @see LeverFactory LeverFactory
 * @see LeverComponent LeverComponent
 * @see ICommand ICommand
 */
public class LeverSystem extends System {

  private final Map<Entity, Boolean> leverStates;

  /**
   * Constructs a new LeverSystem.
   *
   * @see LeverFactory LeverFactory
   * @see LeverComponent LeverComponent
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
   * @see engine.Game#userOnLevelLoad(Consumer) onLevelLoad
   */
  public void clear() {
    leverStates.clear();
  }

  @Override
  public void execute() {
    filteredEntityStream()
        .forEach(
            leverEntity -> {
              LeverComponent leverComp =
                  leverEntity
                      .fetch(LeverComponent.class)
                      .orElseThrow(
                          () -> MissingComponentException.build(leverEntity, LeverComponent.class));

              // Compare unseen levers with their initial state, so a toggle right after spawning
              // still runs the command.
              boolean oldState = leverStates.getOrDefault(leverEntity, leverComp.initiallyOn());
              boolean newState = leverComp.isOn();

              if (oldState == newState) {
                leverStates.put(leverEntity, newState);
                return;
              }

              ICommand cmd = leverComp.command();
              if (newState) {
                if (cmd instanceof IEntityCommand c) c.execute(leverEntity);
                else cmd.execute();
              } else {
                if (cmd instanceof IEntityCommand c) c.undo(leverEntity);
                else cmd.undo();
              }

              leverEntity
                  .fetch(DrawComponent.class)
                  .ifPresent(d -> d.sendSignal(newState ? "on" : "off"));

              leverStates.put(leverEntity, newState);
            });
  }
}
