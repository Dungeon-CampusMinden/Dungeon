package feature.interaction.phone;

import engine.Entity;
import engine.network.messages.s2c.DialogFeedbackMessage;
import feature.components.UIComponent;
import feature.hud.UIUtils;
import feature.hud.dialogs.DialogFeedbackRouter;

/**
 * A call that a line of a phone answers on the server, see {@link PhoneComponent#addLine}.
 *
 * <p>The phone stays open during the call: the line speaks and asks in the phone of the caller, and
 * the caller can dial the next number right after. A line that answers in a dialog of its own hangs
 * up first. Texts may be translation keys that the client of the caller resolves.
 */
public final class PhoneCall {

  private final Entity caller;
  private final UIComponent phone;

  PhoneCall(Entity caller, UIComponent phone) {
    this.caller = caller;
    this.phone = phone;
  }

  /**
   * Returns the player who called.
   *
   * @return the calling player
   */
  public Entity caller() {
    return caller;
  }

  /**
   * Shows what the line says in the phone of the caller.
   *
   * @param text the announcement
   */
  public void say(String text) {
    send(PhoneFactory.SAY, text);
  }

  /**
   * Asks the caller a yes-or-no question in the phone. A no ends the call.
   *
   * @param question the question
   * @param onYes called on the server when the caller answers yes
   */
  public void ask(String question, Runnable onYes) {
    phone.registerCallback(PhoneFactory.YES, payload -> onYes.run());
    send(PhoneFactory.ASK, question);
  }

  /** Closes the phone, e.g. so the line can answer in a dialog of its own. */
  public void hangUp() {
    UIUtils.closeDialog(phone);
  }

  // Only the client of the caller hears the line, like the phone is only open there.
  private void send(String kind, String text) {
    DialogFeedbackRouter.send(
        caller, new DialogFeedbackMessage(phone.dialogContext().dialogId(), kind, "", text, true));
  }
}
