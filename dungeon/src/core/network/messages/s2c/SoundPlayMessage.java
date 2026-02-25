package core.network.messages.s2c;

import core.network.messages.NetworkMessage;
import core.sound.SoundSpec;

/**
 * Server-to-client message instructing the client to play a sound.
 *
 * @param entityId the entity emitting the sound (for position tracking)
 * @param soundSpec the specification of the sound to play
 * @see SoundStopMessage
 */
public record SoundPlayMessage(int entityId, SoundSpec soundSpec) implements NetworkMessage {}
