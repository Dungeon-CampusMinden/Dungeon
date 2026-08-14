package engine.network.messages.s2c;

import engine.network.messages.NetworkMessage;

/** Server-to-client: marks the end of the initial world bootstrap stream. */
public record InitialWorldComplete() implements NetworkMessage {}
