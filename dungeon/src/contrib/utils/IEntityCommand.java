package contrib.utils;

import core.Entity;

/**
 * IEntityCommand is an interface that represents a command that can be executed and undone, with an
 * entity as context. It extends the ICommand interface.
 */
public interface IEntityCommand extends ICommand {

  /** Command that does nothing. */
  IEntityCommand NOOP =
      new IEntityCommand() {
        @Override
        public void execute(Entity entity) {}

        @Override
        public void undo(Entity entity) {}
      };

  /**
   * Executes the command.
   *
   * @param entity The entity that is the context for this command.
   */
  void execute(Entity entity);

  /**
   * Undoes the command.
   *
   * @param entity The entity that is the context for this command.
   */
  void undo(Entity entity);

  /**
   * The default implementation of the execute method from the ICommand interface. It does nothing.
   */
  @Override
  default void execute() {}

  /** The default implementation of the undo method from the ICommand interface. It does nothing. */
  @Override
  default void undo() {}
}
