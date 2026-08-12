package engine.network.messages.s2c;

import engine.network.messages.NetworkMessage;

/**
 * Server-to-client message instructing the client to close a dialog.
 *
 * <p>Sent after the server processes a {@link engine.network.messages.c2s.DialogResponseMessage} or
 * when the server programmatically closes a dialog (e.g., NPC dies while dialog is open).
 *
 * @param dialogId the unique identifier of the dialog to close
 * @see DialogShowMessage
 * @see engine.network.server.DialogTracker#closeDialog(String)
 */
public record DialogCloseMessage(String dialogId) implements NetworkMessage {}
