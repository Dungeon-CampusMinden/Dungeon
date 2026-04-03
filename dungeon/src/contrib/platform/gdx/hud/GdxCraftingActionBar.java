package contrib.platform.gdx.hud;

import com.badlogic.gdx.graphics.g2d.Batch;
import contrib.crafting.CraftingDialogAction;
import contrib.hud.dialogs.DialogCallbackResolver;
import contrib.hud.elements.Button;
import contrib.hud.elements.CombinableGUI;
import contrib.hud.elements.ImageButton;
import contrib.item.Item;
import core.Game;
import core.utils.components.draw.animation.Animation;
import core.utils.components.path.SimpleIPath;
import java.util.EnumMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * libGDX-specific action bar for the crafting dialog.
 *
 * <p>This class isolates the remaining Button/ImageButton construction and drawing from
 * {@code CraftingGUI}. The dialog itself only references shared crafting actions.
 */
public final class GdxCraftingActionBar {

  private final Map<CraftingDialogAction, Button> buttons =
    new EnumMap<>(CraftingDialogAction.class);

  /**
   * Creates a new action bar for the crafting dialog.
   *
   * @param parent the parent GUI
   * @param dialogId dialog id used for callback dispatch
   * @param craftPayloadSupplier supplies the current crafting payload for the craft action
   */
  public GdxCraftingActionBar(
    CombinableGUI parent, String dialogId, Supplier<Item[]> craftPayloadSupplier) {
    for (CraftingDialogAction action : CraftingDialogAction.values()) {
      Button button = createButton(parent, action);

      button.onClick(
        ignored ->
          DialogCallbackResolver.createButtonCallback(dialogId, action.callbackKey())
            .accept(action == CraftingDialogAction.CRAFT ? craftPayloadSupplier.get() : null));

      buttons.put(action, button);
    }
  }

  /**
   * Updates the action bar button bounds relative to the parent crafting dialog.
   *
   * @param parentX parent x
   * @param parentY parent y
   * @param parentWidth parent width
   * @param parentHeight parent height
   */
  public void updateBounds(int parentX, int parentY, int parentWidth, int parentHeight) {
    for (CraftingDialogAction action : CraftingDialogAction.values()) {
      Button button = buttons.get(action);
      button.width(Math.round(parentWidth * action.relativeWidth()));
      button.height(Math.round(parentHeight * action.relativeHeight()));
      button.x(parentX + Math.round(parentWidth * action.relativeX()));
      button.y(parentY + Math.round(parentHeight * action.relativeY()));
    }
  }

  /**
   * Draws the action bar buttons.
   *
   * @param batch target batch
   */
  public void draw(Batch batch) {
    for (CraftingDialogAction action : CraftingDialogAction.values()) {
      buttons.get(action).draw(batch);
    }
  }

  private static Button createButton(CombinableGUI parent, CraftingDialogAction action) {
    if (Game.isHeadless()) {
      return new Button(parent, 0, 0, 0, 0);
    }

    return new ImageButton(
      parent, new Animation(new SimpleIPath(action.iconPath())), 0, 0, 1, 1);
  }
}
