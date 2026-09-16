package engine.network.messages.s2c;

import engine.network.messages.NetworkMessage;
import feature.hud.dialogs.DialogContext;

/**
 * Server-to-client state indicating that a dialog is open.
 *
 * <p>Repeated delivery of the same dialog ID keeps the existing client instance and its
 * presentation state. A new opening uses a new dialog ID; live content updates use the feature's
 * state synchronization.
 *
 * <p>The {@link DialogContext} is serialized, but callbacks are transient and will be {@code null}
 * on the client. The client should use {@link feature.hud.dialogs.DialogCallbackResolver} to create
 * network-sending callbacks.
 *
 * @param context the dialog configuration (callbacks will be null after deserialization)
 * @param canBeClosed whether the dialog can be closed by the user
 * @see DialogCloseMessage
 * @see engine.network.messages.c2s.DialogResponseMessage
 * @see feature.hud.dialogs.DialogCallbackResolver
 */
public record DialogShowMessage(DialogContext context, boolean canBeClosed)
    implements NetworkMessage {}
