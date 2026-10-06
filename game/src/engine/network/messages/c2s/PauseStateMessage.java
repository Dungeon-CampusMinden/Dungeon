package engine.network.messages.c2s;

import engine.network.messages.NetworkMessage;

/**
 * Client intent: whether its own explicit pause screen is open.
 *
 * @param paused whether the pause menu is open
 */
public record PauseStateMessage(boolean paused) implements NetworkMessage {}
