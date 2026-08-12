package engine.network.messages.s2c;

import engine.network.messages.NetworkMessage;

/**
 * Server-to-client message instructing the client to stop a sound.
 *
 * @param soundInstanceId the sound instance to stop
 * @see SoundPlayMessage
 */
public record SoundStopMessage(long soundInstanceId) implements NetworkMessage {}
