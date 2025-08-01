package contrib.components;

import contrib.systems.LeverSystem;
import contrib.utils.ICommand;
import core.Component;

/**
 * The LeverComponent class implements the Component interface. It represents a lever that can be
 * either on or off.
 */
public class LeverComponent implements Component {
  /** The command that will be executed when the lever is toggled. */
  private final ICommand command;

  /** The current state of the lever. True if the lever is on, false otherwise. */
  private boolean isOn;

  /**
   * Constructs a new LeverComponent with the specified initial state and command.
   *
   * <p>The given `command` can be a simple {@link ICommand} or if needed an {@link *
   * IEntityCommand} that takes the lever entity as context.
   *
   * @param isOn The initial state of the lever. True if the lever should start in the on position,
   *     false otherwise.
   * @param command The command that will be executed when the lever is toggled.
   */
  public LeverComponent(boolean isOn, ICommand command) {
    this.isOn = isOn;
    this.command = command;
  }

  /**
   * Constructs a new LeverComponent with the specified command. The lever will start in the off
   * position.
   *
   * <p>The given `command` can be a simple {@link ICommand} or if needed an {@link *
   * IEntityCommand} that takes the lever entity as context.
   *
   * @param command The command that will be executed when the lever is toggled.
   */
  public LeverComponent(ICommand command) {
    this(false, command);
  }

  /**
   * Returns the command that will be executed when the lever is toggled.
   *
   * @return The command that will be executed when the lever is toggled.
   */
  public ICommand command() {
    return command;
  }

  /**
   * This method returns the current state of the lever.
   *
   * @return A boolean representing the state of the lever. True if the lever is on, false
   *     otherwise.
   */
  public boolean isOn() {
    return isOn;
  }

  /**
   * This method, toggles the `isOn` state of the lever. If the lever is currently on, it will be
   * turned off, and if it is currently off, it will be turned on.
   *
   * <p>The associated command will be executed by the {@link LeverSystem} when the lever is
   * toggled.
   */
  public void toggle() {
    this.isOn = !isOn;
  }

  @Override
  public String toString() {
    return "LeverComponent{"
        + "isOn="
        + isOn
        + ", Command="
        + command.getClass().getSimpleName()
        + '}';
  }
}
