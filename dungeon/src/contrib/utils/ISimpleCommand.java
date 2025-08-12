package contrib.utils;

import core.Component;

/**
 * ICommand is an interface that represents a command that can be executed and undone.
 *
 * @see contrib.entities.LeverFactory LeverFactory
 * @see contrib.systems.LeverSystem LeverSystem
 */
public interface ISimpleCommand extends IComponentCommand {

  /** Command that does nothing. */
  ISimpleCommand NOOP =
      new ISimpleCommand() {
        @Override
        public void execute() {}

        @Override
        public void undo() {}
      };

  /** Executes the command. */
  void execute();

  /** Undoes the command. */
  void undo();

  @Override
  default void execute(Component comp) {}

  @Override
  default void undo(Component comp) {}
}
