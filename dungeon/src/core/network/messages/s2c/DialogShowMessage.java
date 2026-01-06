package core.network.messages.s2c;

import contrib.hud.dialogs.DialogContext;
import core.network.messages.NetworkMessage;
import java.io.Serial;

/**
 * Server-to-client message instructing the client to display a dialog.
 *
 * <p>The {@link DialogContext} is serialized, but callbacks are transient and will be {@code null}
 * on the client. The client should use {@link contrib.hud.dialogs.DialogCallbackResolver} to create
 * network-sending callbacks.
 *
 * @param context the dialog configuration (callbacks will be null after deserialization)
 * @param canBeClosed whether the dialog can be closed by the user
 * @see DialogCloseMessage
 * @see core.network.messages.c2s.DialogResponseMessage
 * @see contrib.hud.dialogs.DialogCallbackResolver
 */
public record DialogShowMessage(DialogContext context, boolean canBeClosed)
    implements NetworkMessage {
  @Serial private static final long serialVersionUID = 1L;
}
