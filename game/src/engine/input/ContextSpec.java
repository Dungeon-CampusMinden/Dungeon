package engine.input;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Declaration of an input context, shared by all its instances.
 *
 * @param name name shown in logs and in the debug HUD
 * @param layer vertical position in the context stack
 * @param blocksBelow true if input not taken here must not reach lower contexts
 * @param passesPad true if pad input passes a blocking context, as around a focused text field
 * @param actions actions this context takes
 * @param closing actions of this context that close it; they may share bindings with each other
 * @param padOnly actions this context takes from the pad only, because their keys mean something
 *     else here
 * @param handled inputs a widget of this context handles itself, with the action it performs on
 *     them; no other action of this context can be bound to such an input
 */
public record ContextSpec(
    String name,
    ContextLayer layer,
    boolean blocksBelow,
    boolean passesPad,
    Set<ActionId> actions,
    Set<ActionId> closing,
    Set<ActionId> padOnly,
    Map<Binding, ActionId> handled) {

  /**
   * Creates a specification of a context that lets input it does not take pass.
   *
   * @param name name shown in logs and in the debug HUD
   * @param layer vertical position in the context stack
   * @param actions actions this context takes
   * @return the specification
   */
  public static ContextSpec of(String name, ContextLayer layer, ActionId... actions) {
    return new ContextSpec(
        name, layer, false, false, Set.of(actions), Set.of(), Set.of(), Map.of());
  }

  /**
   * Returns a copy that keeps all input from the contexts below.
   *
   * @return the copy
   */
  public ContextSpec blocking() {
    return new ContextSpec(name, layer, true, false, actions, closing, padOnly, handled);
  }

  /**
   * Returns a copy that keeps keyboard and mouse from the contexts below, but lets the pad pass.
   *
   * @return the copy
   */
  public ContextSpec blockingExceptPad() {
    return new ContextSpec(name, layer, true, true, actions, closing, padOnly, handled);
  }

  /**
   * Returns a copy that also takes the given actions and closes through them, in addition to the
   * closing actions it already has. A UI lists the action that opens it here, so that the same
   * input closes it again.
   *
   * @param ids the closing actions
   * @return the copy
   */
  public ContextSpec closedBy(ActionId... ids) {
    Set<ActionId> allClosing = new HashSet<>(closing);
    allClosing.addAll(List.of(ids));
    return new ContextSpec(
        name, layer, blocksBelow, passesPad, with(ids), Set.copyOf(allClosing), padOnly, handled);
  }

  /**
   * Returns a copy that also takes the given actions, but only through their pad bindings, in
   * addition to the pad-only actions it already has.
   *
   * @param ids the pad-only actions
   * @return the copy
   */
  public ContextSpec padOnly(ActionId... ids) {
    Set<ActionId> allPadOnly = new HashSet<>(padOnly);
    allPadOnly.addAll(List.of(ids));
    return new ContextSpec(
        name, layer, blocksBelow, passesPad, with(ids), closing, Set.copyOf(allPadOnly), handled);
  }

  /**
   * Returns a copy that names inputs a widget of this context handles itself, such as the right
   * click on an inventory slot. The widget takes them before any action starts, so the conflict
   * check keeps every other action of this context off them.
   *
   * @param action the action the widget performs on these inputs
   * @param inputs the inputs, without prefixes
   * @return the copy
   */
  public ContextSpec handles(ActionId action, Binding... inputs) {
    Map<Binding, ActionId> all = new HashMap<>(handled);
    for (Binding input : inputs) all.put(input, action);
    return new ContextSpec(
        name, layer, blocksBelow, passesPad, actions, closing, padOnly, Map.copyOf(all));
  }

  private Set<ActionId> with(ActionId... ids) {
    Set<ActionId> all = new HashSet<>(actions);
    all.addAll(List.of(ids));
    return Set.copyOf(all);
  }

  /**
   * Checks whether this context takes an action through a binding.
   *
   * @param action the action
   * @param binding the binding driving the action
   * @return true if the binding reaches the action in this context
   */
  public boolean accepts(ActionId action, Binding binding) {
    return actions.contains(action) && (binding.isPad() || !padOnly.contains(action));
  }

  /**
   * Checks whether this context keeps input from the contexts below.
   *
   * @param pad true for pad input, false for keyboard and mouse
   * @return true if such input does not pass
   */
  public boolean blocks(boolean pad) {
    return blocksBelow && !(pad && passesPad);
  }
}
