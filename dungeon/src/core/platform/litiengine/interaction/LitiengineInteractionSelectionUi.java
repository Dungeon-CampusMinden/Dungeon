package core.platform.litiengine.interaction;

import contrib.modules.interaction.IInteractable;
import contrib.modules.interaction.Interaction;
import contrib.modules.interaction.InteractionSelectionUi;
import core.ui.overlay.LitiengineUiOverlayRegistry;
import java.util.function.Consumer;

/** LITIENGINE-backed interaction chooser based on a custom screen overlay. */
public final class LitiengineInteractionSelectionUi implements InteractionSelectionUi {

  public static final LitiengineInteractionSelectionUi INSTANCE =
    new LitiengineInteractionSelectionUi();

  private LitiengineInteractionSelectionUi() {}

  @Override
  public void show(IInteractable interactable, Consumer<Interaction> onSelected) {
    LitiengineInteractionSelectionOverlay overlay =
      new LitiengineInteractionSelectionOverlay(interactable, onSelected);

    LitiengineUiOverlayRegistry.add(overlay);
    LitiengineUiOverlayRegistry.toFront(overlay);
  }
}
