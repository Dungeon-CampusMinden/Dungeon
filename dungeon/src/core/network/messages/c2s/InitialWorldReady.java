package core.network.messages.c2s;

import core.network.messages.NetworkMessage;

/** Client-to-server: confirms that the client applied the initial world bootstrap. */
public record InitialWorldReady() implements NetworkMessage {}
