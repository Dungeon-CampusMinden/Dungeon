package contrib.utils;

import core.Component;

public interface IComponentCommand<T extends Component> {

  IComponentCommand NOOP =
      new IComponentCommand<>() {
        @Override
        public void execute(Component comp) {}

        @Override
        public void undo(Component comp) {}
      };

  /** Executes the command on the given component. */
  void execute(T comp);

  /**
   * Undoes the command on the given entity.
   *
   * <p>Default implementation does nothing.
   */
  void undo(T comp);
}
