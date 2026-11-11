package engine.input;

import java.util.HashMap;
import java.util.Map;

/**
 * One entry of the context stack. Contexts have an identity: each open dialog owns its own
 * instance, and removing one never touches another.
 *
 * <p>A context may react to its actions through handlers. Handlers run on the game thread when the
 * press reaches the current frame, and only if this context owns the press. Everything else polls
 * {@link GameInput}.
 */
public final class InputContext {
  private final ContextSpec spec;
  private final Map<ActionId, Runnable> pressHandlers = new HashMap<>();

  /**
   * Creates a context.
   *
   * @param spec the declaration of this context
   */
  public InputContext(ContextSpec spec) {
    this.spec = spec;
  }

  /**
   * Gets the declaration of this context.
   *
   * @return the specification
   */
  public ContextSpec spec() {
    return spec;
  }

  /**
   * Registers the reaction to a press of an action owned by this context.
   *
   * @param action the action
   * @param handler the reaction
   * @return this context
   */
  public InputContext onPress(ActionId action, Runnable handler) {
    pressHandlers.put(action, handler);
    return this;
  }

  void firePress(ActionId action) {
    Runnable handler = pressHandlers.get(action);
    if (handler != null) handler.run();
  }

  @Override
  public String toString() {
    return spec.name();
  }
}
