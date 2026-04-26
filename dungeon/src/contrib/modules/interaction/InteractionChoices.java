package contrib.modules.interaction;

import java.util.List;
import java.util.Objects;

/**
 * Shared interaction ordering for selection UIs.
 *
 * <p>This keeps the gameplay semantics in one place while allowing multiple backends to present the
 * same choices differently.
 */
public final class InteractionChoices {

  private InteractionChoices() {}

  /**
   * Returns the selectable interactions in the shared canonical order.
   *
   * @param interactable source of the interactions
   * @return ordered list of selectable interactions
   */
  public static List<Interaction> from(IInteractable interactable) {
    Objects.requireNonNull(interactable, "interactable must not be null");

    return List.of(
        interactable.look(),
        interactable.interact(),
        interactable.take(),
        interactable.talk(),
        interactable.usewithitem(),
        interactable.attack());
  }
}
