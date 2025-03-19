package contrib.utils;

/**
 * ICommand is an interface that represents a command that can be executed and undone.
 *
 * @see contrib.entities.LeverFactory LeverFactory
 * @see contrib.systems.LeverSystem LeverSystem
 */
public interface ICommand {

  /** Command that does nothing. */
  ICommand EMPTY_COMMAND = () -> {};

  /** Executes the command. */
  void execute();

  /** Undoes the command. */
  default void undo() {}
}
