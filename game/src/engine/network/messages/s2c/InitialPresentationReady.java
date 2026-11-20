package engine.network.messages.s2c;

import engine.network.messages.NetworkMessage;

/**
 * Server-to-client message telling a joining client that everything it should see first, e.g. an
 * intro cutscene, has been sent. The client hides the world until it arrives, so players do not see
 * the level for a moment before such a dialog covers it.
 */
public record InitialPresentationReady() implements NetworkMessage {}
