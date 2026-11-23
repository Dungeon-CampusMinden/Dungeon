package feature.interaction.phone;

import engine.Entity;
import engine.network.messages.c2s.DialogResponseMessage;
import engine.network.messages.s2c.DialogFeedbackMessage;
import feature.components.UIComponent;
import feature.hud.UIUtils;
import feature.hud.dialogs.DialogFeedbackFingerprint;
import feature.hud.dialogs.DialogFeedbackRouter;
import java.util.function.Consumer;

/**
 * A call that a line of a phone answers on the server, see {@link PhoneComponent#addLine}.
 *
 * <p>The phone stays open during the call: the line speaks and asks in the phone of the caller, and
 * the caller can dial the next number right after. A line that answers in a dialog of its own hangs
 * up first. Texts may be translation keys that the client of the caller resolves.
 */
public final class PhoneCall {

  private static final Consumer<DialogResponseMessage.Payload> NO_QUESTION = payload -> {};

  private final Entity caller;
  private final UIComponent phone;
  private final String number;

  PhoneCall(Entity caller, UIComponent phone, String number) {
    this.caller = caller;
    this.phone = phone;
    this.number = number;
    // A new call ends the question of the last one, so a late yes does nothing.
    phone.registerCallback(PhoneFactory.YES, NO_QUESTION);
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
    phone.registerCallback(PhoneFactory.YES, NO_QUESTION);
    send(PhoneFactory.SAY, text);
  }

  /**
   * Asks the caller a yes-or-no question in the phone. A no ends the call.
   *
   * @param question the question
   * @param onYes called on the server when the caller answers yes
   */
  public void ask(String question, Runnable onYes) {
    phone.registerCallback(
        PhoneFactory.YES,
        payload -> {
          phone.registerCallback(PhoneFactory.YES, NO_QUESTION);
          onYes.run();
        });
    send(PhoneFactory.ASK, question);
  }

  /** Closes the phone, e.g. so the line can answer in a dialog of its own. */
  public void hangUp() {
    UIUtils.closeDialog(phone);
  }

  // Only the client of the caller hears the line, like the phone is only open there. The number
  // tells the phone which call the text belongs to.
  private void send(String kind, String text) {
    DialogFeedbackRouter.send(
        caller,
        new DialogFeedbackMessage(
            phone.dialogContext().dialogId(),
            kind,
            DialogFeedbackFingerprint.of(number),
            text,
            true));
  }
}
