package engine.network.messages.s2c;

import engine.network.messages.NetworkMessage;
import engine.sound.SoundSpec;

/**
 * Server-to-client message instructing the client to play a sound.
 *
 * <p>If the client already plays the instance, the spec replaces the old one and the sound keeps
 * playing with the new volume, range and attenuation; sound name, pitch and looping stay as they
 * were at the start, see {@link engine.sound.AudioApi#changeVolume(long, float)}.
 *
 * @param entityId the entity emitting the sound (for position tracking)
 * @param soundSpec the specification of the sound to play
 * @see SoundStopMessage
 */
public record SoundPlayMessage(int entityId, SoundSpec soundSpec) implements NetworkMessage {}
