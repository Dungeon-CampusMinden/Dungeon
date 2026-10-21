package escaperoom.foundation.definition;

import feature.tasks.Task;

/**
 * Input answered at its own placed device surface and evaluated by its task.
 *
 * @param <A> the answer type
 */
public sealed interface SurfaceInputDefinition<A> extends InputDefinition
    permits AssignmentInputDefinition, ChoiceInputDefinition, NumericInputDefinition {
  /**
   * Returns the authored device surface identifier.
   *
   * @return stable surface identifier
   */
  String surfaceId();

  /**
   * Returns the task that evaluates and tracks answers.
   *
   * @return host-owned task
   */
  Task<A> task();
}
