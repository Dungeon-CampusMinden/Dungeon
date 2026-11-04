package engine.input;

import java.util.Arrays;
import java.util.Set;

/**
 * Declaration of an input context, shared by all its instances.
 *
 * @param name name shown in logs and in the debug HUD
 * @param layer vertical position in the context stack
 * @param blocksBelow true if input not taken here must not reach lower contexts
 * @param actions actions this context takes
 * @param keyboardOnly actions of this context that ignore their pad bindings here, because the pad
 *     button has another meaning in this context
 */
public record ContextSpec(
    String name,
    ContextLayer layer,
    boolean blocksBelow,
    Set<ActionId> actions,
    Set<ActionId> keyboardOnly) {

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
    return new ContextSpec(name, layer, true, actions, keyboardOnly);
  }

  /**
   * Returns a copy whose given actions ignore their pad bindings.
   *
   * @param ids the keyboard-only actions
   * @return the copy
   */
  public ContextSpec keyboardOnly(ActionId... ids) {
    return new ContextSpec(name, layer, blocksBelow, actions, Set.copyOf(Arrays.asList(ids)));
  }

  /**
   * Checks whether this context takes an action through a binding.
   *
   * @param action the action
   * @param binding the binding driving the action
   * @return true if the binding reaches the action in this context
   */
  public boolean accepts(ActionId action, Binding binding) {
    return actions.contains(action) && !(binding.isPad() && keyboardOnly.contains(action));
  }
}
