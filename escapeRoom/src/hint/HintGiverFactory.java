package hint;

import contrib.components.InteractionComponent;
import contrib.hud.dialogs.OkDialog;
import contrib.hud.dialogs.YesNoDialog;
import core.Entity;
import core.Game;
import core.components.DrawComponent;
import core.components.PositionComponent;
import core.utils.Point;
import core.utils.components.draw.animation.Animation;
import core.utils.components.path.IPath;
import core.utils.components.path.SimpleIPath;
import java.util.Optional;
import java.util.function.BiConsumer;

/** Factory class for hint giver entities. */
public class HintGiverFactory {

  private static final IPath MAILBOX_TEXTURE = new SimpleIPath("objects/mailbox/mailbox_2.png");
  private static final String ASK_FOR_HINT = "Willst du einen Tipp?";
  private static final String AKS_FOR_HINT_TITLE = "Tipps";

  /**
   * Creates a mailbox entity at the given position that gives hints to the player.
   *
   * @param point the position of the mailbox
   * @return the configured mailbox entity
   */
  public static Entity mailbox(Point point) {
    Entity mailbox = new Entity("Hint Mailbox");
    mailbox.add(new PositionComponent(point));
    mailbox.add(new DrawComponent(new Animation(MAILBOX_TEXTURE)));
    mailbox.add(new InteractionComponent(1, true, wantHintInteraction()));
    return mailbox;
  }

  /**
   * Build the Consumer for the Yes/No Dialog.
   *
   * @return Consumer for the Yes/No Dialog.
   */
  private static BiConsumer<Entity, Entity> wantHintInteraction() {
    return (mailbox, player) ->
        Game.system(
            HintSystem.class,
            hintSystem -> {
              Optional<Hint> hintOpt = hintSystem.nextHint();
              hintOpt.ifPresent(hint -> showHintConfirmation(player, hint));
            });
  }

  /**
   * Shows the Yes/No dialog for a hint, and if the player accepts, shows the hint text and adds it
   * to the player's HintStorageComponent.
   *
   * @param player the player entity
   * @param hint the hint to show
   */
  private static void showHintConfirmation(Entity player, Hint hint) {
    YesNoDialog.showYesNoDialog(
        ASK_FOR_HINT,
        AKS_FOR_HINT_TITLE,
        () -> showHintText(player, hint),
        () -> {
          /* no-op on cancel */
        });
  }

  /**
   * Shows the hint text in an OK dialog and adds it to the player's HintStorageComponent.
   *
   * @param player the player entity
   * @param hint the hint to show
   */
  private static void showHintText(Entity player, Hint hint) {
    OkDialog.showOkDialog(
        hint.text(),
        hint.titel(),
        () -> player.fetch(HintLogComponent.class).ifPresent(log -> log.addHint(hint)));
  }
}
