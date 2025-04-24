package contrib.utils;

import contrib.systems.EventScheduler;

/**
 * IAction is a functional interface that defines a single method, execute, which is used to
 * represent an action that can be executed. This interface is primarily used in the context of
 * scheduling actions to be performed at a later time via the {@link EventScheduler}.
 *
 * @see EventScheduler EventScheduler
 */
@FunctionalInterface
public interface IAction {
  /**
   * Callback method that should get called when the action is executed via the {@link
   * EventScheduler}.
   */
  void execute();
}
