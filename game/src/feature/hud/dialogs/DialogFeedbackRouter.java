package feature.hud.dialogs;

import com.badlogic.gdx.scenes.scene2d.Group;
import engine.Entity;
import engine.Game;
import engine.network.messages.s2c.DialogFeedbackMessage;
import feature.components.UIComponent;
import feature.hud.UIUtils;
import java.util.Optional;

/** Delivers targeted dialog updates to the matching local UI instance. */
public final class DialogFeedbackRouter {

  private DialogFeedbackRouter() {}

  /**
   * Delivers feedback to the open dialog with the same ID, if that dialog supports live updates.
   *
   * @param feedback server response received by this process
   */
  public static void deliver(DialogFeedbackMessage feedback) {
    Game.levelEntities()
        .filter(entity -> hasDialog(entity, feedback.dialogId()))
        .findFirst()
        .flatMap(entity -> entity.fetch(UIComponent.class))
        .map(UIComponent::dialog)
        .flatMap(DialogFeedbackRouter::receiver)
        .ifPresent(receiver -> receiver.applyFeedback(feedback));
  }

  // A dialog may sit in a container that places it on the stage.
  private static Optional<DialogFeedbackReceiver> receiver(Group dialog) {
    if (dialog instanceof DialogFeedbackReceiver receiver) return Optional.of(receiver);
    return UIUtils.findTypeInGroup(dialog, DialogFeedbackReceiver.class);
  }

  private static boolean hasDialog(Entity entity, String dialogId) {
    return entity
        .fetch(UIComponent.class)
        .map(ui -> ui.dialogContext().dialogId().equals(dialogId))
        .orElse(false);
  }
}
