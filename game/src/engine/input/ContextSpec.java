package engine.input;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Declaration of an input context, shared by all its instances.
 *
 * @param name name shown in logs and in the debug HUD
 * @param layer vertical position in the context stack
 * @param blocksBelow true if input not taken here must not reach lower contexts
 * @param actions actions this context takes
 * @param closing actions of this context that close it; they may share bindings with each other
 */
public record ContextSpec(
    String name,
    ContextLayer layer,
    boolean blocksBelow,
    Set<ActionId> actions,
    Set<ActionId> closing) {

  /**
   * Creates a specification of a context that lets input it does not take pass.
   *
   * @param name name shown in logs and in the debug HUD
   * @param layer vertical position in the context stack
   * @param actions actions this context takes
   * @return the specification
   */
  public static ContextSpec of(String name, ContextLayer layer, ActionId... actions) {
    return new ContextSpec(name, layer, false, Set.of(actions), Set.of());
  }

  /**
   * Returns a copy that keeps all input from the contexts below.
   *
   * @return the copy
   */
  public ContextSpec blocking() {
    return new ContextSpec(name, layer, true, actions, closing);
  }

  /**
   * Returns a copy that also takes the given actions and closes through them. A UI lists the action
   * that opens it here, so that the same input closes it again.
   *
   * @param ids the closing actions
   * @return the copy
   */
  public ContextSpec closedBy(ActionId... ids) {
    Set<ActionId> all = new HashSet<>(actions);
    all.addAll(List.of(ids));
    return new ContextSpec(name, layer, blocksBelow, Set.copyOf(all), Set.of(ids));
  }
}
