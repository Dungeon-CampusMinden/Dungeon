package contrib.modules.interaction.ui;

import contrib.modules.interaction.IInteractable;
import contrib.modules.interaction.Interaction;
import contrib.modules.interaction.InteractionSelectionUi;
import core.ui.overlay.OverlayManager;
import java.util.function.Consumer;

/**
 * UI implementation for displaying an overlay allowing users to select an interaction from a set of
 * available interactions associated with an interactable entity.
 *
 * <p>This class uses an overlay mechanism to present interaction choices and manage user selection.
 * It registers the overlay into the {@link OverlayManager} and ensures it is brought to the front
 * for user visibility.
 *
 * <p>This class follows the singleton pattern, with its single instance accessible via the {@code
 * INSTANCE} field.
 */
public final class InteractionMenuUi implements InteractionSelectionUi {

  /** The singleton instance of {@code InteractionSelectionOverlayUi}. */
  public static final InteractionMenuUi INSTANCE = new InteractionMenuUi();

  private InteractionMenuUi() {}

  @Override
  public void show(IInteractable interactable, Consumer<Interaction> onSelected) {
    InteractionMenuOverlay overlay = new InteractionMenuOverlay(interactable, onSelected);

    OverlayManager.add(overlay);
    OverlayManager.toFront(overlay);
  }
}
