package feature.trash;

import engine.Entity;
import engine.components.PositionComponent;
import engine.network.messages.s2c.ItemState;
import feature.components.InventoryComponent;
import feature.components.UIComponent;
import feature.hud.UIUtils;
import feature.hud.dialogs.DialogContext;
import feature.hud.dialogs.DialogFactory;
import feature.hud.dialogs.DialogType;
import feature.inventory.Item;
import feature.inventory.items.HintItem;
import java.util.Objects;
import java.util.function.BooleanSupplier;
import tools.jackson.databind.json.JsonMapper;

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
  private static final JsonMapper JSON = JsonMapper.builder().build();

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
    show(who, reward, paperCount, () -> true, afterAward);
  }

  /**
   * Opens the minigame with a server-side guard for a one-time reward.
   *
   * @param who the interacting player
   * @param reward the item awarded on win, or {@code null}
   * @param paperCount number of crumpled papers
   * @param canAward checked when the player wins, before the reward is added
   * @param afterAward action run after a permitted reward was added to the inventory, or dropped at
   *     the player's feet because the inventory was full
   */
  public static void show(
      Entity who, Item reward, int paperCount, BooleanSupplier canAward, Runnable afterAward) {
    Objects.requireNonNull(who, "who");
    Objects.requireNonNull(canAward, "canAward");

    DialogContext.Builder builder =
        DialogContext.builder()
            .type(DialogType.DefaultTypes.TRASHCAN)
            .put(TrashMinigameUI.KEY_PAPER_COUNT, paperCount);

    if (reward != null) {
      builder
          .put(TrashMinigameUI.KEY_NOTE_PATH, resolveTexturePath(reward))
          .put(TrashMinigameUI.KEY_REWARD_ITEM, rewardJson(reward))
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
            if (!canAward.getAsBoolean()) return;
            if (!give(who, reward)) return;
            if (afterAward != null) afterAward.run();
          });
      // Close the dialog only after the win animation has finished playing on the client.
      ui.registerCallback(
          TrashMinigameUI.DEFAULT_CLOSE_CALLBACK_KEY, payload -> UIUtils.closeDialog(ui));
    }
  }

  /**
   * Gives the reward to the player, or drops it at the player's feet if the inventory is full.
   *
   * @param who winning player
   * @param reward found item
   * @return false if the item could neither be added nor dropped
   */
  private static boolean give(Entity who, Item reward) {
    if (who.fetch(InventoryComponent.class).map(inventory -> inventory.add(reward)).orElse(false)) {
      return true;
    }
    return who.fetch(PositionComponent.class)
        .flatMap(position -> reward.drop(position.position()))
        .isPresent();
  }

  private static String rewardJson(Item reward) {
    try {
      return JSON.writeValueAsString(ItemState.fromItem(reward));
    } catch (RuntimeException exception) {
      // The client then shows the texture only, generated textures may be missing.
      return "";
    }
  }

  /**
   * Resolves the texture used for the winnable actor inside the minigame.
   *
   * <p>For {@link HintItem}s this is the referenced image (so the trash visual matches the popup
   * the item opens when used from the inventory). For any other item this is the world-animation
   * source path, falling back to a generic paper sprite if none is available.
   *
   * @param reward the item whose texture should be resolved
   * @return the resolved texture path
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
}
