package contrib.modules.interaction;

import java.util.Objects;
import java.util.function.Consumer;

/**
 * Global access point for backend-specific interaction selection UIs.
 *
 * <p>The active backend installs its implementation during bootstrap.
 */
public final class InteractionSelection {

  private static final InteractionSelectionUi UNSUPPORTED =
    (interactable, onSelected) -> {
      throw new IllegalStateException("No InteractionSelectionUi installed.");
    };

  private static volatile InteractionSelectionUi current = UNSUPPORTED;

  private InteractionSelection() {}

  /**
   * Installs the backend-specific interaction selection UI.
   *
   * @param selectionUi backend-specific implementation
   */
  public static void install(InteractionSelectionUi selectionUi) {
    current = selectionUi == null ? UNSUPPORTED : selectionUi;
  }

  /**
   * Shows the interaction chooser using the currently installed backend implementation.
   *
   * @param interactable source of selectable interactions
   * @param onSelected callback invoked with the chosen interaction, or {@code null} if cancelled
   */
  public static void show(IInteractable interactable, Consumer<Interaction> onSelected) {
    current.show(
      Objects.requireNonNull(interactable, "interactable must not be null"),
      Objects.requireNonNull(onSelected, "onSelected must not be null"));
  }
}
