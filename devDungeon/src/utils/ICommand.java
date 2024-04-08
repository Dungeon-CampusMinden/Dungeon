package utils;

/**
 * ICommand is an interface that represents a command that can be executed and undone.
 *
 * @see entities.LeverFactory LeverFactory
 * @see systems.LeverSystem LeverSystem
 */
public interface ICommand {

  /** Executes the command. */
  void execute();

  /** Undoes the command. */
  void undo();
}
