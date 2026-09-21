package engine.network.messages.s2c;

import engine.network.messages.NetworkMessage;
import java.util.Objects;

/**
 * Server-authoritative feedback for an already open dialog.
 *
 * <p>The message contains a localization key instead of rendered text so each client can resolve
 * the feedback in its own language. It is targeted at the client that owns the dialog action and
 * must not be broadcast as shared game state.
 *
 * @param dialogId dialog receiving the feedback
 * @param targetTabKey optional local tab key that should render the feedback
 * @param sourceFingerprint fingerprint of the submitted source, or an empty string when the sender
 *     did not provide one
 * @param messageKey localization key relative to the active room namespace
 * @param successful whether the server accepted the submitted action
 */
public record DialogFeedbackMessage(
    String dialogId,
    String targetTabKey,
    String sourceFingerprint,
    String messageKey,
    boolean successful)
    implements NetworkMessage {

  /** Creates a validated dialog feedback message. */
  public DialogFeedbackMessage {
    Objects.requireNonNull(dialogId, "dialogId");
    Objects.requireNonNull(targetTabKey, "targetTabKey");
    Objects.requireNonNull(sourceFingerprint, "sourceFingerprint");
    Objects.requireNonNull(messageKey, "messageKey");
  }

  /** Creates feedback using the legacy terminal target and no source correlation. */
  public DialogFeedbackMessage(String dialogId, String messageKey, boolean successful) {
    this(dialogId, "", "", messageKey, successful);
  }
}
