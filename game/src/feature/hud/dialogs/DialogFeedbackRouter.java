package feature.hud.dialogs;

import engine.Entity;
import engine.Game;
import engine.network.messages.s2c.DialogFeedbackMessage;
import feature.components.UIComponent;

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
        .filter(DialogFeedbackReceiver.class::isInstance)
        .map(DialogFeedbackReceiver.class::cast)
        .ifPresent(receiver -> receiver.applyFeedback(feedback));
  }

  private static boolean hasDialog(Entity entity, String dialogId) {
    return entity
        .fetch(UIComponent.class)
        .map(ui -> ui.dialogContext().dialogId().equals(dialogId))
        .orElse(false);
  }
}
