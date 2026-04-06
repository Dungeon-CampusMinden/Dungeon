package contrib.platform.gdx.interaction;

import contrib.modules.interaction.IInteractable;
import contrib.modules.interaction.Interaction;
import contrib.modules.interaction.InteractionSelectionUi;
import contrib.modules.interaction.RingMenu;
import java.util.function.Consumer;

/** libGDX-specific interaction chooser based on the existing RingMenu widget. */
public final class GdxInteractionSelectionUi implements InteractionSelectionUi {

  public static final GdxInteractionSelectionUi INSTANCE = new GdxInteractionSelectionUi();

  private GdxInteractionSelectionUi() {}

  @Override
  public void show(IInteractable interactable, Consumer<Interaction> onSelected) {
    RingMenu.show(interactable, onSelected);
  }
}
