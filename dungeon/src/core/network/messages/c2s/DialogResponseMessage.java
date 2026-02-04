package core.network.messages.c2s;

import core.network.messages.NetworkMessage;
import java.io.Serial;
import java.io.Serializable;

/**
 * Client-to-server message reporting a user's interaction with a dialog.
 *
 * <p>Sent by network callbacks created by {@link contrib.hud.dialogs.DialogCallbackResolver} when
 * the user interacts with a dialog (clicks a button, submits input, or closes the dialog).
 *
 * <p>The server validates this message against {@link core.network.server.DialogTracker} to ensure:
 *
 * <ul>
 *   <li>The client was authorized to see this dialog
 *   <li>For shared dialogs, only the first responder's callback is executed
 * </ul>
 *
 * @param dialogId the unique identifier of the dialog being responded to
 * @param callbackKey the callback key to execute (e.g., "onConfirm", "craft"), null for CLOSED
 * @param data optional custom data for the callback, may be null
 * @see core.network.messages.s2c.DialogShowMessage
 * @see core.network.server.DialogTracker
 */
public record DialogResponseMessage(String dialogId, String callbackKey, Serializable data)
    implements NetworkMessage {
  @Serial private static final long serialVersionUID = 2L;
}
