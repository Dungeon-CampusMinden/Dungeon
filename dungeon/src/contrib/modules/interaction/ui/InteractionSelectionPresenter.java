package contrib.modules.interaction.ui;

import contrib.modules.interaction.IInteractable;
import contrib.modules.interaction.Interaction;
import contrib.modules.interaction.InteractionSelectionUi;
import core.ui.overlay.OverlayManager;
import java.util.function.Consumer;

/**
 * Presents an interaction selection panel as an overlay, allowing users to choose from available
 * interactions associated with an interactable entity.
 *
 * <p>This implementation renders an interaction menu using an overlay mechanism and manages the
 * complete lifecycle of the interaction selection UI: displaying the panel, handling user input,
 * and notifying the result via a callback.
 *
 * <p>This class follows the singleton pattern, with its single instance accessible via the {@code
 * INSTANCE} field.
 */
public final class InteractionSelectionPresenter implements InteractionSelectionUi {

  /** The singleton instance of {@code InteractionSelectionPresenter}. */
  public static final InteractionSelectionPresenter INSTANCE = new InteractionSelectionPresenter();

  private InteractionSelectionPresenter() {}

  @Override
  public void show(IInteractable interactable, Consumer<Interaction> onSelected) {
    InteractionSelectionOverlay overlay = new InteractionSelectionOverlay(interactable, onSelected);
    OverlayManager.add(overlay);
    OverlayManager.toFront(overlay);
  }
}

