package feature.utils;

/**
 * ICommand is an interface that represents a command that can be executed and undone.
 *
 * @see feature.entities.LeverFactory LeverFactory
 * @see feature.systems.LeverSystem LeverSystem
 */
public interface ICommand {

  /** Command that does nothing. */
  ICommand NOOP =
      new ICommand() {
        @Override
        public void execute() {}

        @Override
        public void undo() {}
      };

  /** Executes the command. */
  void execute();

  /** Undoes the command. */
  void undo();
}
