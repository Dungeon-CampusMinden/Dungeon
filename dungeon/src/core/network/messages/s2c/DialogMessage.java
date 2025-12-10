package core.network.messages.s2c;

import contrib.components.UIComponent;
import core.network.messages.NetworkMessage;

/**
 * Network message for synchronizing dialog state across clients in a multiplayer environment.
 *
 * <p>This server-to-client (s2c) message contains a UIComponent that represents a dialog to be
 * displayed on the receiving client. It is used to ensure that all clients show the same dialogs at
 * the same time in a networked multiplayer game.
 *
 * @param component the UIComponent containing the dialog to be displayed
 */
public record DialogMessage(UIComponent component) implements NetworkMessage {}
