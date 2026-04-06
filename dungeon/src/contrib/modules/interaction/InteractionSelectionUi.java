package contrib.modules.interaction;

import java.util.function.Consumer;

/**
 * Backend-specific launcher for choosing one interaction out of a multi-interaction source.
 *
 * <p>The gameplay layer only depends on this abstraction. Concrete backends are free to implement
 * the UI as a scene2d widget, a LITIENGINE overlay, or any other presentation form.
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
