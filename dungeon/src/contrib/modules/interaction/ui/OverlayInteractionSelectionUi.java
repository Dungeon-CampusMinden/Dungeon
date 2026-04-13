package contrib.modules.interaction.ui;

import contrib.modules.interaction.IInteractable;
import contrib.modules.interaction.Interaction;
import contrib.modules.interaction.InteractionSelectionUi;
import core.ui.overlay.UiOverlayRegistry;
import java.util.function.Consumer;

/** LITIENGINE-backed interaction chooser based on a custom screen overlay. */
public final class OverlayInteractionSelectionUi implements InteractionSelectionUi {

  public static final OverlayInteractionSelectionUi INSTANCE =
    new OverlayInteractionSelectionUi();

  private OverlayInteractionSelectionUi() {}

  @Override
  public void show(IInteractable interactable, Consumer<Interaction> onSelected) {
    InteractionSelectionOverlay overlay =
      new InteractionSelectionOverlay(interactable, onSelected);

    UiOverlayRegistry.add(overlay);
    UiOverlayRegistry.toFront(overlay);
  }
}
