package contrib.modules.interaction;

import java.util.function.Consumer;

/**
 * Represents a user interface for selecting an interaction from a set of available interactions
 * associated with an interactable entity.
 *
 * <p>Implementations of this interface are responsible for managing the presentation logic
 * of the interaction selection, allowing users to choose from various interaction options.
 */
public interface InteractionSelectionUi {

  /**
   * Shows an interaction chooser for the given interactable source.
   *
   * @param interactable source of the selectable interactions
   * @param onSelected callback invoked with the chosen interaction, or {@code null} if canceled
   */
  void show(IInteractable interactable, Consumer<Interaction> onSelected);
}
