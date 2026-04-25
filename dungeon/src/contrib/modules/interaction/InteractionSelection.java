package contrib.modules.interaction;

import contrib.modules.interaction.ui.InteractionSelectionUi;

import java.util.Objects;
import java.util.function.Consumer;

/**
 * Provides a static API for managing and displaying an interaction selection UI.
 *
 * <p>This class serves as a centralized abstraction for managing backend-specific implementations
 * of interaction selection UIs. It allows the gameplay layer to trigger interaction selection
 * without being tied to a specific UI technology or framework.
 *
 * <p>The {@code InteractionSelection} class maintains a single backend implementation that must be
 * installed using the {@link #install(InteractionSelectionUi)} method. Once installed, the
 * {@link #show(IInteractable, Consumer)} method can be used to display the interaction chooser and
 * handle user selections.
 *
 * <p>If no backend is installed and the {@code show} method is called, an exception will be thrown.
 *
 * <p>This class is utility-based and is not meant to be instantiated.
 */
public final class InteractionSelection {

  private static final InteractionSelectionUi UNSUPPORTED =
    (_, _) -> {
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
   * @param onSelected callback invoked with the chosen interaction, or {@code null} if canceled
   */
  public static void show(IInteractable interactable, Consumer<Interaction> onSelected) {
    current.show(
      Objects.requireNonNull(interactable, "interactable must not be null"),
      Objects.requireNonNull(onSelected, "onSelected must not be null"));
  }
}
