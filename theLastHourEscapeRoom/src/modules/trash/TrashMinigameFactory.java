package modules.trash;

import contrib.components.InventoryComponent;
import contrib.components.UIComponent;
import contrib.hud.UIUtils;
import contrib.hud.dialogs.DialogContext;
import contrib.hud.dialogs.DialogFactory;
import contrib.item.Item;
import contrib.item.concreteItem.HintItem;
import core.Entity;
import java.util.Objects;
import modules.computer.LastHourDialogTypes;

/**
 * Public, single-method API for opening the trashcan minigame.
 *
 * <p>Encapsulates everything the call-site does not need to know about:
 *
 * <ul>
 *   <li>Registering the {@link LastHourDialogTypes#TRASHCAN} dialog type with {@link DialogFactory}
 *       (done once, lazily).
 *   <li>Choosing the texture path shown for the special (winnable) actor: for {@link HintItem}s the
 *       referenced image is used directly so the in-trash visual matches what the player ends up
 *       viewing; for any other {@link Item} the world-animation source path is used so the visual
 *       matches the dropped/world appearance.
 *   <li>Building the {@link DialogContext}, showing the dialog and wiring up the win callback so
 *       the reward is added to the player's inventory and the dialog closed on all clients.
 * </ul>
 *
 * <p>Pass {@code reward == null} to show the minigame purely as flavor (no winnable special actor,
 * nothing to award).
 */
public final class TrashMinigameFactory {

  private static final String FALLBACK_TEXTURE_PATH = "items/rpg/item_paper.png";

  private static boolean registered = false;

  private TrashMinigameFactory() {}

  /**
   * Opens the trashcan minigame for the given player.
   *
   * @param who the interacting player entity; must not be {@code null}
   * @param reward the item awarded on win, or {@code null} to show a non-winnable flavor minigame
   * @param paperCount number of crumpled papers to scatter inside the trashcan
   * @param afterAward optional action run on the server after the reward has been added to the
   *     player's inventory (e.g. flip a "claimed" flag); ignored when {@code reward == null}; may
   *     be {@code null}
   */
  public static void show(Entity who, Item reward, int paperCount, Runnable afterAward) {
    Objects.requireNonNull(who, "who");
    ensureRegistered();

    DialogContext.Builder builder =
        DialogContext.builder()
            .type(LastHourDialogTypes.TRASHCAN)
            .put(TrashMinigameUI.KEY_PAPER_COUNT, paperCount);

    if (reward != null) {
      builder
          .put(TrashMinigameUI.KEY_NOTE_PATH, resolveTexturePath(reward))
          .put(TrashMinigameUI.KEY_CALLBACK_KEY, TrashMinigameUI.DEFAULT_CALLBACK_KEY)
          .put(TrashMinigameUI.KEY_CLOSE_CALLBACK_KEY, TrashMinigameUI.DEFAULT_CLOSE_CALLBACK_KEY);
    }

    DialogContext ctx = builder.build();
    UIComponent ui = DialogFactory.show(ctx, who.id());

    if (reward != null) {
      // Award the reward immediately when the player clicks the special actor, so the item is
      // granted even if the dialog is closed early.
      ui.registerCallback(
          TrashMinigameUI.DEFAULT_CALLBACK_KEY,
          payload -> {
            who.fetch(InventoryComponent.class).ifPresent(inv -> inv.add(reward));
            if (afterAward != null) afterAward.run();
          });
      // Close the dialog only after the win animation has finished playing on the client.
      ui.registerCallback(
          TrashMinigameUI.DEFAULT_CLOSE_CALLBACK_KEY, payload -> UIUtils.closeDialog(ui));
    }
  }

  /**
   * Resolves the texture used for the winnable actor inside the minigame.
   *
   * <p>For {@link HintItem}s this is the referenced image (so the trash visual matches the popup
   * the item opens when used from the inventory). For any other item this is the world-animation
   * source path, falling back to a generic paper sprite if none is available.
   */
  private static String resolveTexturePath(Item reward) {
    if (reward instanceof HintItem hint) {
      return hint.imagePath().pathString();
    }
    return reward
        .worldAnimation()
        .sourcePath()
        .map(p -> p.pathString())
        .orElse(FALLBACK_TEXTURE_PATH);
  }

  private static void ensureRegistered() {
    if (registered) return;
    DialogFactory.register(LastHourDialogTypes.TRASHCAN, TrashMinigameUI::build);
    registered = true;
  }
}
